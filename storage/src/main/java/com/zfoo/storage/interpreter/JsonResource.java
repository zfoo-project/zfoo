package com.zfoo.storage.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 *  配置文件资源
 * 
 */
public class JsonResource {
    // 文件名
    private String name;
    //配置表字段名
    private List<ColumnMeta> columns = new ArrayList<>();
    // 配置表数据
    private List<List<String>> data = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }
   
    public static class ColumnMeta {
        //字段名
        private String name;
        //类型
        private String type;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
}
