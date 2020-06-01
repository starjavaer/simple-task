package com.ixingji.task.atomic.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;

public class JSONUtils {

    public static String toStr(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static byte[] toStrBytes(Object obj) {
        return toStr(obj).getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T parseObj(byte[] bytes, Class<T> clazz) {
        return JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

    public static JSONObject parseObj(byte[] bytes) {
        return JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
    }

}
