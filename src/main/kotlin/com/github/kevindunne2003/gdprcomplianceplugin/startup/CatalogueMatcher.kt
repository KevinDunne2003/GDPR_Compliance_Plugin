package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.DataCatalogueType
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.PSIType
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

class CatalogueMatcher {

    private var varsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var paramVarsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var fieldsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var methodsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var classesThatMatchDataCat = mutableListOf<ComparisonData>()

    fun compareAllPSIObjects() {
        compareFieldsWithDataCatalogue()
        compareMethodsWithDataCatalogue()
        compareClassesWithDataCatalogue()
        compareLocalVariablesWithDataCatalogue()
        compareParamVariablesWithDataCatalogue()

    }

    private fun compareFieldsWithDataCatalogue() {
        for (i in 0 until PSICodeScanningEngine.getFieldsInProject().size) {
            val psiField = PSICodeScanningEngine.getFieldsInProject()[i]
            compareCatalogueWithStringNames(
                psiField,
                PSIType.Field,
                psiField.name,
                DataCatalogueType.PersonalData
            )
        }
    }

    private fun compareParamVariablesWithDataCatalogue() {
        for (i in 0 until PSICodeScanningEngine.getParamVariablesInProject().size) {
            val psiParamVar = PSICodeScanningEngine.getParamVariablesInProject()[i]
            compareCatalogueWithStringNames(
                psiParamVar,
                PSIType.ParameterVariable,
                psiParamVar.name,
                DataCatalogueType.PersonalData
            )
        }
    }

    private fun compareLocalVariablesWithDataCatalogue() {
        for (i in 0 until PSICodeScanningEngine.getLocalVariablesInProject().size) {
            val psiLocalVar = PSICodeScanningEngine.getLocalVariablesInProject()[i]
            compareCatalogueWithStringNames(
                psiLocalVar,
                PSIType.LocalVariable,
                psiLocalVar.name,
                DataCatalogueType.PersonalData
            )
        }
    }

    private fun compareMethodsWithDataCatalogue() {
        for (i in 0 until PSICodeScanningEngine.getMethodsInsideProject().size) {
            val psiMethod = PSICodeScanningEngine.getMethodsInsideProject()[i]
            compareCatalogueWithStringNames(
                psiMethod,
                PSIType.Method,
                psiMethod.name,
                DataCatalogueType.Keyword
            )
            compareCatalogueWithStringNames(
                psiMethod,
                PSIType.Method,
                psiMethod.name,
                DataCatalogueType.RelatedTerm
            )
        }
    }

    private fun compareClassesWithDataCatalogue() {
        for (i in 0 until PSICodeScanningEngine.getClassesInProject().size) {
            //May have a null name for the class
            val psiClass = PSICodeScanningEngine.getClassesInProject()[i]
            if (psiClass.name != null) {
                compareCatalogueWithStringNames(
                    psiClass,
                    PSIType.Class,
                    psiClass.name.toString(),
                    DataCatalogueType.Keyword
                )
                compareCatalogueWithStringNames(
                    psiClass,
                    PSIType.Class,
                    psiClass.name.toString(),
                    DataCatalogueType.RelatedTerm
                )
            }
        }
    }


    private fun compareCatalogueWithStringNames(
        psiElement: PsiElement,
        psiType: PSIType,
        stringNameOfOject: String,
        typeOfDataCatVal: DataCatalogueType
    ) {
        var comparisonData: ComparisonData? = null

        if (typeOfDataCatVal == DataCatalogueType.Keyword) {
            comparisonData = compareKeywordsWithString(stringNameOfOject, psiElement, psiType)
        } else if (typeOfDataCatVal == DataCatalogueType.RelatedTerm) {
            comparisonData = compareRelTermsWithString(stringNameOfOject, psiElement, psiType)
        } else if (typeOfDataCatVal == DataCatalogueType.PersonalData) {
            comparisonData = comparePerDataWithString(stringNameOfOject, psiElement, psiType)
        } else {
            println("Invalid int sent to compareCatalogueWithStringNames")
        }
        if (comparisonData != null) {
            addCompareDataToGlobalVariables(comparisonData)
        }
    }

    private fun addCompareDataToGlobalVariables(comparisonData: ComparisonData) {
        if (comparisonData.psiType == PSIType.LocalVariable) {
            varsThatMatchDataCat.add(comparisonData)
        } else if (comparisonData.psiType == PSIType.Method) {
            methodsThatMatchDataCat.add(comparisonData)
        } else if (comparisonData.psiType == PSIType.Class) {
            classesThatMatchDataCat.add(comparisonData)
        } else if (comparisonData.psiType == PSIType.ParameterVariable) {
            paramVarsThatMatchDataCat.add(comparisonData)
        } else if (comparisonData.psiType == PSIType.Field) {
            fieldsThatMatchDataCat.add(comparisonData)
        }
    }

