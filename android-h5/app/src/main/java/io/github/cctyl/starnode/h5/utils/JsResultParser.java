package io.github.cctyl.starnode.h5.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsResultParser {
    public static Object parse(String jsonValue) {
        if (jsonValue == null) return null;        // undefined
        if ("null".equals(jsonValue)) return null; // null
        if ("undefined".equals(jsonValue)) return null; // null
        try {

            // 尝试解析为 JSON 对象
            return new JSONObject(jsonValue);
        } catch (JSONException e1) {
            try {
                // 尝试解析为 JSON 数组
                return new JSONArray(jsonValue);
            } catch (JSONException e2) {
                // 尝试解析基本类型
                if (jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
                    // 字符串类型（去除引号）
                    return jsonValue.substring(1, jsonValue.length() - 1);
                } else if ("true".equals(jsonValue) || "false".equals(jsonValue)) {
                    // 布尔类型
                    return Boolean.parseBoolean(jsonValue);
                } else {
                    try {
                        // 数字类型
                        return Double.parseDouble(jsonValue);
                    } catch (NumberFormatException e) {
                        // 无法识别的格式
                        return jsonValue;
                    }
                }
            }
        }
    }
}

