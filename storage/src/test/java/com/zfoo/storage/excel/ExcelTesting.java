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
        //第一步创建workbook
        var wb = WorkbookFactory.create(true);

        //第二步创建sheet
        var sheet = wb.createSheet("测试");

        //第三步创建行row:添加表头0行
        var row = sheet.createRow(0);
        var style = wb.createCellStyle();
        //style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  //居中


        //第四步创建单元格
        var cell = row.createCell(0); //第一个单元格
        cell.setCellValue("姓名");
        cell.setCellStyle(style);

        cell = row.createCell(1);         //第二个单元格
        cell.setCellValue("年龄");
        cell.setCellStyle(style);


        //第五步插入数据
        for (int i = 0; i < 5; i++) {
            //创建行
            row = sheet.createRow(i + 1);
            //创建单元格并且添加数据
            row.createCell(0).setCellValue("aa" + i);
            row.createCell(1).setCellValue(i);
        }

        //第六步将生成excel文件保存到指定路径下
        FileOutputStream target = new FileOutputStream("target.xlsx");
        wb.write(target);
        target.close();

        System.out.println("Excel文件生成成功...");
    }

    @Test
    public void readExcelTest() throws IOException {
        Workbook wb = WorkbookFactory.create(new File("target.xlsx"));

        // 只读取第一个工作簿
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

        System.out.println("Excel文件读取成功");
    }

}
