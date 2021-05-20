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

package com.zfoo.orm.converter;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.TestUnit;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.ArrayList;

/**
 * 将mysql导出的json数据导入到mongodb
 *
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class MysqlToMongoDB {

    @Test
    public void converter() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);

        var file = new File("E:\\user.json");

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        var result = new ArrayList<UserEntity>();
        try {
            fileInputStream = FileUtils.openInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream, StringUtils.DEFAULT_CHARSET_NAME);
            bufferedReader = new BufferedReader(inputStreamReader);

            var builder = new StringBuilder();
            var line = "";

            bufferedReader.readLine();
            bufferedReader.readLine();

            builder = new StringBuilder();

            var index = 1;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("},") || line.equals("]")) {
                    builder.append("}");
                    var entity = JsonUtils.string2Object(builder.toString(), UserEntity.class);
                    entity.setId(index);
                    index++;
                    result.add(entity);
                    builder = new StringBuilder();
                    continue;
                }

                builder.append(line);
            }

            OrmContext.getAccessor().batchInsert(result);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeIO(bufferedReader, inputStreamReader, fileInputStream);
        }
    }

}
