package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.CodeScanningEngine
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.collections.emptyList

// A Large number of these test cases were written using an llm
class CodeScanningEnginePsiTest : BasePlatformTestCase() {

    fun test_getAllClassNamesInPSIJavaFile_returnsTopLevelClassNames() {
        val psiFile = myFixture.configureByText(
            "Example.java",
            """
            public class Foo {
                class Inner {}
            }
            class Bar {}
            """.trimIndent()
        )

        val javaFile: PsiJavaFile = psiFile as? PsiJavaFile
            ?: error("Fixture did not produce a PsiJavaFile; got: ${psiFile::class.java.name}")

        val names: List<String> = CodeScanningEngine.getAllClassNamesInPSIJavaFile(javaFile)

        // PsiJavaFile.classes returns top-level classes only (Foo, Bar)
        assertEquals(listOf("Foo", "Bar"), names)
    }

    fun test_getAllMethodNamesInPSIClassObject_returnsDeclaredMethodsIncludingConstructor() {
        val javaFile = myFixture.configureByText(
            "Methods.java",
            """
            public class Foo {
                public Foo() {}
                public void doThing() {}
                private int calc() { return 1; }
            }
            """.trimIndent()
        ) as PsiJavaFile

        val fooClass: PsiClass = javaFile.classes.first { it.name == "Foo" }

        val methodNames: List<String> = CodeScanningEngine.getAllMethodNamesInPSIClassObject(fooClass)

        // constructors are included; constructor name == class name
        assertEquals(listOf("Foo", "doThing", "calc"), methodNames)
    }

    fun test_getAllVariableNamesInPSIClassObject_returnsFieldNames() {
        val javaFile = myFixture.configureByText(
            "Fields.java",
            """
            public class Foo {
                private int a;
                public String b;
            }
            """.trimIndent()
        ) as PsiJavaFile

        val fooClass: PsiClass = javaFile.classes.first { it.name == "Foo" }

        val fieldNames: List<String> = CodeScanningEngine.getAllVariableNamesInPSIClassObject(fooClass)

        assertEquals(listOf("a", "b"), fieldNames)
    }
    fun test_getAllClassNamesInPSIJavaFile_returnsEmpty_whenNoTopLevelClasses() {
        val psiFile = myFixture.configureByText(
            "Empty.java",
            """
            // Just comments and whitespace
            /* no classes here */
            """.trimIndent()
        )

        val javaFile = psiFile as PsiJavaFile
        assertNotNull(javaFile)

        val names = CodeScanningEngine.getAllClassNamesInPSIJavaFile(javaFile)

        assertEquals(listOf<String>(), names)
    }

    fun test_getAllMethodNamesInPSIClassObject_returnsEmpty_whenNoMethodsOrConstructors() {
        val javaFile = myFixture.configureByText(
            "NoMethods.java",
            """
            public class Foo {
                // no fields
                // no methods
            }
            """.trimIndent()
        ) as PsiJavaFile

        val foo: PsiClass = javaFile.classes.first { it.name == "Foo" }

        // In Java, a class with no explicit constructors has an *implicit* default constructor,
        // but PSI typically does NOT list implicit constructors in psiClass.methods.
        val methods = CodeScanningEngine.getAllMethodNamesInPSIClassObject(foo)

        assertEquals(listOf<String>(), methods)
    }

    fun test_getAllVariableNamesInPSIClassObject_returnsEmpty_whenNoFields() {
        val javaFile = myFixture.configureByText(
            "NoFields.java",
            """
            public class Foo {
                public void doThing() {}
            }
            """.trimIndent()
        ) as PsiJavaFile

        val foo: PsiClass = javaFile.classes.first { it.name == "Foo" }

        val fields = CodeScanningEngine.getAllVariableNamesInPSIClassObject(foo)

        assertEquals(listOf<String>(), fields)
    }

    fun test_getAllClassNamesInPSIJavaFile_ignoresAnonymousAndLocalClasses() {
        val javaFile = myFixture.configureByText(
            "LocalAnonymous.java",
            """
            public class Foo {
                public void m() {
                    Runnable r = new Runnable() { public void run() {} };
                    class Local { void x() {} }
                }
            }
            """.trimIndent()
        ) as PsiJavaFile

        // PsiJavaFile.classes should contain only *top-level* classes; Foo is top-level.
        val names = CodeScanningEngine.getAllClassNamesInPSIJavaFile(javaFile)

        assertEquals(listOf("Foo"), names)
    }

    fun test_getAllMethodNamesInPSIClassObject_includesConstructors_whenExplicit() {
        val javaFile = myFixture.configureByText(
            "Ctor.java",
            """
            public class Foo {
                public Foo() {}
                public Foo(int x) {}
            }
            """.trimIndent()
        ) as PsiJavaFile

        val foo = javaFile.classes.first { it.name == "Foo" }

        val methods = CodeScanningEngine.getAllMethodNamesInPSIClassObject(foo)

        // constructors appear as methods with name "Foo"
        assertEquals(listOf("Foo", "Foo"), methods)
    }

    fun test_getAllVariableNamesInPSIClassObject_includesConstantsAndMultipleDeclarations() {
        val javaFile = myFixture.configureByText(
            "MultiFields.java",
            """
            public class Foo {
                private int a, b;
                public static final String EMAIL = "x";
            }
            """.trimIndent()
        ) as PsiJavaFile

        val foo = javaFile.classes.first { it.name == "Foo" }

        val vars = CodeScanningEngine.getAllVariableNamesInPSIClassObject(foo)

        // `fields` splits `int a, b;` into two PsiField entries (a and b)
        assertEquals(listOf("a", "b", "EMAIL"), vars)
    }
}
