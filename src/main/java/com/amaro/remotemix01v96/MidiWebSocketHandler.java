package com.amaro.remotemix01v96;

import com.amaro.remotemix01v96.simulation.MidiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MidiWebSocketHandler extends TextWebSocketHandler {

    private final MidiService midiService;
    private final MidiWebSocketBroadcaster broadcaster;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MidiWebSocketHandler(MidiService midiService, MidiWebSocketBroadcaster broadcaster) {
        this.midiService = midiService;
        this.broadcaster = broadcaster;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        broadcaster.register(session);
        System.out.println("Conexão WebSocket estabelecida: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        broadcaster.unregister(session);
        System.out.println("Conexão WebSocket encerrada: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            MidiMessageDto dto = objectMapper.readValue(message.getPayload(), MidiMessageDto.class);

            System.out.printf("[Simulação] Enviar para AUX %d, Canal %d, Valor %d%%%n",
                    dto.auxNumber(), dto.inputChannel(), dto.faderLevel());

            midiService.sendCompleteConvertedMidiMessage(dto.auxNumber(), dto.inputChannel(), dto.faderLevel());
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
