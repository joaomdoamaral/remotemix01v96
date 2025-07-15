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
    public void setarVolume(
            @RequestParam int canal,
            @RequestParam int auxiliar,
            @RequestParam int valor // valor de 0 a 100
    ) {
        midiService.enviarVolumePercentual(canal, auxiliar, valor);
    }
}
