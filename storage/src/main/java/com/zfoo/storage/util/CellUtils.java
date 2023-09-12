/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.storage.util;

import com.zfoo.protocol.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;

/**
 * @author godotg
 */
public abstract class CellUtils {

    public static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        return getCellValue(cell).toString().trim();
    }

    /**
     * 获取单元格值
     *
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        return getCellValue(cell, cell.getCellType());
    }

    /**
     * 获取单元格值<br>
     * 如果单元格值为数字格式，则判断其格式中是否有小数部分，无则返回Long类型，否则返回Double类型
     */
    private static Object getCellValue(Cell cell, CellType cellType) {
        Object value;
        switch (cellType) {
            case NUMERIC:
                value = getNumericValue(cell);
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case FORMULA:
                // 遇到公式时查找公式结果类型
                value = getCellValue(cell, cell.getCachedFormulaResultType());
                break;
            case BLANK:
                value = StringUtils.EMPTY;
                break;
            case ERROR:
                final FormulaError error = FormulaError.forInt(cell.getErrorCellValue());
                value = (null == error) ? StringUtils.EMPTY : error.getString();
                break;
            default:
                value = cell.getStringCellValue();
        }

        return value;
    }


    // -------------------------------------------------------------------------------------------------------------- Private method start

    /**
     * 获取数字类型的单元格值
     *
     * @return 单元格值，可能为Long、Double、Date
     */
    private static Object getNumericValue(Cell cell) {
        var value = cell.getNumericCellValue();

        var style = cell.getCellStyle();
        if (null == style) {
            return value;
        }

        // 判断是否为日期
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }

        var format = style.getDataFormatString();
        // 普通数字
        if (null != format && !format.contains(StringUtils.PERIOD)) {
            var longValue = (long) value;
            if (longValue == value) {
                return Long.valueOf(longValue);
            }
        }
        return value;
    }

}
