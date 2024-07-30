package com.zfoo.orm.convention;

import com.zfoo.orm.anno.Id;
import org.bson.BsonType;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;
import org.bson.codecs.pojo.annotations.*;

import java.lang.annotation.Annotation;

/**
 * zfoo注解约定
 *
 * @author Sando
 * @version 1.0
 * @since 2024/7/30
 */
public class ZfooAnnotationConvention implements Convention {
    public static final ZfooAnnotationConvention INSTANCE = new ZfooAnnotationConvention();
    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            processPropertyAnnotations(classModelBuilder, propertyModelBuilder);
        }
    }

    private void processPropertyAnnotations(final ClassModelBuilder<?> classModelBuilder,
                                            final PropertyModelBuilder<?> propertyModelBuilder) {
        for (Annotation annotation : propertyModelBuilder.getReadAnnotations()) {
            if (annotation instanceof Id) {
                String idPropertyName = classModelBuilder.getIdPropertyName();
                String fieldName = propertyModelBuilder.getName();
                if (idPropertyName != null && !fieldName.equals(idPropertyName)) {
                    // allow using @Id and @BsonId on same field
                    String typeName = classModelBuilder.getType().getName();
                    throw new IllegalStateException("The class " +
                            typeName + " has more than one id property. The properties are " +
                            idPropertyName + " and " + fieldName);
                }
                classModelBuilder.idPropertyName(fieldName);
            }
        }
    }
}
