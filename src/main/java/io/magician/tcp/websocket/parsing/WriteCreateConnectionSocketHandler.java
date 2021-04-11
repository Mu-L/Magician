package io.magician.tcp.websocket.parsing;

import io.magician.tcp.http.server.HttpServerConfig;
import io.magician.tcp.http.util.ChannelUtil;
import io.magician.tcp.websocket.WebSocketSession;
import io.magician.tcp.websocket.cache.ConnectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

/**
 * 往socket客户端写数据，建立连接
 */
public class WriteCreateConnectionSocketHandler implements CompletionHandler<Integer, ByteBuffer> {

    private Logger logger = LoggerFactory.getLogger(WriteCreateConnectionSocketHandler.class);
    /**
     * 通道
     */
    private AsynchronousSocketChannel channel;
    /**
     * socket会话
     */
    private WebSocketSession socketSession;

    /**
     * 构造函数
     * @param channel
     * @param socketSession
     */
    public WriteCreateConnectionSocketHandler(AsynchronousSocketChannel channel, WebSocketSession socketSession){
        this.channel = channel;
        this.socketSession = socketSession;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        try {
            if(attachment.hasRemaining()){
                channel.write(attachment, HttpServerConfig.getWriteTimeout(),
                        TimeUnit.MILLISECONDS, attachment,this);
            }
        } catch (Exception e){
            ConnectionCache.removeSession(socketSession.getId());
            ChannelUtil.close(channel);
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        logger.error("写入socket客户端异常", exc);
        ConnectionCache.removeSession(socketSession.getId());
        ChannelUtil.close(channel);
    }
}
