package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ComparisonData
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ViolationPrincipleMatch
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.Priority
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ViolationReportExporter {

    fun exportViolationMatch() {
        val outputFile = File("C:\\Year 4\\Final Year Project\\Results\\gdpr-violation-matches.xlsx")

        exportViolationMatches(
            variableViolations = ViolationPrincipleMapper.getVariableViolationsMappedToPrinciples(),
            fieldViolations = ViolationPrincipleMapper.getFieldViolationsMappedToPrinciples(),
            parameterViolations = ViolationPrincipleMapper.getParamViolationsMappedToPrinciples(),
            outputFile = outputFile
        )

        println("Saved Excel report to: ${outputFile.absolutePath}")
    }

    private fun exportViolationMatches(
        variableViolations: MutableList<ViolationPrincipleMatch>,
        fieldViolations: MutableList<ViolationPrincipleMatch>,
        parameterViolations: MutableList<ViolationPrincipleMatch>,
        outputFile: File
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GDPR Violations")

        val titleStyle = createSectionTitleStyle(workbook)
        val headerStyle = createHeaderStyle(workbook)

        var rowIndex = 0

        rowIndex = writeSection(
            sheet = sheet,
            startRow = rowIndex,
            sectionTitle = "Variable Violations",
            matches = variableViolations,
            titleStyle = titleStyle,
            headerStyle = headerStyle
        )

        rowIndex += 1

        rowIndex = writeSection(
            sheet = sheet,
            startRow = rowIndex,
            sectionTitle = "Field Variable Violations",
            matches = fieldViolations,
            titleStyle = titleStyle,
            headerStyle = headerStyle
        )

        rowIndex += 1

        rowIndex = writeSection(
            sheet = sheet,
            startRow = rowIndex,
            sectionTitle = "Parameter Variable Violations",
            matches = parameterViolations,
            titleStyle = titleStyle,
            headerStyle = headerStyle
        )

        for (i in 0..14) {
            sheet.autoSizeColumn(i)
        }

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { out ->
            workbook.write(out)
        }
        workbook.close()
    }

    private fun writeSection(
        sheet: Sheet,
        startRow: Int,
        sectionTitle: String,
        matches: List<ViolationPrincipleMatch>,
        titleStyle: CellStyle,
        headerStyle: CellStyle
    ): Int {
        var rowIndex = startRow

        val titleRow = sheet.createRow(rowIndex++)
        titleRow.createCell(0).setCellValue(sectionTitle)
        titleRow.getCell(0).cellStyle = titleStyle
        titleRow.createCell(8).setCellValue("Containing Method")
        titleRow.getCell(8).cellStyle = titleStyle

        val headerRow = sheet.createRow(rowIndex++)
        val headers = listOf(
            "Violation 1",
            "Violation 2",

            "DataCatName",
            "Priority",
            "TypeOfDataCatVal",
            "Column",
            "Row",
            "PsiIdentifier",

            "DataCatName",
            "Priority",
            "TypeOfDataCatVal",
            "Column",
            "Row",
            "PsiIdentifier",
            "Containing Class"
        )

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        if (matches.isEmpty()) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("No entries")
            return rowIndex
        }

        matches.forEach { match ->
            val row = sheet.createRow(rowIndex++)

            val principles = match.principles ?: mutableListOf()

            row.createCell(0).setCellValue(principles.getOrNull(0)?.name ?: "")
            row.createCell(1).setCellValue(principles.getOrNull(1)?.name ?: "")

            val varData = match.violationData.comparisonDataOfVar
            val methodData = match.violationData.comparisonDataOfMethod

            writeComparisonData(row, startColumn = 2, comparisonData = varData)
            writeMethodComparisonData(row, startColumn = 8, comparisonData = methodData)
        }

        return rowIndex
    }

    private fun writeComparisonData(
        row: Row,
        startColumn: Int,
        comparisonData: ComparisonData
    ) {
        row.createCell(startColumn).setCellValue(comparisonData.dataCatName)
        row.createCell(startColumn + 1).setCellValue(calculatePersonalDataPriority(comparisonData.row ?: -1).name)
        row.createCell(startColumn + 2).setCellValue(comparisonData.typeOfDataCatVal.name)
        row.createCell(startColumn + 3).setCellValue(comparisonData.column.toDouble())
        row.createCell(startColumn + 4).setCellValue(comparisonData.row?.toDouble() ?: 0.0)
        row.createCell(startColumn + 5).setCellValue(getPsiIdentifier(comparisonData.psiElement))
    }

    private fun writeMethodComparisonData(
        row: Row,
        startColumn: Int,
        comparisonData: ComparisonData
    ) {
        row.createCell(startColumn).setCellValue(comparisonData.dataCatName)
        row.createCell(startColumn + 1).setCellValue(calculateKeywordPriority(comparisonData.row ?: -1).name)
        row.createCell(startColumn + 2).setCellValue(comparisonData.typeOfDataCatVal.name)
        row.createCell(startColumn + 3).setCellValue(comparisonData.column.toDouble())
        row.createCell(startColumn + 4).setCellValue(comparisonData.row?.toDouble() ?: -1.0)
        row.createCell(startColumn + 5).setCellValue(getPsiIdentifier(comparisonData.psiElement))
        row.createCell(startColumn + 6).setCellValue(getContainingClassName(comparisonData.psiElement))
    }

    private fun calculatePersonalDataPriority(priorityValue: Int): Priority {
        return when (priorityValue) {
            1 -> Priority.High
            8 -> Priority.Low
            in 2..7 -> Priority.Medium
            else -> Priority.Medium
        }
    }

    private fun calculateKeywordPriority(priorityValue: Int): Priority {
        return when (priorityValue) {
            1, 7 -> Priority.Low
            2, 3, 5 -> Priority.Medium
            4, 6 -> Priority.High
            else -> Priority.Medium
        }
    }

    private fun getContainingClassName(element: PsiElement): String {
        val method = element as? PsiMethod ?: return ""
        val containingClass: PsiClass? = method.containingClass
        return containingClass?.qualifiedName
            ?: containingClass?.name
            ?: ""
    }

    private fun getPsiIdentifier(element: PsiElement): String {
        val nameIdentifierOwner = element as? PsiNameIdentifierOwner
        if (nameIdentifierOwner != null) {
            val identifier = nameIdentifierOwner.nameIdentifier?.text
            if (!identifier.isNullOrBlank()) {
                return identifier
            }
        }

        val namedElement = element as? PsiNamedElement
        if (namedElement?.name != null) {
            return namedElement.name!!
        }

        return element.text
            .replace("\n", " ")
            .replace("\r", " ")
            .take(200)
    }

    private fun createSectionTitleStyle(workbook: XSSFWorkbook): CellStyle {
        val font = workbook.createFont().apply {
            bold = true
            fontHeightInPoints = 14
        }

        return workbook.createCellStyle().apply {
            setFont(font)
            alignment = HorizontalAlignment.LEFT
        }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): CellStyle {
        val font = workbook.createFont().apply {
            bold = true
        }

        return workbook.createCellStyle().apply {
            setFont(font)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }
}