package com.amaro.remotemix01v96;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

public class ListMidiDevices {
    public static void main(String[] args) {
        Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                System.out.println("Nome: " + info.getName());
                System.out.println("Descrição: " + info.getDescription());
                System.out.println("Vendor: " + info.getVendor());
                System.out.println("Versão: " + info.getVersion());
                System.out.println("Transmissor disponível: " + device.getMaxTransmitters());
                System.out.println("Receptor disponível: " + device.getMaxReceivers());
                System.out.println("-----------------------------");
            } catch (MidiUnavailableException e) {
                System.out.println("Indisponível: " + info.getName());
            }
        }
    }
}

