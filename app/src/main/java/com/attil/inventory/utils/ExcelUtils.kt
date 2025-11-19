package com.attil.inventory.utils

import android.content.Context
import android.net.Uri
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelUtils {

    /**
     * Read Excel file and return list of rows as maps
     */
    fun readExcelFile(
        context: Context,
        uri: Uri,
        startRow: Int = 1 // Skip header row by default
    ): List<Map<String, String>> {
        val results = mutableListOf<Map<String, String>>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Get header row
            val headerRow = sheet.getRow(0)
            val headers = headerRow.cellIterator().asSequence().map {
                getCellValueAsString(it)
            }.toList()

            // Read data rows
            for (rowIndex in startRow until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex) ?: continue
                val rowData = mutableMapOf<String, String>()

                headers.forEachIndexed { colIndex, header ->
                    val cell = row.getCell(colIndex)
                    rowData[header] = getCellValueAsString(cell)
                }

                results.add(rowData)
            }

            workbook.close()
        }

        return results
    }

    /**
     * Get cell value as string regardless of cell type
     */
    private fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    dateFormat.format(cell.dateCellValue)
                } else {
                    // Handle both integers and decimals
                    val numValue = cell.numericCellValue
                    if (numValue == numValue.toLong().toDouble()) {
                        numValue.toLong().toString()
                    } else {
                        numValue.toString()
                    }
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue.toString()
                } catch (e: Exception) {
                    try {
                        cell.stringCellValue
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }

    /**
     * Create Excel file with provided data
     */
    fun createExcelFile(
        context: Context,
        outputStream: OutputStream,
        sheetName: String,
        headers: List<String>,
        data: List<List<Any>>
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(sheetName)

        // Create header row with styling
        val headerRow = sheet.createRow(0)
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont()
            font.bold = true
            font.color = IndexedColors.WHITE.index
            setFont(font)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorderBottom(BorderStyle.THIN)
            setBorderTop(BorderStyle.THIN)
            setBorderLeft(BorderStyle.THIN)
            setBorderRight(BorderStyle.THIN)
        }

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Create data rows
        val dataStyle = workbook.createCellStyle().apply {
            setBorderBottom(BorderStyle.THIN)
            setBorderTop(BorderStyle.THIN)
            setBorderLeft(BorderStyle.THIN)
            setBorderRight(BorderStyle.THIN)
        }

        data.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)
            rowData.forEachIndexed { colIndex, cellValue ->
                val cell = row.createCell(colIndex)
                when (cellValue) {
                    is Number -> cell.setCellValue(cellValue.toDouble())
                    is Boolean -> cell.setCellValue(cellValue)
                    else -> cell.setCellValue(cellValue.toString())
                }
                cell.cellStyle = dataStyle
            }
        }

        // Auto-size columns
        headers.indices.forEach { colIndex ->
            sheet.autoSizeColumn(colIndex)
        }

        workbook.write(outputStream)
        workbook.close()
    }

    /**
     * Validate Excel structure matches expected template
     */
    fun validateExcelStructure(
        context: Context,
        uri: Uri,
        expectedHeaders: List<String>
    ): ValidationResult {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                if (sheet.physicalNumberOfRows == 0) {
                    return ValidationResult(false, "Excel file is empty")
                }

                val headerRow = sheet.getRow(0)
                val actualHeaders = headerRow.cellIterator().asSequence().map {
                    getCellValueAsString(it).trim()
                }.toList()

                // Check if all expected headers are present
                val missingHeaders = expectedHeaders.filter { expected ->
                    actualHeaders.none { it.equals(expected, ignoreCase = true) }
                }

                if (missingHeaders.isNotEmpty()) {
                    return ValidationResult(
                        false,
                        "Missing columns: ${missingHeaders.joinToString(", ")}"
                    )
                }

                workbook.close()
                return ValidationResult(true, "Excel structure is valid")
            }

            return ValidationResult(false, "Could not open Excel file")
        } catch (e: Exception) {
            return ValidationResult(false, "Error reading Excel file: ${e.message}")
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}
