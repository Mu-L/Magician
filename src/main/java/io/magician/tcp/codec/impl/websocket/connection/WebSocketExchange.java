package io.magician.tcp.codec.impl.websocket.connection;

import io.magician.tcp.codec.impl.websocket.constant.WebSocketEnum;

import java.io.ByteArrayOutputStream;

/**
 * webSocket数据中转器
 */
public class WebSocketExchange {

    /**
     * 会话
     */
    private WebSocketSession webSocketSession;
    /**
     * webSocket状态
     */
    private WebSocketEnum webSocketEnum;
    /**
     * 报文数据
     */
    private ByteArrayOutputStream outputStream;
    /**
     * 数据包长度
     */
    private int length;

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public WebSocketEnum getWebSocketEnum() {
        return webSocketEnum;
    }

    public void setWebSocketEnum(WebSocketEnum webSocketEnum) {
        this.webSocketEnum = webSocketEnum;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
