package com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects

data class ViolationData(
    var comparisonDataOfVar: ComparisonData,
    val comparisonDataOfMethod: ComparisonData
)
