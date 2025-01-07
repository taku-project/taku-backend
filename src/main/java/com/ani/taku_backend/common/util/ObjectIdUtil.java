package com.ani.taku_backend.common.util;

import org.bson.types.ObjectId;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;

public class ObjectIdUtil {
    
    public static ObjectId convertToObjectId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
