package com.github.kevindunne2003.gdprcomplianceplugin

import com.github.kevindunne2003.gdprcomplianceplugin.startup.RunPluginTool
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class loadDataCatalogueTest {

    val pluginTool = RunPluginTool()

    @Test
    fun returnsEmptyListWhenRowHasNoCells() {
        val workbook = XSSFWorkbook()
        workbook.createSheet().createRow(0)

        val result = pluginTool.loadDataCatKeywords(workbook)

        assertTrue(result.isEmpty())
    }

    @Test
    fun skipsNullCellsInRow() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()
        val row = sheet.createRow(0)

        row.createCell(0).setCellValue("Name")
        row.createCell(2).setCellValue("Email")

        val result = pluginTool.loadDataCatKeywords(workbook)

        assertEquals(listOf("Name", "Email"), result)
    }

    @Test
    fun `converts non string cells using toString`() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()
        val row = sheet.createRow(0)

        row.createCell(0).setCellValue("SurName")
        row.createCell(1).setCellValue(123.0)

        val result = pluginTool.loadDataCatKeywords(workbook)

        assertEquals(listOf("SurName", "123.0"), result)
    }

    @Test
    fun handlesMixedCellTypes() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("First Name")
        row0.createCell(1).setCellValue(12.0)
        row0.createCell(2).setCellValue(true)
        row0.createCell(3).setCellValue("DateOfBirth dd/mm/yyyy")

        val result = pluginTool.loadDataCatKeywords(wb)

        assertEquals(listOf("First Name", "12.0", "TRUE", "DateOfBirth dd/mm/yyyy"), result)
    }

    @Test
    fun ignoresCellsBeyondPhysicalCellCount() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("A")
        row0.createCell(5).setCellValue("F")

        val result = pluginTool.loadDataCatKeywords(wb)

        assertEquals(listOf("A"), result)
    }

    @Test
    fun skipsNullCellsWithinPhysicalCellRange() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("A")
        row0.createCell(2).setCellValue("C")

        val result = pluginTool.loadDataCatKeywords(wb)

        assertEquals(listOf("A"), result)
    }

}