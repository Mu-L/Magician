package io.magician.tcp.codec.impl.http.parsing;

import io.magician.tcp.codec.impl.http.cache.HttpParsingCacheManager;
import io.magician.tcp.codec.impl.http.constant.HttpConstant;
import io.magician.tcp.codec.impl.http.model.HttpParsingCacheModel;
import io.magician.tcp.codec.impl.http.request.MagicianHttpExchange;
import io.magician.common.constant.CommonConstant;
import io.magician.common.util.ByteUtil;
import io.magician.tcp.codec.impl.http.constant.ReqMethod;
import io.magician.tcp.workers.Worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * http报文解析
 */
public class HttpMessageParsing {

    /**
     * head结束符
     */
    private static byte[] headEnd;

    /**
     * 初始化head结束符
     * @throws Exception
     */
    private static void initHeadEndTag() throws Exception {
        if(headEnd == null){
            headEnd = HttpConstant.HEAD_END.getBytes(CommonConstant.ENCODING);
        }
    }

    /**
     * 读取请求数据
     * @return
     */
    public static MagicianHttpExchange completed(MagicianHttpExchange magicianHttpExchange, ByteArrayOutputStream outputStream, Worker worker) throws Exception {

        /* 初始化head结束符 */
        initHeadEndTag();

        /* 从worker获取附件 */
        HttpParsingCacheModel httpParsingCacheModel = HttpParsingCacheManager.getHttpParsingCacheModel(worker);

        /* 如果head没读完就接着读head */
        if(!httpParsingCacheModel.isHeadEnd()){
            /* 查找head结束符，如果没找到就返回-1，找到了就返回位置 */
            int headEndIndex = ByteUtil.byteIndexOf(outputStream.toByteArray(), headEnd);
            if (headEndIndex < 0) {
                httpParsingCacheModel.setHeadEnd(false);
                return null;
            }

            /* 根据head的位置 将head读取出来，并返回head的长度 */
            int length = parseHeader(headEndIndex, magicianHttpExchange, outputStream);
            httpParsingCacheModel.setHeadLength(length);
            httpParsingCacheModel.setHeadEnd(true);

            /* 如果是get请求，那么头读完也就结束了 */
            if(ReqMethod.GET.getCode().equals(magicianHttpExchange.getRequestMethod())){
                return magicianHttpExchange;
            }
        }

        /* 获取head的长度 */
        int headLength = httpParsingCacheModel.getHeadLength();

        /* 如果不是get请求，就要获取content-length */
        long contentLength = magicianHttpExchange.getRequestContentLength();

        /*
         * 如果头读完了，但是没有content-length， 这是属于不正常的现象
         * 但是他毕竟真的读到了数据，所以就当他已经完了吧，返回数据让后面的逻辑尝试执行handler
         */
        if(contentLength < 0){
            getBody(headLength, magicianHttpExchange, outputStream);
            return magicianHttpExchange;
        }

        /* 报文长度-head长度 如果 < 内容长度，就说明还没读完 */
        if ((outputStream.size() - headLength) < contentLength) {
            return null;
        }

        /* 获取body */
        getBody(headLength, magicianHttpExchange, outputStream);
        return magicianHttpExchange;
    }

    /**
     * 截取请求头
     * @param length
     * @return
     */
    private static byte[] subHead(int length, ByteArrayOutputStream outputStream){
        if(length <= 0){
            return new byte[1];
        }

        byte[] nowBytes = outputStream.toByteArray();
        byte[] bytes = new byte[length];
        for(int i=0;i<length;i++){
            bytes[i] = nowBytes[i];
        }
        return bytes;
    }

    /**
     * 读取请求头
     *
     * @throws Exception
     */
    private static int parseHeader(int length, MagicianHttpExchange magicianHttpExchange, ByteArrayOutputStream outputStream) throws Exception {
        String headStr = new String(subHead(length, outputStream));
        String[] headers = headStr.split(HttpConstant.CARRIAGE_RETURN);
        for (int i = 0; i < headers.length; i++) {
            String head = headers[i];
            if (i == 0) {
                /* 读取第一行 */
                readFirstLine(head, magicianHttpExchange);
                continue;
            }

            if (head == null || "".equals(head)) {
                continue;
            }

            /* 读取头信息 */
            String[] header = head.split(HttpConstant.SEPARATOR);
            if (header.length < 2) {
                continue;
            }
            magicianHttpExchange.setRequestHeader(header[0].trim(), header[1].trim());
        }

        return (headStr + HttpConstant.HEAD_END).getBytes(CommonConstant.ENCODING).length;
    }

    /**
     * 解析第一行
     *
     * @param firstLine
     */
    private static void readFirstLine(String firstLine, MagicianHttpExchange magicianHttpExchange) {
        String[] parts = firstLine.split("\\s+");

        /*
         * 请求头的第一行必须由三部分构成，分别为 METHOD PATH VERSION
         * 比如：GET /index.html HTTP/1.1
         */
        if (parts.length < 3) {
            return;
        }
        /* 解析开头的三个信息(METHOD PATH VERSION) */
        magicianHttpExchange.setRequestMethod(parts[0]);
        magicianHttpExchange.setRequestURI(parts[1]);
        magicianHttpExchange.setHttpVersion(parts[2]);
    }

    /**
     * 从报文中获取body
     *
     * @throws Exception
     */
    private static void getBody(int headLength, MagicianHttpExchange magicianHttpExchange, ByteArrayOutputStream outputStream) {
        if (outputStream == null || outputStream.size() < 1) {
            return;
        }
        ByteArrayInputStream requestBody = new ByteArrayInputStream(outputStream.toByteArray());
        /* 跳过head，剩下的就是body */
        requestBody.skip(headLength);

        magicianHttpExchange.setRequestBody(requestBody);
    }
}
