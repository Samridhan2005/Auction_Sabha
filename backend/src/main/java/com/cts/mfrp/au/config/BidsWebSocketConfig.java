package com.cts.mfrp.au.config;


import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class BidsWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new BroadcastWebSocketHandler(),"/ws").setAllowedOrigins("*");
    }
}
