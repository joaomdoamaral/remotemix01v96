package com.amaro.remotemix01v96;

import com.amaro.remotemix01v96.simulation.MidiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MidiWebSocketHandler extends TextWebSocketHandler {

    private final MidiService midiService;
    private final MidiWebSocketBroadcaster broadcaster;

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
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(message.getPayload());

            int canal = json.get("canal").asInt();
            int auxiliar = json.get("auxiliar").asInt();
            int valor = json.get("valor").asInt();

            // Simula envio de mensagem MIDI (log no console)
            System.out.printf("[Simulação] Enviar para AUX %d, Canal %d, Valor %d%%%n", auxiliar, canal, valor);

            // Quando for usar real:
            midiService.enviarVolumePercentual(canal, auxiliar, valor);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
