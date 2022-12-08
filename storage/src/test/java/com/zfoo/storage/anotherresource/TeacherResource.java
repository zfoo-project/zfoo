package com.zfoo.storage.anotherresource;

import com.zfoo.storage.model.anno.ExcelColumn;
import com.zfoo.storage.model.anno.FilePathResource;
import com.zfoo.storage.model.anno.Id;


/**
 * 使用绝对路径也可以
 */
@FilePathResource("excel/老师信息.xlsx")
public class TeacherResource {
    @Id
    @ExcelColumn("序号")
    private int id;
    @ExcelColumn(index = 1)
    private String name;
    @ExcelColumn("年龄")
    private int age;
    @ExcelColumn("课程")
    private String course;
    @ExcelColumn("班级")
    private String[] classes;

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public String getCourse() {
        return course;
    }
    public String[] getClasses() {
        return classes;
    }
}
