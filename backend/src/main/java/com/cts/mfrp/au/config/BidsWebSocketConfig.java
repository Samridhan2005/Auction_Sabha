package com.cts.mfrp.au.config;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class BidsWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private BroadcastWebSocketHandler broadcastWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(broadcastWebSocketHandler,"/ws").setAllowedOrigins("*");
    }
}