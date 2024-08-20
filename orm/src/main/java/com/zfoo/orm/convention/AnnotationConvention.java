package com.zfoo.orm.convention;

import com.zfoo.orm.anno.Id;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.ReflectionUtils;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 注解约定
 *
 * @author Sando
 */
public class AnnotationConvention implements Convention {

    public static final AnnotationConvention INSTANCE = new AnnotationConvention();

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        String idPropertyName = classModelBuilder.getIdPropertyName();
        if (idPropertyName != null) {
            Field idField = ReflectionUtils.getFieldByNameInPOJOClass(classModelBuilder.getType(), idPropertyName);
            Id annotation = idField.getAnnotation(Id.class);
            if (annotation == null) {
                throw new RunException("The class:[{}] has an id property:[{}] but no @Id annotation", classModelBuilder.getType().getName(), idPropertyName);
            }
            return;
        }
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            for (Annotation annotation : propertyModelBuilder.getReadAnnotations()) {
                if (annotation.annotationType().equals(Id.class)) {
                    String fieldName = propertyModelBuilder.getName();
                    classModelBuilder.idPropertyName(fieldName);
                    break;
                }
            }
        }
    }

}