package com.amaro.remotemix01v96.simulation;

import org.springframework.stereotype.Service;

import javax.sound.midi.*;

@Service
public class MidiService {

    private static final int MSB_MAX = 7;
    private static final int LSB_MAX = 0x7F;

    private MidiDevice device;
    private Receiver receiver;
    private boolean isOfflineTest = false;

    public MidiService() {
        try {
            String deviceName = "Yamaha 01V96-1";
            device = getMidiDeviceByName(deviceName);
            if (device != null) {
                device.open();
                receiver = device.getReceiver();
            }
            else{
                receiver = null;
                System.out.println("[Simulação] Dispositivo MIDI não encontrado, iniciando em modo simulado.");
                isOfflineTest = true;
            }
        } catch (Exception e) {
            System.out.println("[Simulação] Erro ao inicializar MIDI, iniciando em modo simulado.");
            isOfflineTest = true;
        }
    }

    public void sendCompleteConvertedMidiMessage(int auxNumber, int inputChannel, int faderLevel) throws InvalidMidiDataException {
        faderLevel = Math.max(0, Math.min(100, faderLevel));
        int valorFinal = (int) ((faderLevel / 100.0) * ((MSB_MAX << 7) + LSB_MAX));
        int msb = (valorFinal >> 7) & 0x7F;
        int lsb = valorFinal & 0x7F;

        if (isOfflineTest) {
            SysexMessage completeConvertedMidiMessage =  getCompleteConvertedMidiMessage(auxNumber, inputChannel, msb, lsb);
            System.out.printf("[Simulado] Enviando AUX %d, Canal %d, Volume %d%% [%02X %02X]%n",
                    auxNumber, inputChannel, faderLevel, msb, lsb);
            System.out.printf(completeConvertedMidiMessage.getMessage().toString());
        } else {
            SysexMessage completeConvertedMidiMessage =  getCompleteConvertedMidiMessage(auxNumber, inputChannel, msb, lsb);
            receiver.send(completeConvertedMidiMessage, -1);
        }
    }

    private MidiDevice getMidiDeviceByName(String deviceName) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            if (info.getName().equals(deviceName) && device.getMaxReceivers() != 0) {
                return device;
            }
        }
        return null;
    }

    private SysexMessage getCompleteConvertedMidiMessage(int auxiliar, int canal,  int msb, int lsb) throws InvalidMidiDataException {
        int bytePreAdressAuxiliary = canal == 0 ? 57 : 35; //se for master do auxiliar é 0x39 senão 0x23
        int byteAuxiliar = canal == 0 ? 0 :  (0x02 + (auxiliar - 1) * 3);
        int byteCanal = canal == 0 ? (auxiliar -1) :  (canal - 1);

        byte[] mensagem = new byte[]{
                (byte) 0xF0,
                (byte) 0x43,
                (byte) 0x10,
                (byte) 0x3E,
                (byte) 0x7F,
                (byte) 0x01,
                (byte) bytePreAdressAuxiliary,
                (byte) byteAuxiliar,
                (byte) byteCanal,
                (byte) 0x00,
                (byte) 0x00,
                (byte) msb,
                (byte) lsb,
                (byte) 0xF7
        };
        SysexMessage sysex = new SysexMessage();
        sysex.setMessage(mensagem, mensagem.length);
        return sysex;
    }

}
