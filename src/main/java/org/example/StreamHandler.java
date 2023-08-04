package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(StreamHandler.class);
    private final InputStream inputStream;
    private final StringBuilder strBuild;
    private final Integer maxSize;

    /**
     * 使用新线程在 process.waitFor 函数之前读取进程输出流, 防止死锁问题的同时响应中断
     * 注意: 这里说的响应中断是指外面的process.waitFor响应中断后导致该线程出现IOException, bufferedReader.readLine() 不受中断影响
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
        } catch (IOException exception) {
            // bufferedReader.readLine() 会阻塞, 但是不会响应 Thread.interrupt() ... 奇葩
            logger.info(exception.toString());
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