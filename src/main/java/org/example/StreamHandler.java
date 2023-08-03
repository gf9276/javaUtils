package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(StreamHandler.class);
    private final InputStream inputStream;
    private final StringBuilder strBuild;
    private final Integer maxSize;

    /**
     * 使用新线程在 process.waitFor 函数之前读取进程输出流,可以防止死锁问题
     *
     * @param inputStream: 输入流
     * @param strBuild:    保存字符
     * @param maxSize:     要保留的字符串最大大小, 小于等于0表示没有限制
     */
    public StreamHandler(InputStream inputStream, StringBuilder strBuild, Integer maxSize) {
        this.maxSize = maxSize;
        this.inputStream = inputStream;
        this.strBuild = strBuild;
    }


    public void run() {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            // 获取输入流 --> 生成输入缓冲区
            inputStreamReader = new InputStreamReader(this.inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            // 读取, 并限制大小
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null) {
                if (strBuild != null) {
                    strBuild.append(strLine).append("\n");
                    if (maxSize != null && maxSize > 0 && strBuild.length() > maxSize) {
                        strBuild.delete(0, strBuild.length() - maxSize);
                    }
                }
            }
        } catch (Exception exception) {
            boolean interrupted = Thread.interrupted(); // interrupted() 会获取中断标志位，并清除他
            logger.info(exception.toString());
            if (interrupted) Thread.currentThread().interrupt(); // 恢复中断状态
        } finally {
            try {
                // 收拾残局
                if (inputStreamReader != null) inputStreamReader.close();
                if (bufferedReader != null) bufferedReader.close();
                if (this.inputStream != null) this.inputStream.close();
            } catch (Exception exception) {
                logger.info(exception.toString());
            }
        }
    }
}