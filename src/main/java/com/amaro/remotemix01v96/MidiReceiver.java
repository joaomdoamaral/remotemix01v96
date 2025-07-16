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
            int byteAux = Byte.toUnsignedInt(data[6]);
            int byteCanal = Byte.toUnsignedInt(data[7]);
            int msb = Byte.toUnsignedInt(data[10]);
            int lsb = Byte.toUnsignedInt(data[11]);

            int valorFinal = (msb << 7) | lsb;
            int percentual = (int)((valorFinal / 1023.0) * 100.0);

            int auxNumber = ((byteAux - 0x02) / 3) + 1;
            int inputChannel = byteCanal + 1;

            broadcaster.broadcastVolumeChange(auxNumber, inputChannel, percentual);
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
