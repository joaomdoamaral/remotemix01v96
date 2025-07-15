package com.amaro.remotemix01v96.simulation;

import com.amaro.remotemix01v96.MidiReceiver;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

public class FakeYamahaResponder implements Receiver {

    private final MidiReceiver receiver;

    public FakeYamahaResponder(MidiReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (!(message instanceof SysexMessage)) return;

        byte[] request = message.getMessage();

        if (request.length >= 9 && request[0] == (byte) 0xF0 && request[1] == (byte) 0x43) {
            int auxiliar = Byte.toUnsignedInt(request[6]);
            int canal = Byte.toUnsignedInt(request[7]);

            // Valor fixo com incremento: canal 1 = 20, canal 2 = 30, ..., até canal 24 = 127 (limite)
            int valor = Math.min(20 + (canal - 1) * 5, 127);

            byte[] response = new byte[]{
                    (byte) 0xF0,
                    (byte) 0x43,
                    (byte) 0x10,
                    (byte) 0x7F,
                    (byte) 0x1C,
                    (byte) 0x11, // Subcomando fictício para resposta
                    (byte) auxiliar,
                    (byte) canal,
                    (byte) valor,
                    (byte) 0xF7
            };

            try {
                SysexMessage simulatedResponse = new SysexMessage();
                simulatedResponse.setMessage(response, response.length);
                receiver.send(simulatedResponse, System.currentTimeMillis());

                System.out.printf("Simulando resposta: AUX %d | Canal %d | Valor %d\n", auxiliar, canal, valor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {}
}
