package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.jetbrains.rd.util.catch
import okio.IOException
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

class RunPluginTool: AnAction("Run Plugin") {

    override fun actionPerformed(e: AnActionEvent){
        val project = e.project ?: return

        loadDataCatalogue(project)
    }

    //Uses @NlsSafe for the classpath
    fun getDataCataloguePath(project: Project): @NlsSafe String? {
        val input = Messages.showInputDialog(project,"Enter class path of Data Catalogue", "Run Plugin", null)

        if (input.isNullOrEmpty()) return null
        else return input
    }

    fun loadDataCatKeywords(workbook: XSSFWorkbook): MutableList<String> {
        val sheet = workbook.getSheetAt(0)
        val entries = mutableListOf<String>()

        for (j in 0 until sheet.getRow(0).physicalNumberOfCells) {
            if(sheet.getRow(0).getCell(j) != null) {
                entries.add(sheet.getRow(0).getCell(j).toString())
            }
        }

        return entries
    }

    fun loadDataCatSynonyms(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
        val sheet = workbook.getSheetAt(1)
        val entries = mutableListOf<MutableList<String>>()

        for(i in 0 until sheet.physicalNumberOfRows){
            val row = mutableListOf<String>()

            for (j in 0 until sheet.getRow(i).physicalNumberOfCells) {
                if (sheet.getRow(i).getCell(j) != null) {
                    row.add(sheet.getRow(i).getCell(j).toString())
                }
            }

            entries.add(row)
        }
        return entries
    }

    fun loadDataCaPersonalData(workbook: XSSFWorkbook): MutableList<MutableList<String>> {
        val sheet = workbook.getSheetAt(2)
        val entries = mutableListOf<MutableList<String>>()

        for(i in 0 until sheet.physicalNumberOfRows){
            val row = mutableListOf<String>()

            for (j in 0 until sheet.getRow(i).physicalNumberOfCells) {
                if (sheet.getRow(i).getCell(j) != null) {
                    row.add(sheet.getRow(i).getCell(j).toString())
                }
            }

            entries.add(row)
        }
        return entries
    }

    //Uses @NlsSafe for the classpath
    fun loadKeywordDataCatalogue(dataCataloguePath: @NlsSafe String?): XSSFRow? {
        try {
            FileInputStream("$dataCataloguePath/DataCatalogue.xlsx").use { fis ->
                val workbook = XSSFWorkbook(fis)

                val dataCatKeywords = loadDataCatKeywords(workbook)
                val dataCatRelatedTerms = loadDataCatSynonyms(workbook)
                val dataCatPersonalData = loadDataCaPersonalData(workbook)

            }
        }
            catch (e: IOException) {
                println("Invalid Path")
                e.printStackTrace()
            }
        return null;
    }

    fun loadDataCatalogue(project: Project) {
        val dataCatalogueClassPath = getDataCataloguePath(project)

        while(dataCatalogueClassPath == null) {
            getDataCataloguePath(project)
        }

        var dataCatalogue1 = loadKeywordDataCatalogue(dataCatalogueClassPath)

        if(dataCatalogue1 != null){

        }else{
            println("Incorrect format or no values found in : $dataCatalogueClassPath")
        }
    }
}