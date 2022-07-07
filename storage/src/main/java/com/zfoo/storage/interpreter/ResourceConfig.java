package com.zfoo.storage.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 *  配置文件资源
 * 
 */
public class ResourceConfig {
    // 文件名
    private String name;
    //配置表字段名
    private List<Header> header = new ArrayList<>();
    // 配置表数据
    private List<List<String>> data = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Header> getHeader() {
        return header;
    }

    public void setHeader(List<Header> header) {
        this.header = header;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }
   
    public static class Header {
        //字段名
        private String name;
        //类型
        private String type;
        //列
        private int index;

        public Header() {
        }

        public Header(String name, String type, int index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }

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

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
