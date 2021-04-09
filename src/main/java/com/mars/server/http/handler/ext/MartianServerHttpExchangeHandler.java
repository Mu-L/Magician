package com.mars.server.http.handler.ext;

import com.mars.server.http.handler.MartianServerHandler;
import com.mars.server.http.request.MartianHttpExchange;

/**
 * 稍微简单一点的联络器
 * 已经将channel读好了，将请求方法，请求版本，路径，头，body都分好了
 * 并且提供了响应的方法
 */
public interface MartianServerHttpExchangeHandler extends MartianServerHandler<MartianHttpExchange> {

    void request(MartianHttpExchange martianHttpExchange);
}
