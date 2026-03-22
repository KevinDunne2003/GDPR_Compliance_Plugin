package com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.util.PsiTreeUtil

object PSICodeScanningEngine {

    private var fieldsInsideProject = mutableListOf<PsiField>()
    private var localVariablesInsideProject = mutableListOf<PsiLocalVariable>()
    private var paramVariablesInsideProject = mutableListOf<PsiParameter>()

    private var methodsInsideProject = mutableListOf<PsiMethod>()
    private var classesInsideProject = mutableListOf<PsiClass>()

    fun getFieldsInProject(): MutableList<PsiField> {
        return fieldsInsideProject
    }

    fun getLocalVariablesInProject(): MutableList<PsiLocalVariable> {
        return localVariablesInsideProject
    }

    fun getParamVariablesInProject(): MutableList<PsiParameter> {
        return paramVariablesInsideProject
    }

    fun getMethodsInsideProject(): MutableList<PsiMethod> {
        return methodsInsideProject
    }

    fun getClassesInProject(): MutableList<PsiClass> {
        return classesInsideProject
    }

    fun parseOpenProjectsFileSystem() {
        var project = ProjectManager.getInstance().openProjects[0]
        val rootVirtualFile = project.baseDir ?: return

        var psiJavaFiles = collectAllJavPsiFiles(project, rootVirtualFile)

        if (psiJavaFiles != null) {
            for (i in 0 until psiJavaFiles.size) {
                var methods = getAllMethodInPSIClassObject(psiJavaFiles.get(i) as PsiClass)
                methodsInsideProject.addAll(methods)

                var variables = getAllVariableInPSIClassObject(psiJavaFiles.get(i) as PsiClass)
                fieldsInsideProject.addAll(variables)

                var localVariables = getAllLocalVariablesFromMethodFieldInMethodsInsideProjectClassVariable()
                localVariablesInsideProject.addAll(localVariables)

                var parameterVariables = getAllParameterVariablesFromMethodFieldInMethodsInsideProjectClassVariable()
                paramVariablesInsideProject.addAll(parameterVariables)

                var classes = getAllClassInPSIJavaFile(psiJavaFiles.get(i))
                classesInsideProject.addAll(classes)
            }
        }
    }

    private fun collectAllJavPsiFiles(project: Project, rootVirtualFile: VirtualFile): MutableList<PsiJavaFile>? {
        var psiFiles = mutableListOf<PsiJavaFile>()

        //lambda function with boolean return value so true must be included to continue the iterations
        //This will visit all the child nodes inside the file tree but skip directories and none java files

        VfsUtilCore.iterateChildrenRecursively(rootVirtualFile, null) { vf ->
            if (!vf.isDirectory && vf.name.endsWith(".java", ignoreCase = true)) {
                val psiFile = PsiManager.getInstance(project).findFile(vf) ?: return@iterateChildrenRecursively true
                psiFiles.add(psiFile as PsiJavaFile)
            }
            true
        }
        return psiFiles
    }

    private fun getAllClassInPSIJavaFile(psiJavaFile: PsiJavaFile): MutableList<PsiClass> {
        val psiClasses = psiJavaFile.classes
        var classes: MutableList<PsiClass> = mutableListOf()

        for (i in 0 until psiClasses.size) {
            if (psiClasses.get(i) != null) {
                val classFromJavaFile: PsiClass = psiClasses.get(i)

                classes.add(classFromJavaFile)
            }
        }

        return classes
    }

    private fun getAllMethodInPSIClassObject(psiClass: PsiClass): MutableList<PsiMethod> {
        val psiMethods = psiClass.methods
        var methods: MutableList<PsiMethod> = mutableListOf()

        for (i in 0 until psiMethods.size) {
            methods.add(psiMethods.get(i))
        }
        return methods
    }

    private fun getAllVariableInPSIClassObject(psiClass: PsiClass): MutableList<PsiField> {
        val psiFields = psiClass.fields
        var variables: MutableList<PsiField> = mutableListOf()


        for (i in 0 until psiFields.size) {
            variables.add(psiFields.get(i))
        }
        return variables
    }

    private fun getAllVariableInPSIMethodObject(psiMethod: PsiMethod): MutableList<PsiLocalVariable> {
        val psiLocalVariables = PsiTreeUtil.findChildrenOfType(psiMethod, PsiLocalVariable::class.java)
        var variables: MutableList<PsiLocalVariable> = mutableListOf()

        variables.addAll(psiLocalVariables)

        return variables
    }

    private fun getAllLocalVariablesFromMethodFieldInMethodsInsideProjectClassVariable(): MutableList<PsiLocalVariable> {
        var variables: MutableList<PsiLocalVariable> = mutableListOf()

        for (i in 0 until methodsInsideProject.size) {
            val psiLocalVariables =
                PsiTreeUtil.findChildrenOfType(methodsInsideProject.get(i), PsiLocalVariable::class.java)
            variables.addAll(psiLocalVariables)
        }
        return variables
    }

    private fun getAllParameterVariablesFromMethodFieldInMethodsInsideProjectClassVariable(): MutableList<PsiParameter> {
        var paramVariables: MutableList<PsiParameter> = mutableListOf()
        for (method in methodsInsideProject) {
            paramVariables.addAll(method.parameterList.parameters)
        }
        return paramVariables
    }

    private fun getAllVariableNamesInPSIMethodObject(psiMethod: PsiMethod): MutableList<String> {
        val psiLocalVariables = PsiTreeUtil.findChildrenOfType(psiMethod, PsiLocalVariable::class.java)
        var variables: MutableList<String> = mutableListOf()


        for (local in psiLocalVariables) {
            variables.add(local.name)
        }
        return variables
    }

    private fun getAllClassNamesInPSIJavaFile(psiJavaFile: PsiJavaFile): MutableList<String> {
        val psiClasses = psiJavaFile.classes
        var classes: MutableList<String> = mutableListOf()

        for (i in 0 until psiClasses.size) {
            if (psiClasses.get(i).name != null) {
                val name: String? = psiClasses.get(i).name

                if (name != null) {
                    classes.add(name)
                }
            }
        }
        return classes
    }

    private fun getAllMethodNamesInPSIClassObject(psiClass: PsiClass): MutableList<String> {
        val psiMethods = psiClass.methods
        var methods: MutableList<String> = mutableListOf()

        for (i in 0 until psiMethods.size) {
            methods.add(psiMethods.get(i).name)
        }
        return methods
    }

    private fun getAllVariableNamesInPSIClassObject(psiClass: PsiClass): MutableList<String> {
        val psiFields = psiClass.fields
        var variables: MutableList<String> = mutableListOf()


        for (i in 0 until psiFields.size) {
            variables.add(psiFields.get(i).name)
        }
        return variables
    }
}