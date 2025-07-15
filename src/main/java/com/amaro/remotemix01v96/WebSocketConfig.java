package com.amaro.remotemix01v96;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MidiWebSocketHandler midiWebSocketHandler;

    public WebSocketConfig(MidiWebSocketHandler midiWebSocketHandler) {
        this.midiWebSocketHandler = midiWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(midiWebSocketHandler, "/ws/midi")
                .setAllowedOrigins("*");
    }
}
