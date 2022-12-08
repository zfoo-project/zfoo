package com.zfoo.storage;

import com.zfoo.storage.model.anno.ResInjection;
import com.zfoo.storage.model.vo.Storage;
import com.zfoo.storage.anotherresource.TeacherResource;
import org.springframework.stereotype.Component;

@Component
public class TeacherManager {
    @ResInjection
    public Storage<Integer, TeacherResource> teacherResources;
}
