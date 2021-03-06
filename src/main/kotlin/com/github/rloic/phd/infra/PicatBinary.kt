package com.github.rloic.phd.infra

import com.github.rloic.phd.core.mzn.FznModel
import com.github.rloic.phd.core.mzn.Mzn2FznCompiler
import com.github.rloic.phd.core.mzn.SolverKind.CP_SAT
import com.github.rloic.phd.core.utils.FromArgsWith
import com.github.rloic.phd.core.utils.expectArgument

class PicatBinary(
    private val path: String,
    private val fznPicatSatPi: String,
    mzn2fzn: Mzn2FznCompiler
) : CommonFznSolver(mzn2fzn, CP_SAT) {

    companion object : FromArgsWith<PicatBinary, MiniZincBinary> {
        override fun from(args: Map<String, String>, with: MiniZincBinary) = PicatBinary(
            args.expectArgument("Picat"),
            args.expectArgument("PicatFzn"),
            with
        )
    }

    override fun buildCommand(model: FznModel): List<String> =
        listOf(path, fznPicatSatPi, model.value.absolutePath.removeSuffix(".fzn"))
}