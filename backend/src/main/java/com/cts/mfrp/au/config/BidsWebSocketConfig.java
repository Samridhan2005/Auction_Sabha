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
        // Keeping "*" allowed for testing on mobile hotspot/Render
        // Render handles the SSL termination, so standard /ws mapping works for wss://
        registry.addHandler(broadcastWebSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}