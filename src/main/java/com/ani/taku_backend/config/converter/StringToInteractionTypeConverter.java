package com.ani.taku_backend.config.converter;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.core.convert.converter.Converter;
import com.ani.taku_backend.common.enums.InteractionType;

@ReadingConverter
public class StringToInteractionTypeConverter implements Converter<String, InteractionType> {
    
    @Override
    public InteractionType convert(String source) {
        return InteractionType.fromValue(source);
    }
}
