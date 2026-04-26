package com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis

import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ComparisonData
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ViolationData
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiArrayAccessExpression
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiInstanceOfExpression
import com.intellij.psi.PsiLambdaExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiParenthesizedExpression
import com.intellij.psi.PsiPostfixExpression
import com.intellij.psi.PsiPrefixExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiSuperExpression
import com.intellij.psi.PsiThisExpression
import com.intellij.psi.PsiTypeCastExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil

object TraversePSITree {

    private var originalPsiParameterComparisonData: ComparisonData? = null
    private var methodsThatMatchDataCatPassingDataToParameter = mutableListOf<ComparisonData>()
    private var generatedViolationData = mutableListOf<ViolationData>()

    fun locateAllPossibleViolationsForAParameterVariable(psiParameter: PsiParameter, paramComparisonData: ComparisonData): MutableList<ViolationData> {
        methodsThatMatchDataCatPassingDataToParameter.clear()

        originalPsiParameterComparisonData = paramComparisonData
        traverseTreeToFindPossibleInfractions(psiParameter)

        generateViolationDataFromFieldVariables()
        return generatedViolationData

    }

    private fun generateViolationDataFromFieldVariables() {
        for (methodCompData in methodsThatMatchDataCatPassingDataToParameter) {
            val originalParamData = originalPsiParameterComparisonData
            if (originalParamData != null) {
                val violationData = CatalogueMatchFilter.generateViolationData(originalParamData, methodCompData)
                if(!generatedViolationData.contains(violationData)){
                    generatedViolationData.add(violationData)
                }
            }
        }
    }

    fun getGeneratedViolationData(): MutableList<ViolationData> {
        return generatedViolationData
    }

    private fun traverseTreeToFindPossibleInfractions(psiParameter: PsiParameter) {
        val psiMethod = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod::class.java)
        val methodInPsiMethodNameMatchesMethodsThatMatchDataCat =
            findComparisonDataForMatchedMethod(psiMethod)

