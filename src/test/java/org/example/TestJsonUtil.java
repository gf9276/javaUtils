package org.example;

import java.util.ArrayList;

/**
 * ClassName: TestFileUtil
 * Package: org.example
 */
public class TestJsonUtil {
    public static void main(String[] args) {
        ArrayList<String> strLst = new ArrayList<>();
        strLst.add("abc");
        strLst.add("123");
        strLst.add("过分");
        System.out.println(strLst);
        System.out.println(JsonUtil.toJson(strLst));
        System.out.println(JsonUtil.getStrLst(JsonUtil.toJson(strLst)));
    }
}
