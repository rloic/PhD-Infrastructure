package com.github.phd.infra

import com.github.phd.core.mzn.*
import com.github.phd.core.mzn.SolverKind.*
import com.github.phd.core.utils.FromArgs
import com.github.phd.core.utils.expectArgument
import com.github.phd.core.utils.logger
import java.io.File
import java.lang.RuntimeException
import java.lang.StringBuilder

class MiniZincBinary(private val executable: String): Mzn2FznCompiler {
    companion object : FromArgs<MiniZincBinary> {
        const val SOLVER_ARG_KEY = "--solver"
        const val COMPILER_ARG_KEY = "--compile"
        const val CP_SOLVER = "cp"
        const val MIP_SOLVER = "mip"
        const val SERIALIZED_DATA_ARG_KEY = "-D"

        override fun from(args: Map<String, String>) = MiniZincBinary(args.expectArgument("MiniZinc"))
    }

    private fun toArg(solver: SolverKind) = when (solver) {
        CP, CP_SAT, SAT -> CP_SOLVER
        MIP -> MIP_SOLVER
    }

    private fun String.replaceLast(delimiter: String, replacement: String): String {
        val index = lastIndexOf(delimiter)
        if (index == -1) return this
        return substring(0, index) + replacement + substring(index + delimiter.length)
    }

    override fun format(mznSolution: MznSolution, oznFile: File): MznSolution {
        val tmpFile = File.createTempFile("solution", "in")
        tmpFile.writeText(mznSolution.content)

        val process = ProcessBuilder(executable, "--ozn-file", oznFile.absolutePath)
            .redirectInput(tmpFile)
            .start()

        val content = StringBuilder()
        process.inputStream.bufferedReader().useLines { lines ->
            for (line in lines) { content.appendLine(line) }
        }

        process.waitFor()
        return MznSolution(content.toString())
    }

    override fun compile(mznModel: MznModel, data: Map<String, Any>, solver: SolverKind, vararg mznArgs: String): FznModel {

        val process = if (data.isEmpty()) {
            logger.debug(listOf(executable, SOLVER_ARG_KEY, toArg(solver), *mznArgs, COMPILER_ARG_KEY, mznModel.value.absolutePath).joinToString(" "))
            ProcessBuilder(executable, SOLVER_ARG_KEY, toArg(solver), COMPILER_ARG_KEY, *mznArgs, mznModel.value.absolutePath)
                .inheritIO()
                .start()
        } else {
            val serializedData = data.entries.joinToString(";") { (key, value) -> "$key=$value" }
            logger.debug(listOf(executable, SOLVER_ARG_KEY, toArg(solver), COMPILER_ARG_KEY, *mznArgs, mznModel.value.absolutePath, SERIALIZED_DATA_ARG_KEY, serializedData).joinToString(" "))
            ProcessBuilder(executable, SOLVER_ARG_KEY, toArg(solver), COMPILER_ARG_KEY, *mznArgs, mznModel.value.absolutePath, SERIALIZED_DATA_ARG_KEY, serializedData)
                .inheritIO()
                .start()
        }

        if(process.waitFor() != 0) { throw RuntimeException("Minizinc failed to compile ${mznModel.value}") }

        return FznModel(File(mznModel.value.absolutePath.replaceLast(".mzn", ".fzn")))
    }
}