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
     * Get cell value
     *
     * @return value; possible types: Date, Double, Boolean, String
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        return getCellValue(cell, cell.getCellType());
    }

    /**
     * Get cell value<br>
     * For numeric cells: return Long if no fractional part, otherwise Double
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
                // For formula cells, check the result type
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
     * Get numeric cell value
     *
     * @return cell value; type may be Long, Double, or Date
     */
    private static Object getNumericValue(Cell cell) {
        var value = cell.getNumericCellValue();

        var style = cell.getCellStyle();
        if (null == style) {
            return value;
        }

        // Check if value is a date
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }

        var format = style.getDataFormatString();
        // Plain number
        if (null != format && !format.contains(StringUtils.PERIOD)) {
            var longValue = (long) value;
            if (longValue == value) {
                return Long.valueOf(longValue);
            }
        }
        return value;
    }

}
