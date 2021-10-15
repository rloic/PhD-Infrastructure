package com.github.rloic.phd.infra

import com.github.rloic.phd.core.mzn.*
import com.github.rloic.phd.core.utils.logger

abstract class CommonFznSolver(
    private val mzn2fzn: Mzn2FznCompiler,
    private val solver: SolverKind
) : MznSolver {

    data class CommandAndProcess(
        val command: List<String>,
        val process: Process
    )

    class ExecutionError(command: List<String>): RuntimeException("Invalid return code for ${command.joinToString(" ")}")

    abstract fun buildCommand(model: FznModel): List<String>

    override fun optimize(model: MznModel.Optimization, data: Map<String, Any>, vararg mznArgs: String): MznSolution? {
        val (command, process) = createCommandAndProcess(model, data, *mznArgs)

        var prevSolution = StringBuilder()
        var solution = StringBuilder()
        var tmp: StringBuilder

        process.inputStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                if (line.startsWith("---")) {
                    tmp = prevSolution
                    prevSolution = solution
                    solution = tmp
                    solution.clear()
                } else {
                    solution.appendLine(line)
                }
            }
        }

        if (process.waitFor() != 0) throw ExecutionError(command)

        return if (prevSolution.isEmpty()) {
            null
        } else {
            MznSolution(prevSolution.toString())
        }
    }

    override fun solveOnce(model: MznModel.CompleteSearch, data: Map<String, Any>, vararg mznArgs: String): MznSolution? =
        solve(model, data, *mznArgs)

    override fun solveOnce(model: MznModel.PartialSearch, data: Map<String, Any>, vararg mznArgs: String): MznSolution? =
        solve(model, data, *mznArgs)

    private fun createCommandAndProcess(model: MznModel, data: Map<String, Any>, vararg mznArgs: String): CommandAndProcess {
        val fznModel = mzn2fzn.compile(model, data, solver, *mznArgs)

        val command = buildCommand(fznModel)
        logger.debug(command.joinToString(" "))
        val process = ProcessBuilder(command)
            .start()

        return CommandAndProcess(command, process)
    }

    private fun solve(model: MznModel, data: Map<String, Any>, vararg mznArgs: String): MznSolution? {
        val (command, process) = createCommandAndProcess(model, data, *mznArgs)

        val solution = StringBuilder()
        process.inputStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                solution.appendLine(line)
            }
        }

        if (process.waitFor() != 0) throw ExecutionError(command)

        return if (solution.endsWith("=====UNSATISFIABLE=====\n")) {
            null
        } else {
            MznSolution(solution.toString())
        }
    }
}