        if (methodInPsiMethodNameMatchesMethodsThatMatchDataCat != null) {
            methodsThatMatchDataCatPassingDataToParameter.add(methodInPsiMethodNameMatchesMethodsThatMatchDataCat)
        }
        if (checkIfPsiMethodHasAnyParameters(psiMethod)) {
            //Parameters are sent to this method, so may need to continue checks.
            traverseTree(psiParameter)
        } else {
            //No parameters are passed to this method, so no need to continue checks
            return
        }
    }

    private fun traverseTree(psiParameter: PsiParameter) {
        val paramIndex = getParameterIndexFromPsiParameterVariable(psiParameter)
        val calleePsiMethod = getContainingMethodOfParameter(psiParameter)

        if (calleePsiMethod != null) {
            val methodUsages: MutableList<PsiMethod> = getUsagesOfPsiMethod(calleePsiMethod)

            for (methodUsage in methodUsages) {
                //This holds a list of all the calls to the callee method from the caller method
                val methodCallExpressions = getMethodCallExpressionsThatMatchPsiMethodName(methodUsage, calleePsiMethod)

                //We must check all of these calls
                for (methodCallExpression in methodCallExpressions) {
                    //Double check this cast, not sure it is correct
                    val argument = getArgumentAtIndex(methodCallExpression, paramIndex) as? PsiExpression ?: continue
                    //val variable = resolveArgumentExpressionToSingleVariable(argument)
                     processArgumentExpression(argument)

                    /*if (variable != null && argument != null) {
                        val assignedVariable =
                            findLastAssignedValueBeforeUsage(variable, argument)
                        if (assignedVariable != null) {
                            handleAssignedExpression(assignedVariable)
                        }
                    }*/
                }
            }

        }
    }

    /*This functions processes a PsiExpression to find what type of values are inside this expression
        This expression should come from the argument passed to a method we are looking at in the caller method
     */

    private fun processArgumentExpression(argument: PsiExpression) {
        when (val expr = unwrapExpression(argument)) {
            is PsiMethodCallExpression -> {
                checkMethodCall(expr)
                //Do we just check to see if this method is already in our list?
                //Not completely sure if this completes the correct check here
            }

            is PsiReferenceExpression -> {
                when (val resolved = expr.resolve()) {
                    is PsiField -> {
                        //dealWithFieldVariable(resolved)
                        if (CatalogueMatcher.checkIfFieldVariableIsInfieldsThatMatchDataCat(resolved)) {
                            //If we have a match for a field variable here, we dont have a match to the method.
                            //This means there is no Processing activity
                        }
                    }

                    is PsiVariable -> {
                        val assigned = findLastAssignedValueBeforeUsage(resolved, expr)
                        if (assigned != null) {
                            handleAssignedExpression(assigned)
                        } /*else {
                            resolved.initializer?.let { handleAssignedExpression(it) }
                        }*/
                    }
                }
            }

            //Means it is a collection of multiple variables or something else
            //Currently don't do anything if it is not a collection of variables
            /*else -> {
                val variables = collectVariablesFromArgumentExpression(expr)

                if (variables.isEmpty()) {
                    // If it is not reducible to variables, decide whether to stop
                    return
                }

                for (variable in variables) {
                    when (variable) {
                        is PsiField -> {
                            //dealWithFieldVariable(variable)
                            if (CatalogueMatcher.checkIfFieldVariableIsInfieldsThatMatchDataCat(variable)) {
                                //Match to the catalogue, so log this
                            }
                        }

                        else -> {
                            val assigned = findLastAssignedValueBeforeUsage(variable, argument)
                            if (assigned != null) {
                                handleAssignedExpression(assigned)
                            } /*else {
                                variable.initializer?.let { handleAssignedExpression(it) }
                            }*/
                        }
                    }
                }
            }
            */
        }
    }


    private fun handleAssignedExpression(expression: PsiExpression) {
        //Checks hardcoded variable, forget this
        if (expression is PsiLiteralExpression) {
            return
        }

        //Checks method calls
        if (expression is PsiMethodCallExpression) {
            checkMethodCall(expression)
            return
        }

        //Checks reference to something like a field / parameter / local variable
        if (expression is PsiReferenceExpression) {
            val resolved = expression.resolve()

            if (resolved is PsiField) {
                if (CatalogueMatcher.checkIfFieldVariableIsInfieldsThatMatchDataCat(resolved)) {
                    //Should we log this?
                }
                return
            }

            if (resolved is PsiParameter) {
                //Recursively check this methods calls
                traverseTreeToFindPossibleInfractions(resolved)
                return
            }

            if (resolved is PsiLocalVariable) {
                val assigned = findLastAssignedValueBeforeUsage(resolved, expression)

                if (assigned != null) {
                    handleAssignedExpression(assigned)
                } /*else {
                    resolved.initializer?.let { handleAssignedExpression(it) }
                }*/
                return
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

    private fun getArgumentAtIndex(
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

    //Returns a PsiVariable if the argument comes from a single variable
    //If the argument comes from a method call then it returns null
    private fun resolveArgumentExpressionToSingleVariable(argumentExpression: PsiElement?): PsiVariable? {
        val referenceExpression = argumentExpression as? PsiReferenceExpression ?: return null
        val resolvedElement = referenceExpression.resolve()
        return resolvedElement as? PsiVariable
    }


    //Function written mostly by a llm
    private fun collectVariablesFromArgumentExpression(expression: PsiExpression?): Set<PsiVariable> {
        if (expression == null) return emptySet()

        return when (val e = unwrapExpression(expression)) {
            is PsiReferenceExpression -> {
                val resolved = e.resolve()
                setOfNotNull(resolved as? PsiVariable)
            }

            is PsiMethodCallExpression -> {
                buildSet {
                    // variables in qualifier: obj.foo(x) -> collect from obj
                    val qualifier = e.methodExpression.qualifierExpression
                    addAll(collectVariablesFromArgumentExpression(qualifier))

                    // variables in arguments
                    for (arg in e.argumentList.expressions) {
                        addAll(collectVariablesFromArgumentExpression(arg))
                    }
                }
            }

            is PsiBinaryExpression -> {
                collectVariablesFromArgumentExpression(e.lOperand) +
                        collectVariablesFromArgumentExpression(e.rOperand)
            }

            is PsiConditionalExpression -> {
                collectVariablesFromArgumentExpression(e.condition) +
                        collectVariablesFromArgumentExpression(e.thenExpression) +
                        collectVariablesFromArgumentExpression(e.elseExpression)
            }

            is PsiAssignmentExpression -> {
                collectVariablesFromArgumentExpression(e.lExpression) +
                        collectVariablesFromArgumentExpression(e.rExpression)
            }

            is PsiArrayAccessExpression -> {
                collectVariablesFromArgumentExpression(e.arrayExpression) +
                        collectVariablesFromArgumentExpression(e.indexExpression)
            }

            is PsiNewExpression -> {
                buildSet {
                    val qualifier = e.qualifier
                    if (qualifier is PsiExpression) {
                        addAll(collectVariablesFromArgumentExpression(qualifier))
                    }
                    val arrayDimensions = e.arrayDimensions
                    for (dim in arrayDimensions) {
                        addAll(collectVariablesFromArgumentExpression(dim))
                    }
                    val argList = e.argumentList
                    if (argList != null) {
                        for (arg in argList.expressions) {
                            addAll(collectVariablesFromArgumentExpression(arg))
                        }
                    }
                }
            }

            is PsiTypeCastExpression -> {
                collectVariablesFromArgumentExpression(e.operand)
            }

            is PsiParenthesizedExpression -> {
                collectVariablesFromArgumentExpression(e.expression)
            }

            is PsiPrefixExpression -> {
                collectVariablesFromArgumentExpression(e.operand)
            }

            is PsiPostfixExpression -> {
                collectVariablesFromArgumentExpression(e.operand)
            }

            is PsiInstanceOfExpression -> {
                collectVariablesFromArgumentExpression(e.operand)
            }

            is PsiLiteralExpression -> emptySet()

            is PsiThisExpression -> emptySet()
            is PsiSuperExpression -> emptySet()

            is PsiLambdaExpression -> emptySet()
            is PsiClassObjectAccessExpression -> emptySet()

            else -> emptySet()
        }
    }

    private fun unwrapExpression(expression: PsiExpression): PsiExpression {
        var current = expression
        while (true) {
            current = when (current) {
                is PsiParenthesizedExpression -> current.expression ?: return current
                is PsiTypeCastExpression -> current.operand ?: return current
                else -> return current
            }
        }
    }

    private fun findComparisonDataForMatchedMethod(psiMethod: PsiMethod?): ComparisonData? {
        for (methodThatMatchesDataCat in CatalogueMatcher.getMethodsThatMatchDataCat()) {
            val psiMethodThatMatchesDataCat = methodThatMatchesDataCat.psiElement as PsiMethod
            if (CatalogueMatchFilter.areSameMethod(psiMethodThatMatchesDataCat, psiMethod)) {
                return methodThatMatchesDataCat
            }
        }
        return null
    }

    //Finds the last value assigned to a variable and returns it as a PsiExpression
    private fun findLastAssignedValueBeforeUsage(psiVariable: PsiVariable, usagePlace: PsiElement): PsiExpression? {
        val scope = PsiTreeUtil.getParentOfType(
            usagePlace,
            PsiMethod::class.java,
            PsiLambdaExpression::class.java,
            PsiClassInitializer::class.java
        ) ?: return psiVariable.initializer

        val usageOffset = usagePlace.textRange.startOffset
        var latestAssignment: PsiExpression? = null
        var latestOffset = -1

        //Handles initialiser in declaration
        psiVariable.initializer?.let { initializer ->
            val initOffset = initializer.textRange.startOffset
            if (initOffset < usageOffset && initOffset > latestOffset) {
                latestAssignment = initializer
                latestOffset = initOffset
            }
        }

        // Walk the local scope and find assignments to this variable:
        scope.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitAssignmentExpression(expression: PsiAssignmentExpression) {
                super.visitAssignmentExpression(expression)

                val lExpression = expression.lExpression
                val resolved = (lExpression as? PsiReferenceExpression)?.resolve()

                if (resolved == psiVariable) {
                    val offset = expression.textRange.startOffset
                    if (offset < usageOffset && offset > latestOffset) {
                        latestAssignment = expression.rExpression
                        latestOffset = offset
                    }
                }
            }
        })

        return latestAssignment
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

    private fun resolveVariablePassedAsParameterToCalleeMethod(
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

    private fun getParameterIndexFromPsiParameterVariable(psiParameter: PsiParameter): Int {
        val method = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod::class.java) as PsiMethod
        return method.parameterList.parameters.indexOf(psiParameter)
    }

    private fun getContainingMethodOfParameter(parameter: PsiParameter): PsiMethod? {
        return PsiTreeUtil.getParentOfType(parameter, PsiMethod::class.java)
    }

    private fun checkMethodCall(methodCallExpression: PsiMethodCallExpression) {
        val calledMethod = methodCallExpression.resolveMethod() ?: return
        val methodInPsiMethodNameMatchesMethodsThatMatchDataCat =
            findComparisonDataForMatchedMethod(calledMethod)

        if (methodInPsiMethodNameMatchesMethodsThatMatchDataCat != null) {
            methodsThatMatchDataCatPassingDataToParameter.add(methodInPsiMethodNameMatchesMethodsThatMatchDataCat)
        }

    }


}