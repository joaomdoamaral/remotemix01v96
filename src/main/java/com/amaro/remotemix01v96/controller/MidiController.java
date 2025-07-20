package com.amaro.remotemix01v96.controller;

import com.amaro.remotemix01v96.simulation.MidiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/midi")
public class MidiController {

    @Autowired
    private MidiService midiService;

    @PostMapping("/volume")
    public void setarVolume(@RequestParam int auxNumber, @RequestParam int inputChannel, @RequestParam int faderLevel // valor de 0 a 100
    ) {
        try {
            midiService.sendCompleteConvertedMidiMessage(auxNumber, inputChannel, faderLevel);
        }
        catch (Exception e) {
            System.out.println("Erro ao enviar midi: " + e.getMessage());
        }
    }

    @PostMapping("/sincronizar")
    public void sincronizarVolumes(@RequestParam("aux") int aux) {
        System.out.println(" #### Sincronando midi ... ######");
        midiService.solicitarLeituraDeVolumes(aux, aux, 0, 24); // inclui o canal 0 (master)
    }
}
