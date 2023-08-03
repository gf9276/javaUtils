package org.example;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.lang.reflect.Field;

public class ProcessExeUtil {
    private static final Logger logger = LoggerFactory.getLogger(ProcessExeUtil.class);

    /**
     * 进程执行, 阻塞, 等待执行完毕后返回报错信息和进程输出
     *
     * @param execDirPath: 执行目录
     * @param cmdStr:      执行命令
     * @return new ImmutablePair<>(errStr, outputStrBuilder.toString())
     */
    public static ImmutablePair<String, String> exec(String execDirPath, String cmdStr) {
        // 保存线程输出 & 保存错误信息 & 进程对象
        StringBuilder outputStrBuilder = new StringBuilder();
        String errStr = null;
        Process proc = null;

        try {
            outputStrBuilder = new StringBuilder();

            // 创建执行指定命令的子线程 /bin/bash -c 是linux中用于执行shell命令的解释器 & directory 指定执行命令的目录
            ProcessBuilder procBuilder = new ProcessBuilder("/bin/bash", "-c", cmdStr);
            procBuilder.directory(new File(execDirPath));
            proc = procBuilder.start();

            // 新起一个线程去捕获进程的输出, 防止缓冲区塞满后, 两个进程相互等待而导致的死锁问题
            SequenceInputStream seqInputStream = new SequenceInputStream(proc.getInputStream(), proc.getErrorStream());
            StreamHandler readerThread = new StreamHandler(seqInputStream, outputStrBuilder, 4096);
            readerThread.setName(readerThread.getName() + "(readerThread)");
            readerThread.start();

            // waitFor 等待子进程执行命令完毕 & join 守护线程, 等待读取完毕
            proc.waitFor();
            readerThread.join();

        } catch (IOException | InterruptedException exception) {
            boolean interrupted = Thread.interrupted(); // interrupted() 会获取中断标志位，并清除他
            errStr = exception.toString();
            logger.info("The process encountered an error due to the following reasons: " + errStr);
            if (interrupted) Thread.currentThread().interrupt(); // 恢复中断状态
        } finally {
            if (proc != null) proc.destroyForcibly(); // 无论如何我都会清除这个进程(必要)
        }

        return new ImmutablePair<>(errStr, outputStrBuilder.toString());
    }

    /**
     * 获取 Process 对象 的 pid
     *
     * @param process 进程类
     * @return pid 进程的pid
     */
    public static Long getPid(Process process) {
        Long pid = null;

        try {
            Class<?> clazz = Class.forName("java.lang.UNIXProcess");
            Field field = clazz.getDeclaredField("pid");
            field.setAccessible(true);
            pid = Long.valueOf(String.valueOf(field.get(process)));

        } catch (Exception exception) {
            logger.info(exception.toString());
        }

        return pid;
    }
}
