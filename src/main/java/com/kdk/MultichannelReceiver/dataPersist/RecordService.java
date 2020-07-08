package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecordService {

    private final RecordEntityRepository recordEntityRepository;
    private final ReceivedRecordEntityRepository receivedRecordEntityRepository;
    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository;
    private final FrequencyTable frequencyTable;
    private final List<ThresholdCrossingEntity> thresholdCrossingEntityList;

    //UWAGA testowanie opcji zapisu do bazy danych (v2)
    private final ReceivedRecordOneRawEntityRepository receivedRecordOneRawEntityRepository;
    private final ThresholdCrossingEntityOneRawRepository thresholdCrossingEntityOneRawRepository;
    private ThresholdsTables thresholdsTables;

    @Autowired
    public RecordService(RecordEntityRepository recordEntityRepository,
                         ReceivedRecordEntityRepository receivedRecordEntityRepository,
                         ThresholdCrossingEntityRepository thresholdCrossingEntityRepository, FrequencyTable frequencyTable, List<ThresholdCrossingEntity> thresholdCrossingEntityList, ReceivedRecordOneRawEntityRepository receivedRecordOneRawEntityRepository, ThresholdCrossingEntityOneRawRepository thresholdCrossingEntityOneRawRepository, ThresholdsTables thresholdsTables) {
        this.recordEntityRepository = recordEntityRepository;
        this.receivedRecordEntityRepository = receivedRecordEntityRepository;
        this.thresholdCrossingEntityRepository = thresholdCrossingEntityRepository;
        this.frequencyTable = frequencyTable;
        this.thresholdCrossingEntityList = thresholdCrossingEntityList;
        this.receivedRecordOneRawEntityRepository = receivedRecordOneRawEntityRepository;
        this.thresholdCrossingEntityOneRawRepository = thresholdCrossingEntityOneRawRepository;
        this.thresholdsTables = thresholdsTables;
    }

    public ThresholdsTables addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp,
                                      double freqStart, double freqStep, double threshold) {

        thresholdCrossingEntityList.clear();

        frequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();
        recordEntityRepository.save(newRecord);

        RecordEntity recordEntityFromDb = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        long idFromDB = recordEntityFromDb.getId();


        String allFrequency = "";
        String allSignal = "";

        for (int i = 0; i < dataSize; i++) {
            allFrequency += frequencyTable.getFrequency(i) + " ";
            allSignal += receivedData[i] + " ";
        }

        // UWAGA v2 zmiana zapisu danych w DB wersja z 1 linijka dla calego recordu
        ReceivedRecordOneRowEntity receivedRecordOneRowEntity = ReceivedRecordOneRowEntity
                .builder()
                .frequencyList(allFrequency)
                .signalLevelList(allSignal)
                .recordId(idFromDB)
                .build();

        receivedRecordOneRawEntityRepository.save(receivedRecordOneRowEntity);

        String frequencyThreshold = "";
        String signalThreshold = "";

        for (int i = 0; i < dataSize; i++) {
            if (receivedData[i] > threshold) {
                frequencyThreshold += frequencyTable.getFrequency(i) + " ";
                signalThreshold += receivedData[i] + " ";
            }
        }

        // UWAGA v2 zmiana zapisu danych w DB wersja z 1 linijka dla calego recordu
        if(frequencyThreshold.length()>1) {
            ThresholdCrossingOneRawEntity thresholdCrossingOneRawEntity = ThresholdCrossingOneRawEntity
                    .builder()
                    .frequencyList(frequencyThreshold)
                    .signalLevelList(signalThreshold)
                    .recordId(idFromDB)
                    .build();

            thresholdCrossingEntityOneRawRepository.save(thresholdCrossingOneRawEntity);
        }

//        for (int i = 0; i < dataSize; i++) {
        //UWAGA v1 zapis danych 1 record tworzy  250 linii z danymi w db
//            ReceivedRecordEntity newReceivedRecordEntity = ReceivedRecordEntity.builder()
//                    .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB).build();
//            receivedRecordEntityRepository.save(newReceivedRecordEntity);


//            if (receivedData[i] > threshold) {
//                //UWAGA v1 zapis przekroczen, przekroczenie tworzy 1 linie z danymi w db
//                ThresholdCrossingEntity newThresholdCrossingEntity = ThresholdCrossingEntity.builder()
//                        .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB)
//                        .build();
//                thresholdCrossingEntityRepository.save(newThresholdCrossingEntity);
//
//                thresholdCrossingEntityList.add(newThresholdCrossingEntity);
//            }
//        }

        String[] czestotliwosci = frequencyThreshold.split(" ");
        String[] sygnaly = signalThreshold.split(" ");

        double[] frequencyThresholdDoubleTable = new double[czestotliwosci.length - 1];
        double[] signalThresholdDoubleTable = new double[sygnaly.length - 1];

        for (int i = 0; i < czestotliwosci.length - 1; i++) {
            frequencyThresholdDoubleTable[i] = Double.parseDouble(czestotliwosci[i]);
            signalThresholdDoubleTable[i] = Double.parseDouble(sygnaly[i]);
        }

        thresholdsTables = new ThresholdsTables(frequencyThresholdDoubleTable, signalThresholdDoubleTable);

        return thresholdsTables;
    }

    public List<RecordEntity> getAllRecords() {
        return recordEntityRepository.findAll()
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteAllRecords() {
        List<RecordEntity> allRecords = recordEntityRepository.findAll()
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());

        for (RecordEntity re : allRecords
        ) {
            recordEntityRepository.delete(re);
        }
    }

    public List<RecordEntity> getRecordsBySeqNumber(int seqNumber) {
        return recordEntityRepository.findBySeqNumber(seqNumber)
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteRecordsBySeqNumber(int seqNumber) {
        List<RecordEntity> allRecordsBySeqNumber = recordEntityRepository.findBySeqNumber(seqNumber)
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());

        for (RecordEntity re : allRecordsBySeqNumber
        ) {
            recordEntityRepository.delete(re);
        }
    }

    public RecordEntity findRecordByTimeStamp(double timeStamp) {

        RecordEntity record = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        return RecordEntity.builder().id(record.getId())
                .timeStamp(record.getTimeStamp())
                .seqNumber(record.getSeqNumber())
                .threshold(record.getThreshold())
                .build();
    }

    public void deleteRecordByTimeStamp(double timeStamp) {
        RecordEntity record = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        recordEntityRepository.delete(RecordEntity.builder().id(record.getId())
                .timeStamp(record.getTimeStamp())
                .seqNumber(record.getSeqNumber())
                .threshold(record.getThreshold())
                .build());
    }

    public List<ReceivedRecordEntity> getReceivedRecordByRecordId(long recordId) {
        return receivedRecordEntityRepository.findByRecordId(recordId)
                .stream()
                .map(it -> ReceivedRecordEntity.builder()
                        .id(it.getId())
                        .frequency(it.getFrequency())
                        .signalLevel(it.getSignalLevel())
                        .recordId(it.getRecordId())
                        .build())
                .collect(Collectors.toList());
    }

    public ReceivedRecordOneRowEntity getReceivedRecordOneRawByRecordId(long recordId) {
        ReceivedRecordOneRowEntity entity = receivedRecordOneRawEntityRepository.findByRecordId(recordId);
        return ReceivedRecordOneRowEntity
                .builder()
                .id(entity.getId())
                .frequencyList(entity.getFrequencyList())
                .signalLevelList(entity.getSignalLevelList())
                .recordId(entity.getRecordId())
                .build();
    }

    public List<ThresholdCrossingEntity> getThresholdCrossingRecordByRecordId(long recordId) {
        return thresholdCrossingEntityRepository.findByRecordId(recordId)
                .stream()
                .map(it -> ThresholdCrossingEntity.builder()
                        .id(it.getId())
                        .frequency(it.getFrequency())
                        .signalLevel(it.getSignalLevel())
                        .recordId(it.getRecordId())
                        .build())
                .collect(Collectors.toList());
    }

    public ThresholdCrossingOneRawEntity getThresholdCrossingRecordOneRawByRecordId(long recordId) {
        ThresholdCrossingOneRawEntity entity = thresholdCrossingEntityOneRawRepository.findByRecordId(recordId);
        return ThresholdCrossingOneRawEntity.builder()
                .id(entity.getId())
                .frequencyList(entity.getFrequencyList())
                .signalLevelList(entity.getSignalLevelList())
                .recordId(entity.getRecordId())
                .build();
    }
}
