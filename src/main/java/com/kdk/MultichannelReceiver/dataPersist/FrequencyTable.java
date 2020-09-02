package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * Klasa przechowuje tablicę czestotliwości
 * @author Kamil Ochnik
 */
@Component
public class FrequencyTable {
    /***
     * Tablica czestotliwości, jednostka double
     */
    private double[] frequency;

    /**
     *Konstruuje obiekt zawierający tablicę czestotliwości
     */
    @Autowired
    public FrequencyTable() {
    }

    /***
     * tworzy tablice o wymaganej ilosci elementow i wypelnia ją czestotliwosciami
     * @param dataSize ilość elementow tablicy
     * @param freqStart początkowa czestotliwość
     * @param freqStep krok miedzy czestotliwosciami
     */
    void generateFrequencyTable(int dataSize, double freqStart, double freqStep) {
        frequency = new double[dataSize];
        frequency[0] = freqStart;
        for (int i = 1; i < dataSize; i++) {
            frequency[i] = frequency[i - 1] + freqStep;
        }
    }

    /***
     * Zwraca częstotliwość z określonej pozycji tablicy czestotliwości
     * @param element element tablicy
     * @return zwraca częstotliwość, jednostka double
     */
    public double getFrequency(int element) {
        return frequency[element];
    }

    /***
     * Zwraca kopię tablicy czestotliwości
     * @return zwraca kopię tablicy czestotliwości, jednostka double
     */
    public double[] getFrequency() {
        return frequency.clone();
    }
}
