package org.example;

import java.io.File;

/**
 * ClassName: TestFileUtil
 * Package: org.example
 */
public class TestFileUtil {
    public static void main(String[] args) {

        FileUtil.copyContent(new File(System.getProperty("user.dir") + File.separator + ".idea"),
                new File(System.getProperty("user.dir") + File.separator + "hehe/"),
                true);

    }
}