    //Comparison is to see if the String name of the object contains the data catalogues keyword
    private fun compareKeywordsWithString(stringNameOfOject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatKeywords().size) {
            if (lowerCaseAndCompareTwoStrings(stringNameOfOject, DataCatalogueLoader.getDataCatKeywords().get(i))) {
                val comparisonData = ComparisonData(
                    DataCatalogueLoader.getDataCatKeywords().get(i),
                    DataCatalogueType.Keyword, i, psiElement = psiObject, psiType = psiType
                )
                return comparisonData
            }
        }
        return null
    }

    //Comparison is to see if the String name of the object contains the data catalogues keyword
    private fun compareRelTermsWithString(stringNameOfOject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatRelatedTerms().size) {
            for (j in 0 until DataCatalogueLoader.getDataCatRelatedTerms().get(i).size) {
                if (lowerCaseAndCompareTwoStrings(
                        stringNameOfOject,
                        DataCatalogueLoader.getDataCatRelatedTerms().get(i).get(j)
                    )
                ) {
                    val comparisonData =
                        ComparisonData(
                            DataCatalogueLoader.getDataCatRelatedTerms().get(i).get(j),
                            DataCatalogueType.RelatedTerm,
                            j,
                            i,
                            psiElement = psiObject,
                            psiType = psiType
                        )
                    return comparisonData
                }
            }
        }
        return null
    }

    private fun comparePerDataWithString(stringNameOfOject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatPersonalData().size) {
            for (j in 0 until DataCatalogueLoader.getDataCatPersonalData().get(i).size) {
                if (lowerCaseAndCompareTwoStrings(
                        stringNameOfOject,
                        DataCatalogueLoader.getDataCatPersonalData().get(i).get(j)
                    )
                ) {
                    val comparisonData =
                        ComparisonData(
                            DataCatalogueLoader.getDataCatRelatedTerms().get(i).get(j),
                            DataCatalogueType.PersonalData,
                            j,
                            i,
                            psiElement = psiObject,
                            psiType = psiType
                        )
                    return comparisonData
                }
            }
        }
        return null
    }

    //All data is converted to lowercase for comparisons
    private fun lowerCaseAndCompareTwoStrings(stringNameOfObject: String, dataCatString: String): Boolean {
        val dataCatLowerCase = dataCatString.lowercase()
        val stringNameOfOjectLowerCase = stringNameOfObject.lowercase()

        if (stringNameOfOjectLowerCase.contains(dataCatLowerCase)) {
            return true
        }
        return false
    }


    /*fun compareDataCatKeywordsWithVariableNames() {
        for (i in 0 until DataCatalogueLoader.getDataCatKeywords().size) {
            for (j in 0 until CodeScanningEngine.getVariableNamesInsideProject().size) {
                if (DataCatalogueLoader.getDataCatKeywords().get(i)
                        .equals(CodeScanningEngine.getVariableNamesInsideProject().get(j), ignoreCase = true)
                ) {
                    varNamesThatMatchDataCat.add(
                        codeType(
                            CodeScanningEngine.getVariableNamesInsideProject().get(j),
                            1,
                            i
                        )
                    )
                }
            }
        }
    }

    fun compareDataCatKeywordsWithMethodNames() {
        for (i in 0 until DataCatalogueLoader.getDataCatKeywords().size) {
            for (j in 0 until CodeScanningEngine.getMethodNamesInsideProject().size) {
                if (DataCatalogueLoader.getDataCatKeywords().get(i)
                        .equals(CodeScanningEngine.getMethodNamesInsideProject().get(j), ignoreCase = true)
                ) {
                    methodNamesThatMatchDataCat.add(
                        codeType(
                            CodeScanningEngine.getMethodNamesInsideProject().get(j),
                            1,
                            i
                        )
                    )
                }
            }
        }
    }

    fun compareDataCatKeywordsWithClassNames() {
        for (i in 0 until DataCatalogueLoader.getDataCatKeywords().size) {
            for (j in 0 until CodeScanningEngine.getClassNamesInsideProject().size) {
                if (DataCatalogueLoader.getDataCatKeywords().get(i)
                        .equals(CodeScanningEngine.getClassNamesInsideProject().get(j), ignoreCase = true)
                ) {
                    classNamesThatMatchDataCat.add(
                        codeType(
                            CodeScanningEngine.getClassNamesInsideProject().get(j),
                            1,
                            i
                        )
                    )
                }
            }
        }
    }
*/
}