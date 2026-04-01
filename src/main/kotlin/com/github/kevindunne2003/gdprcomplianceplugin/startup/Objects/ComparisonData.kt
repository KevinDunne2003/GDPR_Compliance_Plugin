package com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects

import com.intellij.psi.PsiElement

//With keyword comparisons, the row value will be null. The indexing here is saved in column
data class ComparisonData(
    val dataCatName: String,
    val typeOfDataCatVal: DataCatalogueType,
    val column: Int,
    val row: Int? = null,
    val psiElement: PsiElement,
    val psiType: PSIType
)