package com.ani.taku_backend.common.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;

public class TypeIdResolverForDevTools extends TypeIdResolverBase {
    @Override
    public String idFromValue(Object value) {
        return value.getClass().getName();
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return suggestedType.getName();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> cls = Class.forName(id, true, classLoader);
            return context.constructType(cls);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + id);
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CLASS;
    }
} 