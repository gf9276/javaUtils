package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.util.Strings;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonUtil {
    private static final Gson gson = new Gson();

    public JsonUtil() {
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 获取字符串列表
     *
     * @param str: 列表经过gson转换得到的json字符串, 例如 ["ab","cd","123"] 这种格式
     * @return List<String>
     */
    public static List<String> getStrLst(String str) {
        return gson.fromJson(str, (new TypeToken<List<String>>() {
        }).getType());
    }


    /**
     * 获取gson转换而来得map
     *
     * @param str: json字符串
     * @return List<String>
     */
    public static Map<String, Object> getMap(String str) {
        Map<String, Object> re = new HashMap<>();
        return Strings.isBlank(str) ? re : gson.fromJson(str, (new TypeToken<Map<String, Object>>() {
        }).getType());
    }

}