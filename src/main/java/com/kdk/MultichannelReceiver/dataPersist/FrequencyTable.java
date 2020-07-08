package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FrequencyTable {

    private double[] frequency;

    @Autowired
    public FrequencyTable(double[] frequency) {
        this.frequency = frequency;
    }

    void generateFrequencyTable(int dataSize, double freqStart, double freqStep) {
        frequency = new double[dataSize];
        frequency[0] = freqStart;
        for (int i = 1; i < dataSize; i++) {
            frequency[i] = frequency[i - 1] + freqStep;
        }
    }

    public double getFrequency(int element) {
        return frequency[element];
    }

    public double[] getFrequency() {
        return frequency.clone();
    }
}
