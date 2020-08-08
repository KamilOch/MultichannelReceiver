package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/***
 * Klasa przechowuje tablicę czestotliwości oraz tablicę poziomu sygnału
 * @author Kamil Ochnik
 */
@Component
public class ThresholdsTables {

    private final double[] frequency;
    private final double[] signal;

    @Autowired
    public ThresholdsTables(double[] frequency, double[] signal) {
        this.frequency = frequency;
        this.signal = signal;
    }

    public double[] getFrequency() {
        return frequency;
    }

    public double[] getSignal() {
        return signal;
    }
}
