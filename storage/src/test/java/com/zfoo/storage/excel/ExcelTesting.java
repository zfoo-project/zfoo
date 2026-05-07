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

package com.zfoo.storage.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author godotg
 */
@Ignore
public class ExcelTesting {

    @Test
    public void createExcelTest() throws IOException {
        //Step 1: create workbook
        var wb = WorkbookFactory.create(true);

        //Step 2: create sheet
        var sheet = wb.createSheet("TestSheet");

        // Step 3: create header row (row 0)
        var row = sheet.createRow(0);
        var style = wb.createCellStyle();
        //style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  //center alignment


        //Step 4: create cells
        var cell = row.createCell(0); //first cell
        cell.setCellValue("Name");
        cell.setCellStyle(style);

        cell = row.createCell(1);         //second cell
        cell.setCellValue("Age");
        cell.setCellStyle(style);


        //Step 5: insert data
        for (int i = 0; i < 5; i++) {
            //Create row
            row = sheet.createRow(i + 1);
            //Create cells and add data
            row.createCell(0).setCellValue("aa" + i);
            row.createCell(1).setCellValue(i);
        }

        //Step 6: save Excel to the specified path
        FileOutputStream target = new FileOutputStream("target.xlsx");
        wb.write(target);
        target.close();

        System.out.println("Excel file generated successfully...");
    }

    @Test
    public void readExcelTest() throws IOException {
        Workbook wb = WorkbookFactory.create(new File("target.xlsx"));

        // Read only the first worksheet
        Sheet sheet = wb.getSheetAt(0);

        Iterator<Row> iterator = sheet.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();

            StringBuilder builder = new StringBuilder();
            builder.append(row.getCell(0));
            builder.append(" - ");
            builder.append(row.getCell(1));

            System.out.println(builder.toString());
        }

        System.out.println("Excel file read successfully");
    }

}
