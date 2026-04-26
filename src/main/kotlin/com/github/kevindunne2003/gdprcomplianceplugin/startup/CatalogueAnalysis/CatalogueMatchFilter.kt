package com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis

import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ComparisonData
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ViolationData
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiReturnStatement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil

object CatalogueMatchFilter {
    private var filteredLocalVariablesAndMethodsThatContainViolations = mutableListOf<ViolationData>()
    private var filteredFieldVariablesAndMethodsThatContainViolations = mutableListOf<ViolationData>()
    private var filteredParamVariablesAndMethodsThatContainViolations = mutableListOf<ViolationData>()

    fun generateCatalogeMatchFilteredValues() {
        clearAllFieldVariables()
        findAllVarsAndTheirMethodsThatMatchDataCat()
        findAllFieldVariablesAndTheMethodsTheirAreReferencedThatMatchDataCat()
        findAllParamVariablesAndTheMethodsTheirAreReferencedThatMatchDataCat()

        printFieldVariablesForTesting()
    }

    private fun clearAllFieldVariables() {
        filteredLocalVariablesAndMethodsThatContainViolations.clear()
        filteredFieldVariablesAndMethodsThatContainViolations.clear()
        filteredParamVariablesAndMethodsThatContainViolations.clear()
    }

    private fun findAllVarsAndTheirMethodsThatMatchDataCat() {
        filteredLocalVariablesAndMethodsThatContainViolations.clear()
        for (localVariableComparisonData in CatalogueMatcher.getVarsThatMatchDataCat()) {
            for (methodComparisonData in CatalogueMatcher.getMethodsThatMatchDataCat()) {

                val methodThatContainsLocalVariable =
                    getPsiMethodObjectFromPsiLocalVariable(localVariableComparisonData.psiElement as PsiLocalVariable)
                if (areSameMethod(methodComparisonData.psiElement as PsiMethod, methodThatContainsLocalVariable)) {

                    val violationData = ViolationData(localVariableComparisonData,
                        methodComparisonData)
                    if(!mutableListAlreadyContainsViolationData(filteredLocalVariablesAndMethodsThatContainViolations, violationData)){
                        filteredLocalVariablesAndMethodsThatContainViolations.add(violationData)
                    }
                }
            }
        }
    }

    private fun findAllFieldVariablesAndTheMethodsTheirAreReferencedThatMatchDataCat() {
        filteredFieldVariablesAndMethodsThatContainViolations.clear()

        for (fieldVariableComparisonData in CatalogueMatcher.getFieldsThatMatchDataCat()) {
            val fieldReferences = findAllReferencesOfPSIField(fieldVariableComparisonData.psiElement as PsiField)
            //Not sure if .distinct() would work in this case, so I created a helper function to use the areSameMethod function
            val containingMethodsOfFields =
                excludeDuplicateMethods(findAllContainingMethodsOfFieldReferences(fieldReferences))

            for (methodComparisonData in CatalogueMatcher.getMethodsThatMatchDataCat()) {
                for (method in containingMethodsOfFields) {
                    if (areSameMethod(methodComparisonData.psiElement as PsiMethod, method)) {
                        val violationData = ViolationData(fieldVariableComparisonData,methodComparisonData)
                        if(!mutableListAlreadyContainsViolationData(filteredFieldVariablesAndMethodsThatContainViolations, violationData)){
                            filteredFieldVariablesAndMethodsThatContainViolations.add(violationData)
                        }

                        break
                    }
                }
            }
        }
    }

    private fun findAllParamVariablesAndTheMethodsTheirAreReferencedThatMatchDataCat() {
        filteredParamVariablesAndMethodsThatContainViolations.clear()
        for (paramVariable in CatalogueMatcher.getParamVarsThatMatchDataCat()) {
            var violationDataList =
                TraversePSITree.locateAllPossibleViolationsForAParameterVariable(paramVariable.psiElement as PsiParameter, paramVariable)
            for (violationData in violationDataList) {
                if(!mutableListAlreadyContainsViolationData(filteredParamVariablesAndMethodsThatContainViolations, violationData)){
                    filteredParamVariablesAndMethodsThatContainViolations.add(violationData)
                }
            }
        }

    }

    private fun getPsiMethodObjectFromPsiLocalVariable(psiVariable: PsiLocalVariable): PsiMethod? {
        return PsiTreeUtil.getParentOfType(psiVariable, PsiMethod::class.java)
    }

    //Helper function to compare 2 PsiMethod objects
    //Uses PsiManager and .getElementsEquivalent to see if both methods reference the same method in code
    fun areSameMethod(method1: PsiMethod, method2: PsiMethod?): Boolean {
        return PsiManager.getInstance(method1.project).areElementsEquivalent(method1, method2)
    }

    private fun excludeDuplicateMethods(methods: MutableList<PsiMethod>): MutableList<PsiMethod> {
        val result = mutableListOf<PsiMethod>()

        for (method in methods) {
            val alreadyExists = result.any { existing ->
                areSameMethod(existing, method)
            }
            if (!alreadyExists) {
                result.add(method)
            }
        }
        return result
    }

    fun generateViolationData(
        varComparisonData: ComparisonData,
        methodComparisonData: ComparisonData
    ): ViolationData {
        return ViolationData(varComparisonData, methodComparisonData)
    }

