package org.example;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: TestProcessUtil
 * Package: edu.npu.nan.logging.util
 */
public class TestProcessUtil {
    public static void main(String[] args) throws InterruptedException {

        Map<String, Pair<String, String>> map = new HashMap<>();
        Thread thread = new Thread(() -> {
            Pair<String, String> pair = ProcessExeUtil.exec("/home/guof", "sleep 20s && echo ${HOME}");
            map.put("pair", pair);
        });
        thread.start();
        TimeUnit.MILLISECONDS.sleep(10000);
        thread.interrupt();
        thread.join();
        System.out.println(map.get("pair").getLeft());
        System.out.println(map.get("pair").getRight());
    }
}
