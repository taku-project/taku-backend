package com.ani.taku_backend.config.converter;

import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.Nullable;
import org.springframework.core.convert.converter.Converter;

import com.ani.taku_backend.common.enums.InteractionType;

/**
 * InteractionType 엔티티를 String으로 변환하는 컨버터
 */
@WritingConverter
public class InteractionTypeConverter implements Converter<InteractionType, String> {

    @Override
    @Nullable
    public String convert(InteractionType source) {
        return source.getValue();
    }
}
