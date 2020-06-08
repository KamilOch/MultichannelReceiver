package com.kdk.MultichannelReceiver.dataPersist;

//@Component
public class FrequencyTable {

    double[] frequency;
    int dataSize;
    double freqStart;
    double freqStep;

//    @Autowired
    public FrequencyTable(int dataSize, double freqStart, double freqStep) {
        this.dataSize = dataSize;
        this.freqStart = freqStart;
        this.freqStep = freqStep;

        double frequency[] = new double[dataSize];
        frequency[0] = freqStart;
        for (int i = 1; i < dataSize; i++) {
            frequency[i] = frequency[i - 1] + freqStep;
        }
    }
}
