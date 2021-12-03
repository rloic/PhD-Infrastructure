package com.github.phd.infra

import com.github.phd.core.mzn.FznModel
import com.github.phd.core.mzn.Mzn2FznCompiler
import com.github.phd.core.mzn.SolverKind.CP_SAT
import com.github.phd.core.utils.FromArgsWith
import com.github.phd.core.utils.expectArgument

class OrToolsBinary(private val path: String, mzn2fzn: Mzn2FznCompiler): CommonFznSolver(mzn2fzn, CP_SAT) {

    companion object : FromArgsWith<OrToolsBinary, MiniZincBinary> {
        override fun from(args: Map<String, String>, with: MiniZincBinary) = OrToolsBinary(
            args.expectArgument("OrTools"),
            with
        )
    }


    override fun buildCommand(model: FznModel) = listOf(path, model.value.absolutePath)
}