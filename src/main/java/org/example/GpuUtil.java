package org.example;

import cn.hutool.core.util.RuntimeUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 只提供申请和释放接口
 */
public class GpuUtil {
    private static final LinkedHashMap<String, Boolean> gpuAvailableList = new LinkedHashMap<>(); // key: uuid & value: is available?
    private static final LinkedHashMap<String, Boolean> gpuLockList = new LinkedHashMap<>(); // key: uuid & value: is locked?


    /**
     * 申请一块 GPU, 并返回其 id
     *
     * @return Integer gpuId
     */
    public static Integer applyForGpu() {
        Integer gpuId = null;
        refreshGpuList();  // 先刷新一遍再说

        int curId = 0;
        for (String uuid : gpuLockList.keySet()) {
            // 满足可用且非锁定状态, 才可以申请gpu
            if (gpuAvailableList.get(uuid) && !gpuLockList.get(uuid)) {
                gpuId = curId;
                gpuLockList.put(uuid, true); // 替换成锁定状态
                break;
            }
            curId++;
        }

        return gpuId;
    }

    /**
     * 根据输入的 gpuId, 释放 GPU 资源
     *
     * @param gpuId: 之前通过申请得到的 gpuId
     */
    public static void releaseGpu(Integer gpuId) {
        refreshGpuList();
        if (gpuId == null || gpuId >= gpuLockList.size()) {
            return;
        }
        String uuid = (String) gpuLockList.keySet().toArray()[gpuId];
        gpuLockList.put(uuid, false); // 解锁
    }


    /**
     * 刷新可用gpu的list
     */
    private static void refreshGpuList() {
        List<GPUInfo> gpuInfoList = getGpuInfos();
        for (GPUInfo gpuInfo : gpuInfoList) {
            // 更新一下, 保持与 gpuAvailableList 内容一致
            if (!gpuLockList.containsKey(gpuInfo.getUuid())) {
                gpuLockList.put(gpuInfo.getUuid(), false);
            }
            // 满足占用率低, 才能被置为可用
            boolean available = gpuInfo.getMemUsageRate() < 25;
            gpuAvailableList.put(gpuInfo.getUuid(), available);
        }
    }


    /**
     * 通过 nvidia-smi -q -x 指令, 获取 gpu 的详细 xml 信息, 解码成具体的 GpuInfo 类
     *
     * @return List<GPUInfo>
     */
    private static List<GPUInfo> getGpuInfos() {
        List<GPUInfo> gpuInfoList = new ArrayList<>();

        try {
            String xmlGpuInfo = RuntimeUtil.execForStr("nvidia-smi -q -x");  // 获取xml输出
            xmlGpuInfo = xmlGpuInfo.replaceAll("<!DOCTYPE.*.dtd\">", ""); // 忽略dtd
            Document document = DocumentHelper.parseText(xmlGpuInfo); // 使用dom4j解析xml字符串
            List<Element> gpu = document.getRootElement().elements("gpu"); // 读取"gpu"这个element
            gpu.forEach(element -> {
                // 获取标识符
                String productName = element.element("product_name").getText(); //名称
                String uuid = element.element("uuid").getText(); // uuid
                // 获取GPU内存信息
                Element fbMemoryUsage = element.element("fb_memory_usage");
                String total = fbMemoryUsage.element("total").getText(); // 总内存
                String used = fbMemoryUsage.element("used").getText(); // 已用内存
                String free = fbMemoryUsage.element("free").getText(); // 空闲内存
                // 获取进程信息
                Element processes = element.element("processes");
                List<Element> infos = processes.elements("process_info");
                List<GPuProcessInfo> GPuProcessInfos = new ArrayList<>();
                infos.forEach(info -> {
                    String pid = info.element("pid").getText();
                    String name = info.element("process_name").getText();
                    String usedMemory = info.element("used_memory").getText();
                    GPuProcessInfo GPuProcessInfo = new GPuProcessInfo();
                    GPuProcessInfo.setPid(pid);
                    GPuProcessInfo.setName(name);
                    GPuProcessInfo.setUsedMemory(usedMemory);
                    GPuProcessInfos.add(GPuProcessInfo);
                });
                // 实例化对象
                GPUInfo gpuInfo = new GPUInfo();
                gpuInfo.setProductName(productName);
                gpuInfo.setUuid(uuid);
                gpuInfo.setTotalMemory(Integer.parseInt(total.substring(0, total.indexOf(" "))));
                gpuInfo.setUsedMemory(Integer.parseInt(used.substring(0, used.indexOf(" "))));
                gpuInfo.setFreeMemory(Integer.parseInt(free.substring(0, used.indexOf(" "))));
                gpuInfo.setMemUsageRate((float) gpuInfo.getUsedMemory() / gpuInfo.getTotalMemory() * 100);
                gpuInfo.setProcessInfos(GPuProcessInfos);
                gpuInfoList.add(gpuInfo);
            });
        } catch (Exception exception) {
            System.out.println(exception.toString());
        }

        return gpuInfoList;
    }


    public static class GPuProcessInfo {
        private String pid;
        private String name;
        private String usedMemory;

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(String usedMemory) {
            this.usedMemory = usedMemory;
        }
    }

    public static class GPUInfo {
        // product_name
        private String productName;
        // uuid
        private String uuid;
        // 总内存, 单位mb
        private int totalMemory;
        // 已用内存, 单位mb
        private int usedMemory;
        // 空闲内存, 单位mb
        private int freeMemory;
        // 使用率（最大为100）
        private float memUsageRate;
        // 进程信息
        private List<GPuProcessInfo> GPuProcessInfos;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public List<GPuProcessInfo> getGPuProcessInfos() {
            return GPuProcessInfos;
        }

        public void setGPuProcessInfos(List<GPuProcessInfo> GPuProcessInfos) {
            this.GPuProcessInfos = GPuProcessInfos;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public int getTotalMemory() {
            return totalMemory;
        }

        public void setTotalMemory(int totalMemory) {
            this.totalMemory = totalMemory;
        }

        public int getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(int usedMemory) {
            this.usedMemory = usedMemory;
        }

        public int getFreeMemory() {
            return freeMemory;
        }

        public void setFreeMemory(int freeMemory) {
            this.freeMemory = freeMemory;
        }

        public float getMemUsageRate() {
            return memUsageRate;
        }

        public void setMemUsageRate(float memUsageRate) {
            this.memUsageRate = memUsageRate;
        }

        public List<GPuProcessInfo> getProcessInfos() {
            return GPuProcessInfos;
        }

        public void setProcessInfos(List<GPuProcessInfo> GPuProcessInfos) {
            this.GPuProcessInfos = GPuProcessInfos;
        }
    }
}


