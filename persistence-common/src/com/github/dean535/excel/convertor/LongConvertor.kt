package com.github.dean535.excel.convertor

import com.github.dean535.files.convert.CellConverter


class LongConvertor : CellConverter{
    override fun convert(value: String, obj: Any): Any {
        if (value.isEmpty()) {
            return 0L
        }
        return value.toLong()
    }
}
