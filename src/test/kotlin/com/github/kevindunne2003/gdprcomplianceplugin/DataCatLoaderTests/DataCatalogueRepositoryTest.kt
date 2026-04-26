package com.github.kevindunne2003.gdprcomplianceplugin

import com.github.kevindunne2003.gdprcomplianceplugin.startup.DataCatalogueValues.DataCatalogueRepository
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// A Large number of these test cases were written using an llm
class DataCatalogueRepositoryTest {

    @Test
    fun `returns empty list when row has no cells in data keywords`() {
        val workbook = XSSFWorkbook()
        workbook.createSheet().createRow(0)

        val result = DataCatalogueRepository.loadDataCatalogueKeywords(workbook)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `converts non string cells using toString in data keywords`() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()
        val row = sheet.createRow(0)

        row.createCell(0).setCellValue("SurName")
        row.createCell(1).setCellValue(123.0)

        val result = DataCatalogueRepository.loadDataCatalogueKeywords(workbook)

        assertEquals(listOf("SurName", "123.0"), result)
    }

    @Test
    fun `skips empty cells but still reads later cells in data keywords`() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("A")
        // Intentionally leave cell(1) empty / not created
        row0.createCell(2).setCellValue("C")

        val result = DataCatalogueRepository.loadDataCatalogueKeywords(wb)

        assertEquals(listOf("A", "C"), result)
    }

    @Test
    fun `handles mixed cell types in data keywords`() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("First Name")
        row0.createCell(1).setCellValue(12.0)
        row0.createCell(2).setCellValue(true)
        row0.createCell(3).setCellValue("DateOfBirth dd/mm/yyyy")

        val result = DataCatalogueRepository.loadDataCatalogueKeywords(wb)

        assertEquals(listOf("First Name", "12.0", "TRUE", "DateOfBirth dd/mm/yyyy"), result)
    }

    @Test
    fun `skips null cells within range in data keywords`() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("A")
        row0.createCell(2).setCellValue("C")

        val result = DataCatalogueRepository.loadDataCatalogueKeywords(wb)

        assertEquals(listOf("A","C"), result)
    }

    @Test
    fun `returns empty list when row has no cells in related terms`() {
        val workbook = XSSFWorkbook()
        workbook.createSheet().createRow(0)
        workbook.createSheet().createRow(1)

        val result = DataCatalogueRepository.loadDataCatalogueRelatedTerms(workbook)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `converts non string cells using toString in related terms`() {
        val workbook = XSSFWorkbook()
        workbook.createSheet()
        val sheet = workbook.createSheet()
        val row = sheet.createRow(0)

        row.createCell(0).setCellValue("FirstName")
        row.createCell(1).setCellValue(156.0)

        val result = DataCatalogueRepository.loadDataCatalogueRelatedTerms(workbook)

        assertEquals(listOf(listOf("FirstName", "156.0")), result)
    }

    @Test
    fun `handles mixed cell types in related terms`() {
        val wb = XSSFWorkbook()
        wb.createSheet()
        val sheet = wb.createSheet()
        val row0 = sheet.createRow(0)

        row0.createCell(0).setCellValue("First Name")
        row0.createCell(1).setCellValue(12.0)
        row0.createCell(2).setCellValue(true)
        row0.createCell(3).setCellValue("DateOfBirth dd/mm/yyyy")

        val result = DataCatalogueRepository.loadDataCatalogueRelatedTerms(wb)

        assertEquals(listOf(listOf("First Name", "12.0", "TRUE", "DateOfBirth dd/mm/yyyy")), result)
    }
}