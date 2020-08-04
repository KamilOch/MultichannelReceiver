package com.kdk.MultichannelReceiver.dataPersist;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RecordServiceTest {

    private final RecordEntityRepository recordEntityRepository = mock(RecordEntityRepository.class);
    private final ReceivedRecordEntityRepository receivedRecordEntityRepository = mock(ReceivedRecordEntityRepository.class);
    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository = mock(ThresholdCrossingEntityRepository.class);
    private final FrequencyTable frequencyTable = new FrequencyTable();
    private final List<ThresholdCrossingEntity> thresholdCrossingEntityList = new ArrayList<>();

    //UWAGA testowanie opcji zapisu do bazy danych (v2) zapis w 1 linii (krotce)
    private final ReceivedRecordOneRawEntityRepository receivedRecordOneRawEntityRepository = mock(ReceivedRecordOneRawEntityRepository.class);
    private final ThresholdCrossingEntityOneRawRepository thresholdCrossingEntityOneRawRepository = mock(ThresholdCrossingEntityOneRawRepository.class);
    private ThresholdsTables thresholdsTable;

    private RecordService testRecordService = new RecordService(
            recordEntityRepository,
            receivedRecordEntityRepository,
            thresholdCrossingEntityRepository,
            frequencyTable,
            thresholdCrossingEntityList,
            receivedRecordOneRawEntityRepository,
            thresholdCrossingEntityOneRawRepository,
            thresholdsTable);

    @Test
    void addOneExampleRecordAndCheckRecordEntityRepository() {
        //given
        double[] receivedData = new double[2];
        receivedData[0] = 100;
        receivedData[1] = 200;

        int dataSize = 2;
        int seqNumber = 1;
        double timeStamp = 58972597;
        double freqStart = 9000;
        double freqStep = 1000;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(timeStamp)).thenReturn(java.util.Optional.ofNullable(newRecord));

        //when
        testRecordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);
        //then
        verify(recordEntityRepository, times(1)).save(newRecord);
    }

    @Test
    void addOneExampleRecordAndCheckReceivedRecordOneRawEntityRepository() {
        //given
        double[] receivedData = new double[2];
        receivedData[0] = 100;
        receivedData[1] = 200;

        int dataSize = 2;
        int seqNumber = 1;
        double timeStamp = 58972597;
        double freqStart = 9000;
        double freqStep = 1000;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(timeStamp)).thenReturn(java.util.Optional.ofNullable(newRecord));

        ReceivedRecordOneRowEntity receivedRecordOneRowEntity = ReceivedRecordOneRowEntity
                .builder()
                .frequencyList("9000.0 10000.0 ")
                .signalLevelList("100.0 200.0 ")
                .recordId(0)
                .build();
        //when
        testRecordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);
        //then
        verify(receivedRecordOneRawEntityRepository, times(1)).save(receivedRecordOneRowEntity);
    }

    @Test
    void addOneExampleRecordAndCheckThresholdCrossingEntityOneRawRepositoryExpectTwoFrequency() {
        //given
        double[] receivedData = new double[2];
        receivedData[0] = 100;
        receivedData[1] = 200;

        int dataSize = 2;
        int seqNumber = 1;
        double timeStamp = 58972597;
        double freqStart = 9000;
        double freqStep = 1000;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(timeStamp)).thenReturn(java.util.Optional.ofNullable(newRecord));

        ThresholdCrossingOneRawEntity thresholdCrossingOneRawEntity = ThresholdCrossingOneRawEntity
                .builder()
                .frequencyList("9000.0 10000.0 ")
                .signalLevelList("100.0 200.0 ")
                .recordId(0)
                .build();
        //when
        testRecordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);
        //then
        verify(thresholdCrossingEntityOneRawRepository, times(1)).save(thresholdCrossingOneRawEntity);
    }

    @Test
    void addOneExampleRecordAndCheckThresholdCrossingEntityOneRawRepositoryExpectOneFrequency() {
        //given
        double[] receivedData = new double[2];
        receivedData[0] = 100;
        receivedData[1] = 50;

        int dataSize = 2;
        int seqNumber = 1;
        double timeStamp = 58972597;
        double freqStart = 9000;
        double freqStep = 1000;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(timeStamp)).thenReturn(java.util.Optional.ofNullable(newRecord));

        ThresholdCrossingOneRawEntity thresholdCrossingOneRawEntity = ThresholdCrossingOneRawEntity
                .builder()
                .frequencyList("9000.0 ")
                .signalLevelList("100.0 ")
                .recordId(0)
                .build();
        //when
        testRecordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);
        //then
        verify(thresholdCrossingEntityOneRawRepository, times(1)).save(thresholdCrossingOneRawEntity);
    }

    @Test
    void getAllRecordsWhenRepositoryIsEmpty() {
        //given
        when(recordEntityRepository.findAll()).thenReturn(Collections.emptyList());
        //when
        List<RecordEntity> recordEntityList = testRecordService.getAllRecords();
        //then
        assertEquals(0, recordEntityList.size());
    }

    @Test
    void getAllRecordsWhenRepositoryHaveOneEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findAll()).thenReturn(Arrays.asList(newRecord));
        //when
        List<RecordEntity> recordEntityList = testRecordService.getAllRecords();
        //then
        assertEquals(1, recordEntityList.size());
        assertEquals(58972597, recordEntityList.get(0).getTimeStamp());
        assertEquals(1, recordEntityList.get(0).getSeqNumber());
        assertEquals(90, recordEntityList.get(0).getThreshold());
    }

    @Test
    void getAllRecordsWhenRepositoryHaveTwoEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        int seqNumberTwo = 2;
        double timeStampTwo = 98972597;
        double thresholdTwo = 100;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        RecordEntity newRecordTwo = RecordEntity.builder().timeStamp(timeStampTwo).seqNumber(seqNumberTwo).threshold(thresholdTwo)
                .build();

        when(recordEntityRepository.findAll()).thenReturn(Arrays.asList(newRecord, newRecordTwo));
        //when
        List<RecordEntity> recordEntityList = testRecordService.getAllRecords();
        //then
        assertEquals(2, recordEntityList.size());
    }

    @Test
    void deleteAllRecordsWhenRepositoryHaveOneEntity() {
        //given
        double[] receivedData = new double[2];
        receivedData[0] = 100;
        receivedData[1] = 50;

        int dataSize = 2;
        int seqNumber = 1;
        double timeStamp = 58972597;
        double freqStart = 9000;
        double freqStep = 1000;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(timeStamp)).thenReturn(java.util.Optional.ofNullable(newRecord));
        when(recordEntityRepository.findAll()).thenReturn(Arrays.asList(newRecord));
        //when
        testRecordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);
        testRecordService.deleteAllRecords();
        //then
        verify(recordEntityRepository, times(1)).delete(newRecord);
    }

    @Test
    void deleteAllRecordsWhenRepositoryHaveTwoEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        int seqNumberTwo = 2;
        double timeStampTwo = 98972597;
        double thresholdTwo = 100;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        RecordEntity newRecordTwo = RecordEntity.builder().timeStamp(timeStampTwo).seqNumber(seqNumberTwo).threshold(thresholdTwo)
                .build();

        when(recordEntityRepository.findAll()).thenReturn(Arrays.asList(newRecord, newRecordTwo));
        //when
        testRecordService.deleteAllRecords();
        //then
        verify(recordEntityRepository, times(1)).delete(newRecord);
        verify(recordEntityRepository, times(1)).delete(newRecordTwo);
    }

    @Test
    void getRecordsBySeqNumberWhenRepositoryHaveOneEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findBySeqNumber(1)).thenReturn(Arrays.asList(newRecord));
        //when
        List<RecordEntity> recordEntityList = testRecordService.getRecordsBySeqNumber(1);
        //then
        assertEquals(1, recordEntityList.size());
        assertEquals(58972597, recordEntityList.get(0).getTimeStamp());
        assertEquals(1, recordEntityList.get(0).getSeqNumber());
        assertEquals(90, recordEntityList.get(0).getThreshold());
    }

    @Test
    void getRecordsBySeqNumberWhenRepositoryHaveTwoEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        int seqNumberTwo = 1;
        double timeStampTwo = 98972597;
        double thresholdTwo = 100;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        RecordEntity newRecordTwo = RecordEntity.builder().timeStamp(timeStampTwo).seqNumber(seqNumberTwo).threshold(thresholdTwo)
                .build();

        when(recordEntityRepository.findBySeqNumber(1)).thenReturn(Arrays.asList(newRecord, newRecordTwo));
        //when
        List<RecordEntity> recordEntityList = testRecordService.getRecordsBySeqNumber(1);
        //then
        assertEquals(2, recordEntityList.size());
    }

    @Test
    void deleteRecordsBySeqNumberWhenRepositoryHaveTwoEntity() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        int seqNumberTwo = 1;
        double timeStampTwo = 98972597;
        double thresholdTwo = 100;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        RecordEntity newRecordTwo = RecordEntity.builder().timeStamp(timeStampTwo).seqNumber(seqNumberTwo).threshold(thresholdTwo)
                .build();

        when(recordEntityRepository.findBySeqNumber(1)).thenReturn(Arrays.asList(newRecord, newRecordTwo));
        //when
        testRecordService.deleteRecordsBySeqNumber(1);
        //then
        verify(recordEntityRepository, times(1)).delete(newRecord);
        verify(recordEntityRepository, times(1)).delete(newRecordTwo);
    }

    @Test
    void findRecordByTimeStamp() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(58972597)).thenReturn(java.util.Optional.ofNullable(newRecord));
        //when
        RecordEntity recordEntity = testRecordService.findRecordByTimeStamp(58972597);
        //then
        assertEquals(58972597, recordEntity.getTimeStamp());
        assertEquals(1, recordEntity.getSeqNumber());
        assertEquals(90, recordEntity.getThreshold());

    }

    @Test
    void deleteRecordByTimeStamp() {
        //given
        int seqNumber = 1;
        double timeStamp = 58972597;
        double threshold = 90;

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();

        when(recordEntityRepository.findByTimeStamp(58972597)).thenReturn(java.util.Optional.ofNullable(newRecord));
        //when
       testRecordService.deleteRecordByTimeStamp(58972597);
        //then
        verify(recordEntityRepository, times(1)).delete(newRecord);
    }

    @Test
    void getReceivedRecordOneRawByRecordId() {
        //given
        ReceivedRecordOneRowEntity receivedRecordOneRowEntity = ReceivedRecordOneRowEntity
                .builder()
                .frequencyList("9000.0 10000.0 ")
                .signalLevelList("100.0 200.0 ")
                .recordId(100)
                .build();

        when(receivedRecordOneRawEntityRepository.findByRecordId(100)).thenReturn(receivedRecordOneRowEntity);
        //when
        ReceivedRecordOneRowEntity entity = testRecordService.getReceivedRecordOneRawByRecordId(100);
        //then
        assertEquals("9000.0 10000.0 ", entity.getFrequencyList());
        assertEquals("100.0 200.0 ", entity.getSignalLevelList());


    }

    @Test
    void getThresholdCrossingRecordOneRawByRecordId() {
        //given
        ThresholdCrossingOneRawEntity thresholdCrossingOneRawEntity = ThresholdCrossingOneRawEntity
                .builder()
                .frequencyList("9000.0 10000.0 ")
                .signalLevelList("100.0 200.0 ")
                .recordId(110)
                .build();

        when(thresholdCrossingEntityOneRawRepository.findByRecordId(100)).thenReturn(thresholdCrossingOneRawEntity);
        //when
        ThresholdCrossingOneRawEntity entity = testRecordService.getThresholdCrossingRecordOneRawByRecordId(100);
        //then
        assertEquals("9000.0 10000.0 ", entity.getFrequencyList());
        assertEquals("100.0 200.0 ", entity.getSignalLevelList());

    }
}