package org.example;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        System.out.println("err: " + map.get("pair").getLeft());
        System.out.println("output: " + map.get("pair").getRight());

        List<String> procDatasetResLst = new ArrayList<>();
        procDatasetResLst.add(null);
        procDatasetResLst.add(null);
        procDatasetResLst.add(null);
        Integer emm = 1;
        System.out.println("hehe"+emm);


    }
}
