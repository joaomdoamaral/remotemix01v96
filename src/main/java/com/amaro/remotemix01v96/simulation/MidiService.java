package com.amaro.remotemix01v96.simulation;

import com.amaro.remotemix01v96.MidiReceiver;
import com.amaro.remotemix01v96.MidiWebSocketBroadcaster;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.midi.*;

@Service
public class MidiService {

    private static final int MSB_MAX = 7;
    private static final int LSB_MAX = 0x7F;

    private MidiDevice outputDevice;
    private MidiDevice inputDevice;
    private Receiver receiver;

    private boolean isOfflineTest = false;

    @Autowired
    private MidiWebSocketBroadcaster broadcaster;

    @PostConstruct
    public void iniciar() {
        try {
            outputDevice = getDeviceWithReceiver("Yamaha");
            inputDevice = getDeviceWithTransmitter("Yamaha");

            if (outputDevice != null) {
                outputDevice.open();
                receiver = outputDevice.getReceiver();
                System.out.println("✅ Dispositivo de envio MIDI aberto.");
            } else {
                System.out.println("⚠️ Dispositivo de envio MIDI não encontrado.");
                isOfflineTest = true;
            }

            if (inputDevice != null) {
                inputDevice.open();
                inputDevice.getTransmitter().setReceiver(new MidiReceiver(broadcaster));
                System.out.println("✅ Dispositivo de entrada MIDI ouvindo.");
            } else {
                System.out.println("⚠️ Dispositivo de entrada MIDI não encontrado.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erro ao inicializar MIDI – iniciando em modo simulado.");
            isOfflineTest = true;
        }
    }

    public void sendCompleteConvertedMidiMessage(int auxNumber, int inputChannel, int faderLevel) throws InvalidMidiDataException {
        faderLevel = Math.max(0, Math.min(100, faderLevel));
        int valorFinal = (int) ((faderLevel / 100.0) * ((MSB_MAX << 7) + LSB_MAX));
        int msb = (valorFinal >> 7) & 0x7F;
        int lsb = valorFinal & 0x7F;

        SysexMessage message = getCompleteConvertedMidiMessage(auxNumber, inputChannel, msb, lsb);

        if (isOfflineTest) {
            System.out.printf("[Simulado] AUX %d, Canal %d, %d%% [%02X %02X]%n", auxNumber, inputChannel, faderLevel, msb, lsb);
        } else {
            receiver.send(message, -1);
        }
    }

    // Busca um dispositivo MIDI que possa ENVIAR mensagens
    private MidiDevice getDeviceWithReceiver(String nameContains) throws MidiUnavailableException {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (info.getName().contains(nameContains)) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxReceivers() != 0) {
                    return device;
                }
            }
        }
        return null;
    }

    // Busca um dispositivo MIDI que possa RECEBER mensagens da mesa (tem Transmitter)
    private MidiDevice getDeviceWithTransmitter(String nameContains) throws MidiUnavailableException {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (info.getName().contains(nameContains)) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                try {
                    device.getTransmitter(); // só pra testar
                    return device;
                } catch (MidiUnavailableException ignored) {}
            }
        }
        return null;
    }

    private SysexMessage getCompleteConvertedMidiMessage(int auxiliar, int canal, int msb, int lsb) throws InvalidMidiDataException {
        int bytePreAdressAuxiliary = canal == 0 ? 57 : 35;
        int byteAuxiliar = canal == 0 ? 0 : (0x02 + (auxiliar - 1) * 3);
        int byteCanal = canal == 0 ? (auxiliar - 1) : (canal - 1);

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



    public void solicitarLeituraDeVolumes(int auxInicial, int auxFinal, int canalInicial, int canalFinal) {
        new Thread(() -> {
            try {
                for (int aux = auxInicial; aux <= auxFinal; aux++) {
                    for (int canal = canalInicial; canal <= canalFinal; canal++) {
                        byte[] mensagem = gerarComandoLeituraSysEx(aux, canal);
                        if (!isOfflineTest && receiver != null) {
                            receiver.send(new SysexMessage(mensagem, mensagem.length), -1);
                        } else {
                            System.out.printf("[Simulado] Leitura: AUX %d, Canal %d%n", aux, canal);
                        }
                        Thread.sleep(30); // delay de 30ms entre comandos
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private byte[] gerarComandoLeituraSysEx(int aux, int canal) {
        int byteEndereco, byteCanal;

        if (canal == 0) {
            // Canal master (endereço diferente)
            byteEndereco = 0x00;
            byteCanal = aux - 1;
        } else {
            byteEndereco = 0x02 + (aux - 1) * 3;
            byteCanal = canal - 1;
        }

        return new byte[] {
                (byte) 0xF0,
                (byte) 0x43,
                (byte) 0x10,
                (byte) 0x3E,
                (byte) 0x7F,
                (byte) 0x01,
                (byte) 0x10,
                (byte) byteEndereco,
                (byte) byteCanal,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x01,
                (byte) 0xF7
        };
    }
}
