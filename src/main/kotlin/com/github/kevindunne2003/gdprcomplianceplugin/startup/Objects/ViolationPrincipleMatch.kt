package com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects

data class ViolationPrincipleMatch(
    var principles: MutableList<Principles>?,
    var violationData: ViolationData,
)