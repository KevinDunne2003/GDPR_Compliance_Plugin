package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.DataCatalogueType
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.PSIType
import com.intellij.psi.PsiElement
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ComparisonData
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

object CatalogueMatcher {

    private var varsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var paramVarsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var fieldsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var methodsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var classesThatMatchDataCat = mutableListOf<ComparisonData>()

    private fun clearFieldVariables(){
        varsThatMatchDataCat.clear()
        paramVarsThatMatchDataCat.clear()
        fieldsThatMatchDataCat.clear()
        methodsThatMatchDataCat.clear()
        classesThatMatchDataCat.clear()
    }

    fun compareAllPSIObjects() {
        clearFieldVariables()

        //compareClassesWithDataCatalogue()
        //compareMethodsWithDataCatalogue()
        //compareFieldsWithDataCatalogue()
        //compareLocalVariablesWithDataCatalogue()
        compareParamVariablesWithDataCatalogue()

        printAllFieldVariablesInObjectForTesting()
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

   /* private fun compareEnclosingMethodMethodWithDataCatalogueGivenPsiLocalVariable(psiLocalVariable: PsiLocalVariable): ComparisonData {
        var method = getEnclosingMethod(psiLocalVariable)

        compareKeywordsWithString()
    }

    private fun compareEnclosingClassWithDataCatalogueGivenPsiLocalVariable(psiLocalVariable: PsiLocalVariable): ComparisonData{

    }*/

    private fun compareCatalogueWithStringNames(
        psiElement: PsiElement,
        psiType: PSIType,
        stringNameOfObject: String,
        typeOfDataCatVal: DataCatalogueType
    ) {
        var comparisonData: ComparisonData? = null

        if (typeOfDataCatVal == DataCatalogueType.Keyword) {
            comparisonData = compareKeywordsWithString(stringNameOfObject, psiElement, psiType)
        } else if (typeOfDataCatVal == DataCatalogueType.RelatedTerm) {
            comparisonData = compareRelTermsWithString(stringNameOfObject, psiElement, psiType)
        } else if (typeOfDataCatVal == DataCatalogueType.PersonalData) {
            comparisonData = comparePerDataWithString(stringNameOfObject, psiElement, psiType)
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
    private fun compareKeywordsWithString(stringNameOfObject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatKeywords().size) {
            if (lowerCaseConversionAndCompareTwoStrings(stringNameOfObject, DataCatalogueLoader.getDataCatKeywords().get(i))) {
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
    private fun compareRelTermsWithString(stringNameOfObject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatRelatedTerms().size) {
            for (j in 0 until DataCatalogueLoader.getDataCatRelatedTerms().get(i).size) {
                if (lowerCaseConversionAndCompareTwoStrings(
                        stringNameOfObject,
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

    private fun comparePerDataWithString(stringNameOfObject: String, psiObject: PsiElement, psiType: PSIType): ComparisonData? {
        for (i in 0 until DataCatalogueLoader.getDataCatPersonalData().size) {
            for (j in 0 until DataCatalogueLoader.getDataCatPersonalData().get(i).size) {
                if (lowerCaseConversionAndCompareTwoStrings(
                        stringNameOfObject,
                        DataCatalogueLoader.getDataCatPersonalData().get(i).get(j)
                    )
                ) {
                    val comparisonData =
                        ComparisonData(
                            DataCatalogueLoader.getDataCatPersonalData().get(i).get(j),
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
    private fun lowerCaseConversionAndCompareTwoStrings(stringNameOfObject: String, dataCatString: String): Boolean {
        val dataCatLowerCase = dataCatString.lowercase()
        val stringNameOfObjectLowerCase = stringNameOfObject.lowercase()

        if (stringNameOfObjectLowerCase.contains(dataCatLowerCase)) {
            return true
        }
        return false
    }

    fun getEnclosingMethod(psiLocalVariable: PsiLocalVariable): PsiMethod? {
        return PsiTreeUtil.getParentOfType(psiLocalVariable, PsiMethod::class.java)
    }

    private fun printComparisonDataListForTesting(name: String, list: List<ComparisonData>) {
        println("---- $name ----")
        if (list.isEmpty()) {
            println("No entries")
        } else {
            list.forEach { println(it) }
        }
        println()
    }

    private fun printVarsThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("varsThatMatchDataCat", varsThatMatchDataCat)
    }

    private fun printParamVarsThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("paramVarsThatMatchDataCat", paramVarsThatMatchDataCat)
    }

    private fun printFieldsThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("fieldsThatMatchDataCat", fieldsThatMatchDataCat)
    }

    private fun printMethodsThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("methodsThatMatchDataCat", methodsThatMatchDataCat)
    }

    private fun printClassesThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("classesThatMatchDataCat", classesThatMatchDataCat)
    }

    private fun printAllFieldVariablesInObjectForTesting(){
        printVarsThatMatchDataCatForTesting()
        printParamVarsThatMatchDataCatForTesting()
        printFieldsThatMatchDataCatForTesting()
        printMethodsThatMatchDataCatForTesting()
        printClassesThatMatchDataCatForTesting()
    }
}