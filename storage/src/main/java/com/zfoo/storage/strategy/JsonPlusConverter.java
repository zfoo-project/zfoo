package com.zfoo.storage.strategy;

import com.zfoo.storage.utils.JACKSONUtils;
import com.zfoo.storage.utils.json.ArrayValue;
import com.zfoo.storage.utils.json.ObjectValue;
import com.zfoo.storage.utils.json.TreeValue;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * @Description: jackson parser
 * 1. 削弱繁重的map类型以及list类型等, 尤其是嵌套类型
 * 2. 减少装箱次数提升效率
 * 3. 新增了两类增强型map -> ObjectValue 和list -> ArrayValue, 功能强大
 * 4. 支持混合类型, [1,2,3,"4",5.0,...], {[0], [1], 1}
 * 5. 建议Orm部分支持 该内容
 * @Author: y.z
 * @Date: 2024/5/7 19:01
 */
public class JsonPlusConverter implements ConditionalGenericConverter {
    @Override
    public boolean matches(@NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        return sourceType.getType() == String.class
                && Set.of(TreeValue.class
//                        , NumericValue.class
//                        , BooleanValue.class
//                        , TextValue.class
                )
                .stream().anyMatch(t -> t.isAssignableFrom(targetType.getType()));
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(String.class, ObjectValue.class),
                      new ConvertiblePair(String.class, ArrayValue.class));
    }

    @Override
    public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (targetType.getType().isAssignableFrom(ArrayValue.class)) {
            return JACKSONUtils.parseArray(source.toString());
        } else if (targetType.getType().isAssignableFrom(ObjectValue.class)) {
            return JACKSONUtils.parseObject(source.toString());
        } else {
            return JACKSONUtils.parse(source.toString());
        }
    }
}
