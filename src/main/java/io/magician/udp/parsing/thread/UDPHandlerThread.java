package io.magician.udp.parsing.thread;

import io.magician.udp.parsing.ReceiveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 执行业务逻辑的线程
 */
public class UDPHandlerThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(UDPHandlerThread.class);

    private LinkedBlockingDeque<ByteArrayOutputStream> blockingDeque;

    public UDPHandlerThread(LinkedBlockingDeque<ByteArrayOutputStream> blockingDeque){
        this.blockingDeque = blockingDeque;
    }

    /**
     * 轮询执行队列里的任务
     */
    @Override
    public void run() {
        while (true){
            try{
                ByteArrayOutputStream outputStream = blockingDeque.take();
                ReceiveHandler.completed(outputStream);
            } catch (Exception e){
                logger.error("执行请求的队列出现异常", e);
            }
        }
    }
}