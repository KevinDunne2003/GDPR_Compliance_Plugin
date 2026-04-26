package com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.github.kevindunne2003.gdprcomplianceplugin.startup.DataCatalogueRepository
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ComparisonData
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.DataCatalogueType
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.PSIType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

object CatalogueMatcher {

    private var varsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var paramVarsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var fieldsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var methodsThatMatchDataCat = mutableListOf<ComparisonData>()
    private var classesThatMatchDataCat = mutableListOf<ComparisonData>()

    private fun clearFieldVariables() {
        varsThatMatchDataCat.clear()
        paramVarsThatMatchDataCat.clear()
        fieldsThatMatchDataCat.clear()
        methodsThatMatchDataCat.clear()
        classesThatMatchDataCat.clear()
    }

    fun compareAllPSIObjects() {
        clearFieldVariables()

        compareClassesWithDataCatalogue()
        compareMethodsWithDataCatalogue()
        compareFieldsWithDataCatalogue()
        compareLocalVariablesWithDataCatalogue()
        compareParamVariablesWithDataCatalogue()

        println("Num Of vars: "+ varsThatMatchDataCat.size)
        println("Num of params: "+ paramVarsThatMatchDataCat.size)
        println("Num of fields: "+ fieldsThatMatchDataCat.size)
        println("Num of methods: "+ methodsThatMatchDataCat.size)
        println("Num of classes: "+classesThatMatchDataCat.size)

        //printAllFieldVariablesInObjectForTesting()
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
    private fun compareKeywordsWithString(
        stringNameOfObject: String,
        psiObject: PsiElement,
        psiType: PSIType
    ): ComparisonData? {
        for (i in 0 until DataCatalogueRepository.getDataCatalogueKeywords().size) {
            if (lowerCaseConversionAndCompareTwoStrings(
                    stringNameOfObject,
                    DataCatalogueRepository.getDataCatalogueKeywords().get(i)
                )
            ) {
                val comparisonData = ComparisonData(
                    DataCatalogueRepository.getDataCatalogueKeywords().get(i),
                    DataCatalogueType.Keyword, i, psiElement = psiObject, psiType = psiType
                )
                return comparisonData
            }
        }
        return null
    }

    //Comparison is to see if the String name of the object contains the data catalogues keyword
    private fun compareRelTermsWithString(
        stringNameOfObject: String,
        psiObject: PsiElement,
        psiType: PSIType
    ): ComparisonData? {
        for (i in 0 until DataCatalogueRepository.getDataCatalogueRelatedTerms().size) {
            for (j in 0 until DataCatalogueRepository.getDataCatalogueRelatedTerms().get(i).size) {
                if (lowerCaseConversionAndCompareTwoStrings(
                        stringNameOfObject,
                        DataCatalogueRepository.getDataCatalogueRelatedTerms().get(i).get(j)
                    )
                ) {
                    val comparisonData =
                        ComparisonData(
                            DataCatalogueRepository.getDataCatalogueRelatedTerms().get(i).get(j),
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

    private fun comparePerDataWithString(
        stringNameOfObject: String,
        psiObject: PsiElement,
        psiType: PSIType
    ): ComparisonData? {
        for (i in 0 until DataCatalogueRepository.getDataCataloguePersonalData().size) {
            for (j in 0 until DataCatalogueRepository.getDataCataloguePersonalData().get(i).size) {
                if (lowerCaseConversionAndCompareTwoStrings(
                        stringNameOfObject,
                        DataCatalogueRepository.getDataCataloguePersonalData().get(i).get(j)
                    )
                ) {
                    val comparisonData =
                        ComparisonData(
                            DataCatalogueRepository.getDataCataloguePersonalData().get(i).get(j),
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

    private fun getEnclosingMethod(psiLocalVariable: PsiLocalVariable): PsiMethod? {
        return PsiTreeUtil.getParentOfType(psiLocalVariable, PsiMethod::class.java)
    }

    //Written by AI for testing purposes
    private fun getLineNumber(element: PsiElement): Int? {
        val project = element.project

        val psiFile = element.containingFile ?: return null
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return null

        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val offset = element.textOffset
        return document.getLineNumber(offset) + 1
    }

    //Written by AI for testing purposes
    private fun getContainingClassName(element: PsiElement): String? {
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
        return psiClass?.qualifiedName ?: psiClass?.name
    }

    private fun printComparisonDataListForTesting(name: String, list: List<ComparisonData>) {
        println("---- $name ----")
        if (list.isEmpty()) {
            println("No entries")
        } else {
            list.forEach {
                val element = it.psiElement

                val line = getLineNumber(element)
                val className = getContainingClassName(element)

                println(it)
                println("  -> Line: ${line ?: "unknown"}")
                println("  -> Class: ${className ?: "unknown"}")
            }

        }
        println()
    }

    private fun concatenateComparisonDataListForTesting(name: String, list: List<ComparisonData>) {
        val output = buildString {
            appendLine("---- $name ----")
            if (list.isEmpty()) {
                appendLine("No entries")
            } else {
                list.forEach {
                    val element = it.psiElement

                    val line = getLineNumber(element)
                    val className = getContainingClassName(element)

                    appendLine(it.toString())
                    appendLine("  -> Line: ${line ?: "unknown"}")
                    appendLine("  -> Class: ${className ?: "unknown"}")
                }
            }
            appendLine()
        }

        saveReportAsTxt(name, output)
    }

    private fun saveReportAsTxt(name: String, content: String) {
        val safeName = name.replace("\\s+".toRegex(), "_")
        val file = File("build/test-output/$safeName.txt")

        file.parentFile.mkdirs()
        file.writeText(content)

        println("Saved report to: ${file.absolutePath}")
    }

    //Helped function written by AI for testing
    private fun findDuplicatePsiElements(): Map<PsiElement, List<ComparisonData>> {
        val allItems = listOf(
            varsThatMatchDataCat,
            paramVarsThatMatchDataCat,
            fieldsThatMatchDataCat,
            methodsThatMatchDataCat,
            classesThatMatchDataCat
        ).flatten()

        return allItems
            .groupBy { it.psiElement }
            .filter { it.value.size > 1 }
    }

    //Function written by AI for Testing purposes
    private fun printDuplicateComparisonDataGrouped() {
        val allItems: List<Pair<String, ComparisonData>> = listOf(
            "varsThatMatchDataCat" to varsThatMatchDataCat,
            "paramVarsThatMatchDataCat" to paramVarsThatMatchDataCat,
            "fieldsThatMatchDataCat" to fieldsThatMatchDataCat,
            "methodsThatMatchDataCat" to methodsThatMatchDataCat,
            "classesThatMatchDataCat" to classesThatMatchDataCat
        ).flatMap { (sourceName, list) ->
            list.map { sourceName to it }
        }

        val duplicateGroups: List<List<Pair<String, ComparisonData>>> =
            allItems
                .groupBy { it.second.psiElement }
                .values
                .filter { it.size > 1 }

        println("---- DUPLICATE PSI ELEMENTS ----")

        if (duplicateGroups.isEmpty()) {
            println("No duplicate entries")
            println()
            return
        }

        duplicateGroups.forEachIndexed { index, group ->
            val element = group.first().second.psiElement
            val line = getLineNumber(element)
            val className = getContainingClassName(element)

            println("Duplicate Group ${index + 1}")
            println("  Shared PsiElement text: ${element.text}")
            println("  -> Line: ${line ?: "unknown"}")
            println("  -> Class: ${className ?: "unknown"}")
            println("  Entries:")

            group.forEach { (sourceName, data) ->
                println("    [$sourceName] $data")
            }

            println()
        }
    }

    private fun printVarsThatMatchDataCatForTesting() {
        printComparisonDataListForTesting("varsThatMatchDataCat", varsThatMatchDataCat)
    }

    private fun printParamVarsThatMatchDataCatForTesting() {
        //printComparisonDataListForTesting("paramVarsThatMatchDataCat", paramVarsThatMatchDataCat)
        //concatenateComparisonDataListForTesting("paramVarsThatMatchDataCat", paramVarsThatMatchDataCat)
        printDuplicatePsiElements()
    }
    private fun printDuplicatePsiElements() {
        val duplicates = findDuplicatePsiElements()

        if (duplicates.isEmpty()) {
            println("No duplicate PsiElements found.")
            return
        }

        println("=== Duplicate PsiElements Found ===")

        duplicates.forEach { (psiElement, items) ->
            println("\nPsiElement: ${psiElement.text}")

            items.forEach { data ->
                println(
                    "  -> dataCatName=${data.dataCatName}, " +
                            "type=${data.typeOfDataCatVal}, " +
                            "row=${data.row}, column=${data.column}, " +
                            "psiType=${data.psiType}"
                )
            }
        }
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

    private fun printAllFieldVariablesInObjectForTesting() {
        printVarsThatMatchDataCatForTesting()
        printParamVarsThatMatchDataCatForTesting()
        //printDuplicateComparisonDataGrouped()
        printFieldsThatMatchDataCatForTesting()
        printMethodsThatMatchDataCatForTesting()
        printClassesThatMatchDataCatForTesting()
    }

    fun checkIfFieldVariableIsInfieldsThatMatchDataCat(psiField: PsiField): Boolean {
        for(fieldInGlobalVariable in fieldsThatMatchDataCat){
            if(fieldInGlobalVariable.psiElement.isEquivalentTo(psiField)){
                return true
            }
        }
        return false
    }

    fun getVarsThatMatchDataCat(): MutableList<ComparisonData> {
        return varsThatMatchDataCat
    }
    fun getParamVarsThatMatchDataCat(): MutableList<ComparisonData> {
        return paramVarsThatMatchDataCat
    }
    fun getFieldsThatMatchDataCat(): MutableList<ComparisonData> {
        return fieldsThatMatchDataCat
    }
    fun getMethodsThatMatchDataCat(): MutableList<ComparisonData>{
        return methodsThatMatchDataCat
    }
    fun getClassesThatMatchDataCat(): MutableList<ComparisonData>{
        return classesThatMatchDataCat
    }
}