package org.example;

/**
 * ClassName: ${NAME}
 * Package: org.example
 */
public class TestGpuUtil {
    public static void main(String[] args) {
        Integer gpuId1 = GpuUtil.applyForGpu();
        Integer gpuId2 = GpuUtil.applyForGpu();

        GpuUtil.releaseGpu(gpuId1);
        GpuUtil.releaseGpu(gpuId2);

        gpuId2 = GpuUtil.applyForGpu();
        gpuId1 = GpuUtil.applyForGpu();

        GpuUtil.releaseGpu(gpuId2);
        GpuUtil.releaseGpu(gpuId1);

        System.out.println();
    }
}