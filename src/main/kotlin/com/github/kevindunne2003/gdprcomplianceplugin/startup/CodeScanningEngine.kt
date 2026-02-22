package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import kotlin.collections.mutableListOf

object CodeScanningEngine {

    private var variableNamesInsideProject = mutableListOf<String>()
    private var methodNamesInsideProject = mutableListOf<String>()
    private var classNamesInsideProject = mutableListOf<String>()

    fun getVariableNamesInsideProject(): MutableList<String> {
        return variableNamesInsideProject
    }

    fun getMethodNamesInsideProject(): MutableList<String> {
        return methodNamesInsideProject
    }

    fun getClassNamesInsideProject(): MutableList<String> {
        return classNamesInsideProject
    }

    fun iterateThroughOpenProjectsFileSystem() {
        var project = ProjectManager.getInstance().openProjects[0]
            val rootVirtualFile = project.baseDir ?: return

            var psiJavaFiles = collectAllJavPsiFiles(project,rootVirtualFile)

            //Insert functionality of accessing these psiFiles with the functions provided
            if(psiJavaFiles != null) {
                for(i in 0 until psiJavaFiles.size){
                    var variables = getAllVariableNamesInPSIClassObject(psiJavaFiles.get(i) as PsiClass)
                    variableNamesInsideProject.addAll(variables)

                    var methods = getAllMethodNamesInPSIClassObject(psiJavaFiles.get(i) as PsiClass)
                    methodNamesInsideProject.addAll(methods)

                    var classes = getAllClassNamesInPSIJavaFile(psiJavaFiles.get(i))
                    methodNamesInsideProject.addAll(methods)
                }
            }
    }

    fun collectAllJavPsiFiles(project: Project, rootVirtualFile: VirtualFile): MutableList<PsiJavaFile>? {
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

    fun getAllClassNamesInPSIJavaFile(psiJavaFile: PsiJavaFile): MutableList<String> {
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

    fun getAllMethodNamesInPSIClassObject(psiClass: PsiClass): MutableList<String> {
        val psiMethods = psiClass.methods
        var methods: MutableList<String> = mutableListOf()

        for (i in 0 until psiMethods.size) {
            methods.add(psiMethods.get(i).name)
        }
        return methods
    }

    fun getAllVariableNamesInPSIClassObject(psiClass: PsiClass): MutableList<String> {
        val psiFields = psiClass.fields
        var variables: MutableList<String> = mutableListOf()

        for (i in 0 until psiFields.size) {
            variables.add(psiFields.get(i).name)
        }
        return variables
    }
}