package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import java.io.IOException
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

object DataCatalogueLoader {

    private var dataCatKeywords = mutableListOf<String>()
    private var dataCatRelatedTerms = mutableListOf<MutableList<String>>()
    private var dataCatPersonalData = mutableListOf<MutableList<String>>()

    fun getDataCatKeywords(): MutableList<String> {
        return dataCatKeywords
    }

    fun getDataCatRelatedTerms(): MutableList<MutableList<String>> {
        return dataCatRelatedTerms
    }

    fun getDataCatPersonalData(): MutableList<MutableList<String>> {
        return dataCatPersonalData
    }

    //Uses @NlsSafe for the classpath
    fun getDataCataloguePath(project: Project): @NlsSafe String? {
        val input = Messages.showInputDialog(project, "Enter class path of Data Catalogue", "Run Plugin", null)

        if (input.isNullOrEmpty()) return null
        else return input
    }

    //Can return an empty list if the data inside is invalid
    fun loadDataCatKeywords(workbook: XSSFWorkbook): MutableList<String> {
        val entries = mutableListOf<String>()

        try {
            val sheet = workbook.getSheetAt(0)

            if (sheet.getRow(0) != null && sheet.getRow(0).lastCellNum.toInt() > 0) {
                for (j in 0..sheet.getRow(0).lastCellNum) {
                    if (sheet.getRow(0).getCell(j) != null) {
                        entries.add(sheet.getRow(0).getCell(j).toString())
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
    fun loadDataCatRelatedTerms(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
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

    fun loadDataCatPersonalData(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
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
    fun loadDataCatalogue(dataCataloguePath: @NlsSafe String?): XSSFRow? {
        try {
            FileInputStream("$dataCataloguePath/DataCatalogue.xlsx").use { fis ->
                val workbook = XSSFWorkbook(fis)

                dataCatKeywords = loadDataCatKeywords(workbook)
                dataCatRelatedTerms = loadDataCatRelatedTerms(workbook)
                dataCatPersonalData = loadDataCatPersonalData(workbook)

            }
        } catch (e: IOException) {
            println("Invalid Path")
            e.printStackTrace()
        }
        return null;
    }

    fun run(project: Project) {
        val dataCatalogueClassPath = getDataCataloguePath(project)

        var dataCatalogue1 = loadDataCatalogue(dataCatalogueClassPath)

    }
}