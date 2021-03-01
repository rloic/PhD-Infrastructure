package com.github.rloic.phd.infra

import com.github.rloic.phd.core.mzn.FznModel
import com.github.rloic.phd.core.mzn.Mzn2FznCompiler
import com.github.rloic.phd.core.mzn.SolverKind.CP_SAT

class OrToolsBinary(private val path: String, mzn2fzn: Mzn2FznCompiler): CommonFznSolver(mzn2fzn, CP_SAT) {
    override fun buildCommand(model: FznModel) = listOf(path, model.value.absolutePath)
}