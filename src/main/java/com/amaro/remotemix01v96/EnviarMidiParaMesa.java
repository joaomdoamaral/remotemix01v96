package com.amaro.remotemix01v96;

import javax.sound.midi.*;

public class EnviarMidiParaMesa {

    // Valor correspondente a 0 dB identificado via MIDI-OX
    private static final int MSB_0DB = 6;
    private static final int LSB_0DB = 0x65;

    // Valor máximo permitido
    private static final int MSB_MAX = 7;
    private static final int LSB_MAX = 0x7F;

    public static void main(String[] args) throws Exception {
        String nomeDoDispositivo = "Yamaha 01V96-1";

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        MidiDevice dispositivo = null;

        for (MidiDevice.Info info : infos) {
            if (info.getName().equals(nomeDoDispositivo) && MidiSystem.getMidiDevice(info).getMaxReceivers() != 0) {
                dispositivo = MidiSystem.getMidiDevice(info);
                break;
            }
        }

        if (dispositivo == null) {
            System.out.println("Dispositivo não encontrado.");
            return;
        }
// a sad sa  asd sdas d
        dispositivo.open();
        Receiver receptor = dispositivo.getReceiver();

        int auxiliar = 1;       // AUX desejado (1 a 8)
        int canalEntrada = 4;   // Canal de entrada desejado (1 a 24)
        double dbDesejado = 0.0; // dB desejado (ex: 0.0, -20.0, -5.5)

        int[] valorVolume = converterDbParaBytes(dbDesejado);
        int byteAuxiliar = calcularByteAuxiliar(auxiliar);
        int byteCanal = canalEntrada - 1;

        byte[] mensagem = new byte[] {
                (byte) 0xF0,
                (byte) 0x43,
                (byte) 0x10,
                (byte) 0x3E,
                (byte) 0x7F,
                (byte) 0x01,
                (byte) 0x23,
                (byte) byteAuxiliar,
                (byte) byteCanal,
                (byte) 0x00,
                (byte) 0x00,
                (byte) valorVolume[0],
                (byte) valorVolume[1],
                (byte) 0xF7
        };

        SysexMessage sysex = new SysexMessage();
        sysex.setMessage(mensagem, mensagem.length);
        receptor.send(sysex, -1);

        System.out.printf("Comando enviado para AUX %d, Canal %d, %.1f dB -> [%02X %02X]\n",
                auxiliar, canalEntrada, dbDesejado, valorVolume[0], valorVolume[1]);

        receptor.close();
        dispositivo.close();
    }

    private static int calcularByteAuxiliar(int aux) {
        return 0x02 + (aux - 1) * 3;
    }

    private static int[] converterDbParaBytes(double db) {
        if (db <= -90.0) return new int[] { 0x00, 0x00 };
        if (db >= 10.0) return new int[] { MSB_MAX, LSB_MAX };

        // 0 dB é 06 65 (hex) = 1637 (decimal)
        int valor0db = (MSB_0DB << 7) + LSB_0DB;
        int valorFinal;

        if (db >= 0.0) {
            int valorMax = (MSB_MAX << 7) + LSB_MAX;
            valorFinal = valor0db + (int) ((valorMax - valor0db) * (db / 10.0));
        } else {
            valorFinal = (int) (valor0db * Math.pow(10, db / 20)); // Escala logarítmica
        }

        valorFinal = Math.max(0, Math.min((MSB_MAX << 7) + LSB_MAX, valorFinal));
        int msb = (valorFinal >> 7) & 0x7F;
        int lsb = valorFinal & 0x7F;
        return new int[] { msb, lsb };
    }
}
