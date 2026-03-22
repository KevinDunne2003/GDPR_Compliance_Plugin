package com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

object CodeScanningAST {
    private var variableNamesInsideProject = mutableListOf<String>()
    private var methodNamesInsideProject = mutableListOf<String>()
    private var classNamesInsideProject = mutableListOf<String>()

    fun getVariableNamesInsideProject(): MutableList<String> = variableNamesInsideProject

    fun getMethodNamesInsideProject(): MutableList<String> = methodNamesInsideProject

    fun getClassNamesInsideProject(): MutableList<String> = classNamesInsideProject

    fun iterateThroughOpenProjectsFileSystem() {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val rootVirtualFile = project.baseDir ?: return

        clearCaches()

        val javaFiles = collectAllJavaFiles(rootVirtualFile)

        for (file in javaFiles) {
            val compilationUnit = parseJavaFileToAst(file) ?: continue

            variableNamesInsideProject.addAll(getAllVariableNamesInAst(compilationUnit))
            methodNamesInsideProject.addAll(getAllMethodNamesInAst(compilationUnit))
            classNamesInsideProject.addAll(getAllClassNamesInAst(compilationUnit))
        }
    }

    //Small helper function to be run everytime the scan needs to be completed
    private fun clearCaches() {
        variableNamesInsideProject.clear()
        methodNamesInsideProject.clear()
        classNamesInsideProject.clear()
    }

    fun collectAllJavaFiles(rootVirtualFile: VirtualFile): MutableList<VirtualFile> {
        val javaFiles = mutableListOf<VirtualFile>()

        VfsUtilCore.iterateChildrenRecursively(rootVirtualFile, null) { vf ->
            if (!vf.isDirectory && vf.name.endsWith(".java", ignoreCase = true)) {
                javaFiles.add(vf)
            }
            true
        }

        return javaFiles
    }

    fun parseJavaFileToAst(file: VirtualFile): CompilationUnit? {
        return try {
            val text = String(file.contentsToByteArray(), StandardCharsets.UTF_8)
            StaticJavaParser.parse(text)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllClassNamesInAst(compilationUnit: CompilationUnit): MutableList<String> {
        val classes = mutableListOf<String>()

        compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { classDecl ->
            classes.add(classDecl.nameAsString)
        }

        return classes
    }

    fun getAllMethodNamesInAst(compilationUnit: CompilationUnit): MutableList<String> {
        val methods = mutableListOf<String>()

        compilationUnit.findAll(MethodDeclaration::class.java).forEach { methodDecl ->
            methods.add(methodDecl.nameAsString)
        }

        return methods
    }

    fun getAllVariableNamesInAst(compilationUnit: CompilationUnit): MutableList<String> {
        val variables = mutableListOf<String>()

        compilationUnit.findAll(FieldDeclaration::class.java).forEach { fieldDecl ->
            fieldDecl.variables.forEach { variable ->
                variables.add(variable.nameAsString)
            }
        }

        return variables
    }
}