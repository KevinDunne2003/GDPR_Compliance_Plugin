package com.github.kevindunne2003.gdprcomplianceplugin.startup.DataCatalogueValues

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException

object DataCatalogueRepository {

    private var dataCatalogueKeywords = mutableListOf<String>()

    //Outer list references the row
    //Inner list references the column
    private var dataCatalogueRelatedTerms = mutableListOf<MutableList<String>>()
    private var dataCataloguePersonalData = mutableListOf<MutableList<String>>()

    fun getDataCatalogueKeywords(): MutableList<String> {
        return dataCatalogueKeywords
    }

    fun getDataCatalogueRelatedTerms(): MutableList<MutableList<String>> {
        return dataCatalogueRelatedTerms
    }

    fun getDataCataloguePersonalData(): MutableList<MutableList<String>> {
        return dataCataloguePersonalData
    }

    //Uses @NlsSafe for the classpath
    fun getDataCataloguePathFromUser(project: Project): @NlsSafe String? {
        val input = Messages.showInputDialog(project, "Enter class path of Data Catalogue", "Run Plugin", null)

        if (input.isNullOrEmpty()) return null
        else return input
    }

    //Can return an empty list if the data inside is invalid
    fun loadDataCatalogueKeywords(workbook: XSSFWorkbook): MutableList<String> {
        val entries = mutableListOf<String>()

        try {
            val sheet = workbook.getSheetAt(0)

            if (sheet.getRow(0) != null && sheet.getRow(0).lastCellNum.toInt() > 0) {
                for (j in 0..sheet.lastRowNum) {
                    if (sheet.getRow(j).getCell(0) != null) {
                        entries.add(sheet.getRow(j).getCell(0).toString())
                    }
                }
            } else {
                print("Row 0 is empty")
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            print("No sheet detected")
        }

        return entries
    }

    //Can return an empty list if the data inside is invalid
    fun loadDataCatalogueRelatedTerms(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
        val entries = mutableListOf<MutableList<String>>()

        try {
            val sheet = workbook.getSheetAt(1)

            for (i in 0..sheet.lastRowNum) {
                val row = mutableListOf<String>()

                if (sheet.getRow(i) != null && sheet.getRow(i).lastCellNum.toInt() > 0) {

                    //If the user skips a cell on a column it is allowed
                    for (j in 0..sheet.getRow(i).lastCellNum) {
                        if (sheet.getRow(i).getCell(j) != null) {
                            row.add(sheet.getRow(i).getCell(j).toString())
                        }
                    }

                    entries.add(row)
                }
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            print("No sheet detected")
        }
        return entries
    }

    fun loadDataCataloguePersonalData(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
        val entries = mutableListOf<MutableList<String>>()
        try {
            val sheet = workbook.getSheetAt(2)

            for (i in 0..sheet.lastRowNum) {
                val row = mutableListOf<String>()

                if (sheet.getRow(i) != null && sheet.getRow(i).lastCellNum.toInt() > 0) {

                    for (j in 0 until sheet.getRow(i).lastCellNum) {
                        if (sheet.getRow(i).getCell(j) != null) {
                            row.add(sheet.getRow(i).getCell(j).toString())
                        }
                    }
                }
                entries.add(row)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            print("Error with sheet 2")
        }
        return entries
    }

    //Uses @NlsSafe for the classpath
    fun loadDataCatalogue(dataCataloguePath: @NlsSafe String?) {
        try {
            FileInputStream("$dataCataloguePath/DataCatalogue.xlsx").use { fis ->
                val workbook = XSSFWorkbook(fis)

                dataCatalogueKeywords = loadDataCatalogueKeywords(workbook)
                dataCatalogueRelatedTerms = loadDataCatalogueRelatedTerms(workbook)
                dataCataloguePersonalData = loadDataCataloguePersonalData(workbook)

            }
        } catch (e: IOException) {
            println("Invalid Path")
            e.printStackTrace()
        }
    }

    fun run(project: Project) {
        val dataCatalogueClassPath = getDataCataloguePathFromUser(project)

        loadDataCatalogue(dataCatalogueClassPath)

        //printDataCatKeywordsForTesting()
        //printDataCatRelatedTermsForTesting()
        //printDataCatPersonalDataForTesting()

    }

    private fun printDataCatKeywordsForTesting() {
        println("=== Data Catalogue Keywords ===")
        dataCatalogueKeywords.forEachIndexed { index, keyword ->
            println("[$index] $keyword")
        }
    }

    fun printDataCatRelatedTermsForTesting() {
        println("=== Data Catalogue Related Terms ===")
        dataCatalogueRelatedTerms.forEachIndexed { rowIndex, row ->
            println("Row $rowIndex: ${row.joinToString(", ")}")
        }
    }

    fun printDataCatPersonalDataForTesting() {
        println("=== Data Catalogue Personal Data ===")
        dataCataloguePersonalData.forEachIndexed { rowIndex, row ->
            println("Row $rowIndex: ${row.joinToString(", ")}")
        }
    }
}