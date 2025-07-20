package com.amaro.remotemix01v96;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class MidiWebSocketBroadcaster {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

    public void broadcast(String message) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcastVolumeChange(int auxNumber, int inputChannel, int faderLevel) {
        String json = String.format("{\"canal\":%d,\"valor\":%d}", inputChannel, faderLevel);
        broadcast(json);
    }
}
