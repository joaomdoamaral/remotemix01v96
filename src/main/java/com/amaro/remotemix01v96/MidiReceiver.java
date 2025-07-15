package com.amaro.remotemix01v96;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

public class MidiReceiver implements Receiver {

    private final MidiWebSocketBroadcaster broadcaster;

    public MidiReceiver(MidiWebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (!(message instanceof SysexMessage)) return;

        SysexMessage sysex = (SysexMessage) message;
        byte[] data = sysex.getData();

        // Exibe a mensagem para debug
        System.out.println("SysEx recebido: " + byteArrayToHex(data));

        try {
            // ⚠️ Exemplo fictício — você precisará ajustar as posições conforme o manual Yamaha 01V96
            if (data.length >= 9 && data[0] == 0x43) { // Verifica ID da Yamaha
                int auxiliar = Byte.toUnsignedInt(data[6]);
                int canal = Byte.toUnsignedInt(data[7]);   // posição pode variar
                int valor = Byte.toUnsignedInt(data[8]);   // volume lido

                // Envia para o frontend via WebSocket
                broadcaster.broadcastVolumeChange(auxiliar, canal, valor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // Encerramento limpo, se necessário
    }

    private String byteArrayToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X ", b));
        }
        return hex.toString().trim();
    }
}
