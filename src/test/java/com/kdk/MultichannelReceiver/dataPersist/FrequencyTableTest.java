package com.kdk.MultichannelReceiver.dataPersist;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/***
 * @author Kamil Ochnik
 */
class FrequencyTableTest {

    private final FrequencyTable testFrequencyTable = new FrequencyTable();

    @Test
    void generateTwoElementsFrequencyTable() {
        //given
        int dataSize = 2;
        double freqStart = 0;
        double freqStep = 0;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(2, testFrequencyTable.getFrequency().length);

    }

    @Test
    void checkFirstElementInFrequencyTable() {
        //given
        int dataSize = 2;
        double freqStart = 10;
        double freqStep = 50;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(10, testFrequencyTable.getFrequency(0), 0);
    }

    @Test
    void checkSecondElementInFrequencyTable() {
        //given
        int dataSize = 2;
        double freqStart = 10;
        double freqStep = 50;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(60, testFrequencyTable.getFrequency(1), 0);
    }

    @Test
    void checkSecondElementInFrequencyTableWhenFreqStartHaveMinusValue() {
        //given
        int dataSize = 3;
        double freqStart = -10;
        double freqStep = 50;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(40, testFrequencyTable.getFrequency(1), 0);
    }

    @Test
    void checkSecondElementInFrequencyTableWhenFreqStepHaveMinusValue() {
        //given
        int dataSize = 3;
        double freqStart = 10;
        double freqStep = -100;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(-90, testFrequencyTable.getFrequency(1), 0);
    }

    @Test
    void checkSecondElementInFrequencyTableWhenBFreqStartAndFreqStepHaveMinusValue() {
        //given
        int dataSize = 3;
        double freqStart = -10;
        double freqStep = -100;
        //when
        testFrequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);
        //then
        assertEquals(-110, testFrequencyTable.getFrequency(1), 0);
    }
}