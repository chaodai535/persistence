package com.github.dean535.files

import com.github.dean535.files.filter.RowFilter
import com.github.dean535.files.parser.CellParser
import com.github.dean535.files.parser.FileParser
import com.google.common.collect.Lists
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.joor.Reflect
import java.io.File

object PoiImporter {
    fun loadAllSheets(file: File): List<Pair<String, List<List<String>>>> {
        val result = Lists.newArrayList<Pair<String, List<List<String>>>>()
        val wb: Workbook = WorkbookFactory.create(file)
        for (i in 0 until wb.numberOfSheets) {
            val sheetList = Lists.newArrayList<List<String>>()
            val sheet = wb.getSheetAt(i)
            val sheetName = sheet.sheetName
            for (rowIndex in 0..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex)
                val columns = Lists.newArrayList<String>()
                if (row == null)
                    continue
                val cellNum = row.lastCellNum.toInt()
                for (cellIndex in 0 until cellNum) {
                    var column = ""
                    val cell = row.getCell(cellIndex)
                    if (cell != null) {
                        when (cell.cellType) {
                            Cell.CELL_TYPE_NUMERIC -> column = cell.numericCellValue.toString()
                            Cell.CELL_TYPE_STRING -> column = cell.stringCellValue
                            Cell.CELL_TYPE_BOOLEAN -> column = cell.booleanCellValue.toString() + ""
                            Cell.CELL_TYPE_FORMULA -> column = cell.cellFormula
                            Cell.CELL_TYPE_BLANK -> column = ""
                        }
                    }
                    columns.add(column.trim())
                }
                sheetList.add(columns)
            }
            val filteredList = sheetList.filter { it.any { cell -> cell.isNotEmpty() } }
            result.add(Pair(sheetName, filteredList))
        }
        return result
    }

    private fun loadFile(file: File, fileParser: FileParser): List<List<List<String>>> {
        val result = Lists.newArrayList<List<List<String>>>()
        val wb: Workbook = WorkbookFactory.create(file)
        for (i in 0 until wb.numberOfSheets) {
            var start = fileParser.start
            var end = fileParser.end
            if (fileParser.sheetNo > 0 && i != fileParser.sheetNo) {
                result.add(Lists.newArrayList())
                continue
            }
            val sheet = wb.getSheetAt(i)
            val sheetList = Lists.newArrayList<List<String>>()
            val rows = sheet.lastRowNum
            if (start <= sheet.firstRowNum) {
                start = sheet.firstRowNum
            }
            if (end >= rows) {
                end = rows
            } else if (end <= 0) {
                end += rows
            }
            for (rowIndex in start..end) {
                val row = sheet.getRow(rowIndex)
                val columns = Lists.newArrayList<String>()
                if (row == null)
                    continue
                val cellNum = row.lastCellNum.toInt()
                for (cellIndex in 0 until cellNum) {
                    var column = ""
                    val cell = row.getCell(cellIndex)
                    if (cell != null) {
                        val cellType = cell.cellType
                        when (cellType) {
                            Cell.CELL_TYPE_NUMERIC -> column = cell.numericCellValue.toString()
                            Cell.CELL_TYPE_STRING -> column = cell.stringCellValue
                            Cell.CELL_TYPE_BOOLEAN -> column = cell.booleanCellValue.toString() + ""
                            Cell.CELL_TYPE_FORMULA -> column = cell.cellFormula
                            Cell.CELL_TYPE_BLANK -> column = " "
                        }
                    }

                    columns.add(column.trim())
                }

                val rowFilterFlagList = Lists.newArrayList<Boolean>()
                val rowFilterList = Lists.newArrayList<RowFilter>()
                for (k in rowFilterList.indices) {
                    val rowFilter = rowFilterList[k]
                    rowFilterFlagList.add(rowFilter.doFilter(rowIndex, columns))
                }
                sheetList.add(columns)
            }
            val filteredList = sheetList.filter { it.isNotEmpty() }
            result.add(filteredList)
        }
        return result
    }

    fun loadSheet(file: File, fileParser: FileParser): List<List<String>> {
        return loadFile(file, fileParser)[fileParser.sheetNo]
    }


    fun <T> processSheet(file: File, fileParser: FileParser, clazz: Class<*>): List<T> {
        val srcList = loadSheet(file, fileParser)
        // log.debug(" result  {}", srcList)
        val results = Lists.newArrayList<T>()
        for (i in srcList.indices) {
            val list = srcList[i]
            results.add(fillModel(clazz, list, fileParser, i) as T)
        }
        return results
    }


    fun fillModel(clazz: Class<*>, list: List<String>, fileParser: FileParser, rowIndex: Int): Any {
        val model = Reflect.on(clazz).create().get<Any>()
        if (fileParser.preRowProcessor != null) {
            fileParser.preRowProcessor!!.exec(model, list, rowIndex)
        }
        var message = ""
        for (i in list.indices) {
            val value = list[i]
            val cell = matchCell(fileParser, i) ?: continue
            val name = cell.attribute
            val cellValidate = cell.validate
            var valid = true
            if (cellValidate != null) {
                valid = cellValidate.validate(value)
                if (!valid) {
                    message =
                        message + "value(" + value + ") is invalid in row " + (rowIndex + 1) + " column " + cell.index + "\n"
                }
            }
            if (valid) {
                var convertedValue: Any = value
                val cellConvertor = cell.convert
                if (cellConvertor != null) {
                    convertedValue = cellConvertor.convert(value, model)
                }
                val obj = convertedValue
                name!!.split(",")
                    .forEach { Reflect.on(model).set(it, obj) }
            }
        }
        if (fileParser.postRowProcessor != null) {
            fileParser.postRowProcessor!!.exec(model, list, rowIndex)
        }

        if (StringUtils.isNotEmpty(message)) {
            throw RuntimeException(message)
        }
        return model
    }

    private fun matchCell(fileParser: FileParser, index: Int): CellParser? {
        val cells = fileParser.cellParsers
        for (i in cells.indices) {
            val cell = cells[i]
            if (index + 1 == cell.index) return cell
        }
        return null
    }
}
