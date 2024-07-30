package com.zfoo.orm.convention;

import com.zfoo.orm.anno.Id;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 注解约定
 *
 * @author Sando
 * @version 1.0
 * @since 2024/7/30
 */
public class AnnotationConvention implements Convention {
    public static final AnnotationConvention INSTANCE = new AnnotationConvention();

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        String idPropertyName = classModelBuilder.getIdPropertyName();
        if (idPropertyName != null) {
            try {
                Field idField = classModelBuilder.getType().getDeclaredField(idPropertyName);
                Id annotation = idField.getAnnotation(Id.class);
                if (annotation == null) {
                    throw new RuntimeException("The class " + classModelBuilder.getType().getName() +
                            " has an id property[name=" + idPropertyName + "] but no @Id annotation");
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            processPropertyAnnotations(classModelBuilder, propertyModelBuilder);
        }
    }

    private void processPropertyAnnotations(final ClassModelBuilder<?> classModelBuilder,
                                            final PropertyModelBuilder<?> propertyModelBuilder) {
        for (Annotation annotation : propertyModelBuilder.getReadAnnotations()) {
            if (annotation.annotationType().equals(Id.class)) {
                String fieldName = propertyModelBuilder.getName();
                classModelBuilder.idPropertyName(fieldName);
                break;
            }
        }
    }
}
