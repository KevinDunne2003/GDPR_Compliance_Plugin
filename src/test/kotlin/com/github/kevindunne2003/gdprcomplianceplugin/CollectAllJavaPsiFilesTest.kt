package com.github.kevindunne2003.gdprcomplianceplugin
import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngine.collectAllJavPsiFiles
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

// A Large number of these test cases were written using an llm
class CollectAllJavPsiFilesTest : BasePlatformTestCase() {

    fun test_collectsAllJavaFilesRecursively_andSkipsNonJava() {
        val root: VirtualFile = myFixture.tempDirFixture.findOrCreateDir("root")

        // Create files
        myFixture.tempDirFixture.createFile("root/A.java", "class A {}")
        myFixture.tempDirFixture.createFile("root/readme.md", "# hi")
        myFixture.tempDirFixture.createFile("root/src/B.java", "class B {}")
        myFixture.tempDirFixture.createFile("root/src/notes.txt", "nope")
        myFixture.tempDirFixture.createFile("root/src/deeper/C.java", "class C {}")
        myFixture.tempDirFixture.createFile("root/src/deeper/image.png", "not really a png")

        val found: List<PsiJavaFile> = collectAllJavPsiFiles(project, root) ?: emptyList()

        // 1) Ensure no missing Java files (exact match)
        val expectedPaths = setOf(
            myFixture.tempDirFixture.getFile("root/A.java")!!.path,
            myFixture.tempDirFixture.getFile("root/src/B.java")!!.path,
            myFixture.tempDirFixture.getFile("root/src/deeper/C.java")!!.path
        )
        val foundPaths = found.map { it.virtualFile.path }.toSet()
        assertEquals(expectedPaths, foundPaths)

        // 2) Ensure no non-Java files are returned
        assertTrue(found.all { it.virtualFile.name.endsWith(".java", ignoreCase = true) })
    }

    fun test_doesNotFail_whenNonJavaFilesExist() {
        val root = myFixture.tempDirFixture.findOrCreateDir("root")

        myFixture.tempDirFixture.createFile("root/data.bin", "\u0000\u0001\u0002")
        myFixture.tempDirFixture.createFile("root/file.txt", "hello")
        myFixture.tempDirFixture.createFile("root/Ok.java", "class Ok {}")
        myFixture.tempDirFixture.createFile("root/nested/AlsoOk.java", "class AlsoOk {}")

        // If this throws, the test fails automatically.
        val found = collectAllJavPsiFiles(project, root) ?: emptyList()
        val names = found.map { it.virtualFile.name }.toSet()

        assertEquals(setOf("Ok.java", "AlsoOk.java"), names)
    }

    fun test_supportsCaseInsensitiveJavaExtension() {
        val root = myFixture.tempDirFixture.findOrCreateDir("root")
        myFixture.tempDirFixture.createFile("root/Upper.JAVA", "class Upper {}")

        val found = collectAllJavPsiFiles(project, root) ?: emptyList()

        assertEquals(1, found.size)
        assertEquals("Upper.JAVA", found.single().virtualFile.name)
    }
}