    private fun findAllContainingMethodsOfFieldReferences(fieldReferences: MutableList<PsiReference>): MutableList<PsiMethod> {
        var containingMethods: MutableList<PsiMethod> = mutableListOf()

        for (fieldReferences in fieldReferences) {
            val containingMethod = getContainingMethodOfPsiReference(fieldReferences)
            if (containingMethod != null) {
                containingMethods.add(containingMethod)
            }
        }
        return containingMethods
    }

    private fun findAllReferencesOfPSIField(psiField: PsiField): MutableList<PsiReference> {
        return ReferencesSearch.search(psiField)
            .findAll().toMutableList()
    }

    private fun getContainingMethodOfPsiReference(psiReference: PsiReference): PsiMethod? {
        return PsiTreeUtil.getParentOfType(psiReference.element, PsiMethod::class.java)
    }

    private fun getPsiReferenceExpressionFromPsiReference(psiReference: PsiReference): PsiReferenceExpression? {
        val expressionElement = psiReference.element
        if (expressionElement is PsiReferenceExpression) {
            return expressionElement
        }
        return null
    }


    private fun isPsiReferenceExpressionUsedForWriting(psiReferenceExpression: PsiReferenceExpression): Boolean {
        return PsiUtil.isAccessedForWriting(psiReferenceExpression)
    }

    private fun isPsiReferenceExpressionUsedForReading(psiReferenceExpression: PsiReferenceExpression): Boolean {
        return PsiUtil.isAccessedForReading(psiReferenceExpression)
    }

    private fun isPsiReferenceExpressionPassedAsAMethodArgument(psiReferenceExpression: PsiReferenceExpression): Boolean {
        if (psiReferenceExpression.parent is PsiExpressionList) {
            return true
        } else {
            return false
        }
    }

    private fun isPsiReferenceExpressionUsedForLocalVariableInitialisation(psiReferenceExpression: PsiReferenceExpression): Boolean {
        val localVariable = PsiTreeUtil.getParentOfType(psiReferenceExpression, PsiLocalVariable::class.java)
        val initializer = localVariable?.initializer
        return initializer != null && PsiTreeUtil.isAncestor(initializer, psiReferenceExpression, true)
    }

    private fun isPsiReferenceExpressionUsedAsReturnStatement(psiReferenceExpression: PsiReferenceExpression): Boolean {
        val returnStatementElement = PsiTreeUtil.getParentOfType(psiReferenceExpression, PsiReturnStatement::class.java)
        val returnValue = returnStatementElement?.returnValue
        return returnValue != null && PsiTreeUtil.isAncestor(returnValue, returnStatementElement, false)
    }

    fun getFilteredLocalVariablesAndMethodsThatContainViolations(): MutableList<ViolationData> {
        return filteredLocalVariablesAndMethodsThatContainViolations
    }

    fun getFilteredFieldVariablesAndMethodsThatContainViolations(): MutableList<ViolationData> {
        return filteredFieldVariablesAndMethodsThatContainViolations
    }
    fun getFilteredParamVariablesAndMethodsThatContainViolations(): MutableList<ViolationData> {
        return filteredParamVariablesAndMethodsThatContainViolations
    }

    //Print statements were written using LLMs
    private fun printFieldVariablesForTesting() {
        printFieldVarsForTesting()
        printVariableForTesting()
        printParametersForTesting()
    }

    private fun printFieldVarsForTesting() {
        println("=== Field Variable Violations ===")
        println("Total number of entries: " + filteredFieldVariablesAndMethodsThatContainViolations.size)
        /*filteredFieldVariablesAndMethodsThatContainViolations.forEach {
            printViolationData(it)
        }*/

    }

    private fun printVariableForTesting() {
        println("=== Local Variable Violations ===")
        println("Total number of entries: " + filteredLocalVariablesAndMethodsThatContainViolations.size)
        /*filteredLocalVariablesAndMethodsThatContainViolations.forEach {
            printViolationData(it)
        }*/
    }

    private fun printParametersForTesting() {
        println("=== Parameter Variable Violations ===")
        println("Total number of entries: " + filteredParamVariablesAndMethodsThatContainViolations.size)
        /*filteredParamVariablesAndMethodsThatContainViolations.forEach {
            printViolationData(it)
        }*/
    }

    private fun printViolationData(violation: ViolationData) {
        val varData = violation.comparisonDataOfVar
        val methodData = violation.comparisonDataOfMethod

        println("---- Violation ----")

        println("Variable:")
        println("  Name: ${varData.dataCatName}")
        println("  Type: ${varData.typeOfDataCatVal}")
        println("  Position: row=${varData.row}, column=${varData.column}")
        println("  PSI Element: ${varData.psiElement}")
        println("  PSI Type: ${varData.psiType}")

        println("Method:")
        println("  Name: ${methodData.dataCatName}")
        println("  Type: ${methodData.typeOfDataCatVal}")
        println("  Position: row=${methodData.row}, column=${methodData.column}")
        println("  PSI Element: ${methodData.psiElement}")
        println("  PSI Type: ${methodData.psiType}")

        println("-------------------")
    }

    private fun mutableListAlreadyContainsViolationData(
        mutableList: MutableList<ViolationData>,
        violationData: ViolationData
    ): Boolean {
        return mutableList.any {
            it.comparisonDataOfVar == violationData.comparisonDataOfVar &&
                    it.comparisonDataOfMethod == violationData.comparisonDataOfMethod
        }
    }
}
