package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil

class TraversePSITree {

    fun traverseTreeToFindPossibleInfractions(psiParameter: PsiParameter) {
        val psiMethod = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod::class.java)
        if (checkIfPsiMethodNameMatchesTheDataCatalogue(psiMethod)) {
            //Log possible infraction
        }
        if (checkIfPsiMethodHasAnyParameters(psiMethod)) {
            //Parameters are sent to this method, so may need to continue checks.
        } else {
            //No parameters are passed to this method, so no need to continue checks
        }
    }

    private fun findVarInCallingMethodWhereParameterWasSent(
        callerPsiMethod: PsiMethod,
        calleePsiMethod: PsiMethod,
        psiParameter: PsiParameter
    ) {
        var methodCalls = PsiTreeUtil.getChildrenOfType(callerPsiMethod, PsiMethodCallExpression::class.java)
        //var variable: psiLiteralExpression

        if (methodCalls != null) {
            for (methodCall in methodCalls) {
                for (literalExpression in methodCall.argumentList.expressions) {
                    if (literalExpression.text == psiParameter.name) {

                    }
                }
            }
        }
    }

    private fun getUsagesOfPsiMethod(psiMethod: PsiMethod): MutableList<PsiMethod> {
        var methods: MutableList<PsiMethod> = mutableListOf()

        for (reference in ReferencesSearch.search(psiMethod)) {
            val element = reference.element
            val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)

            //If methods calls are made outside of a method they should not be captured here
            if (method != null && method !in methods) {
                methods.add(method)
            }
        }

        return methods
    }

    private fun getMethodCallExpressionsThatMatchPsiMethodName(
        callerPsiMethod: PsiMethod,
        calleePsiMethod: PsiMethod
    ): MutableList<PsiMethodCallExpression> {
        var methodCallExpressionsThatCallCalleeMethod: MutableList<PsiMethodCallExpression> = mutableListOf()

        for (methodCalls in PsiTreeUtil.findChildrenOfType(callerPsiMethod, PsiMethodCallExpression::class.java)) {
            //Checks to see if the method call is to the calleeMethod
            if (methodCalls.resolveMethod() == calleePsiMethod) {
                methodCallExpressionsThatCallCalleeMethod.add(methodCalls)
            }
        }
        return methodCallExpressionsThatCallCalleeMethod
    }

    private fun resolveVaribalePassedAsParameterToCalleeMethod(
        methodCallExpression: PsiMethodCallExpression,
        parameterIndex: Int
    ): PsiElement? {
        var arguments = methodCallExpression.argumentList.expressions

        var variablePassed = arguments.getOrNull(parameterIndex)
        if (variablePassed != null) {
            return variablePassed
        } else {
            return null
        }
    }

    /*private fun findVariableInMethodFromPsiElement(psiElement: PsiElement, psiMethod: PsiMethod): PsiVariable? {
        var psiVariables = psiMethod.
    }*/


    private fun checkIfPsiMethodNameMatchesTheDataCatalogue(psiMethod: PsiMethod?): Boolean {
        if (psiMethod == null) {
            return false
        }
        //Implement some check for the method. Should be done in CatalogueMatcher.kt but the methods currently return ComparisonData type
        //Could checks already found methods? instead of checking these methods again
        return false
    }

    private fun checkIfPsiMethodHasAnyParameters(psiMethod: PsiMethod?): Boolean {
        if (psiMethod == null) {
            return false
        }
        if (psiMethod.parameterList.parametersCount == 0) {
            return false
        } else {
            return true
        }
    }

    private fun getParameterIndexFromPsiParameterVariable(psiParameter: PsiParameter): Int {
        val method = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod::class.java) as PsiMethod
        return method.parameterList.parameters.indexOf(psiParameter)
    }
}