package com.amaro.remotemix01v96.simulation;

import org.springframework.stereotype.Service;

import javax.sound.midi.*;

@Service
public class MidiService {

    private static final int MSB_MAX = 7;
    private static final int LSB_MAX = 0x7F;

    private Receiver receptor;
    private MidiDevice dispositivo;
    private boolean simulado = false;

    public MidiService() {
        try {
            String nomeDoDispositivo = "Yamaha 01V96-1";
            MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

            for (MidiDevice.Info info : infos) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (info.getName().equals(nomeDoDispositivo) && device.getMaxReceivers() != 0) {
                    dispositivo = device;
                    dispositivo.open();
                    receptor = dispositivo.getReceiver();
                    break;
                }
            }

            if (receptor == null) {
                System.out.println("[Simulação] Dispositivo MIDI não encontrado, iniciando em modo simulado.");
                simulado = true;
            }
        } catch (Exception e) {
            System.out.println("[Simulação] Erro ao inicializar MIDI, iniciando em modo simulado.");
            simulado = true;
        }
    }

    public void enviarVolumePercentual(int canal, int auxiliar, int percentual) {
        percentual = Math.max(0, Math.min(100, percentual));
        int valorFinal = (int) ((percentual / 100.0) * ((MSB_MAX << 7) + LSB_MAX));
        int msb = (valorFinal >> 7) & 0x7F;
        int lsb = valorFinal & 0x7F;

        if (simulado) {
            System.out.printf("[Simulado] Enviando AUX %d, Canal %d, Volume %d%% [%02X %02X]%n",
                    auxiliar, canal, percentual, msb, lsb);
        } else {
            enviarMensagem(canal, auxiliar, msb, lsb);
        }
    }

    private void enviarMensagem(int canal, int auxiliar, int msb, int lsb) {
        int byteAuxiliar = 0x02 + (auxiliar - 1) * 3;
        int byteCanal = canal - 1;

        byte[] mensagem = new byte[]{
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
                (byte) msb,
                (byte) lsb,
                (byte) 0xF7
        };

        try {
            SysexMessage sysex = new SysexMessage();
            sysex.setMessage(mensagem, mensagem.length);
            receptor.send(sysex, -1);

            System.out.printf("Enviado AUX %d, Canal %d -> [%02X %02X]%n", auxiliar, canal, msb, lsb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
