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
        byte[] data = ((SysexMessage) message).getData();

        System.out.println("🎹 SYSEX IN: " + byteArrayToHex(data));

        if (data.length >= 13) {
            // 🔄 Resposta de leitura
            if (data[0] == 0x43 && data[5] == 0x01 && data[6] == 0x10) {
                int endereco = Byte.toUnsignedInt(data[7]);
                int canalRaw = Byte.toUnsignedInt(data[8]);
                int msb = Byte.toUnsignedInt(data[11]);
                int lsb = Byte.toUnsignedInt(data[12]);
                int valor = (msb << 7) + lsb;
                int valorMax = (7 << 7) + 0x7F;
                int percentual = (int) ((valor / (double) valorMax) * 100);

                if (endereco == 0x00) {
                    int aux = canalRaw + 1;
                    broadcaster.broadcastVolumeChange(aux, 0, percentual); // canal 0 = master
                } else {
                    int aux = ((endereco - 0x02) / 3) + 1;
                    int canal = canalRaw + 1;
                    broadcaster.broadcastVolumeChange(aux, canal, percentual);
                }
            }

            // 🔂 Atualização vinda da mesa (ex: mover fader físico)
            else if (data[6] == 0x23) { // comando 0x23 → "data set by panel"
                int endereco = Byte.toUnsignedInt(data[7]);
                int canal = Byte.toUnsignedInt(data[8]) + 1;
                int msb = Byte.toUnsignedInt(data[11]);
                int lsb = Byte.toUnsignedInt(data[12]);
                int valor = (msb << 7) + lsb;
                int valorMax = (7 << 7) + 0x7F;
                int percentual = (int) ((valor / (double) valorMax) * 100);
                int aux = ((endereco - 0x02) / 3) + 1;

                broadcaster.broadcastVolumeChange(aux, canal, percentual);
            }
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
