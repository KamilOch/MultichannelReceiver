package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecordService {

    private final RecordEntityRepository recordEntityRepository;
    private final ReceivedRecordEntityRepository receivedRecordEntityRepository;
    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository;
    private final FrequencyTable frequencyTable;
    private final List<ThresholdCrossingEntity> thresholdCrossingEntityList;

    @Autowired
    public RecordService(RecordEntityRepository recordEntityRepository,
                         ReceivedRecordEntityRepository receivedRecordEntityRepository,
                         ThresholdCrossingEntityRepository thresholdCrossingEntityRepository, FrequencyTable frequencyTable, List<ThresholdCrossingEntity> thresholdCrossingEntityList) {
        this.recordEntityRepository = recordEntityRepository;
        this.receivedRecordEntityRepository = receivedRecordEntityRepository;
        this.thresholdCrossingEntityRepository = thresholdCrossingEntityRepository;
        this.frequencyTable = frequencyTable;
        this.thresholdCrossingEntityList = thresholdCrossingEntityList;
    }

    public List<ThresholdCrossingEntity> addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp,
                                                   double freqStart, double freqStep, double threshold) {

        thresholdCrossingEntityList.clear();

        frequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);

        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold)
                .build();
        recordEntityRepository.save(newRecord);

        RecordEntity recordEntityFromDb = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        long idFromDB = recordEntityFromDb.getId();

        for (int i = 0; i < dataSize; i++) {
            ReceivedRecordEntity newReceivedRecordEntity = ReceivedRecordEntity.builder()
                    .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB).build();
            receivedRecordEntityRepository.save(newReceivedRecordEntity);

            if (receivedData[i] > threshold) {
                ThresholdCrossingEntity newThresholdCrossingEntity = ThresholdCrossingEntity.builder()
                        .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB)
                        .build();
                thresholdCrossingEntityRepository.save(newThresholdCrossingEntity);

                thresholdCrossingEntityList.add(newThresholdCrossingEntity);
            }
        }
        return new ArrayList<>(thresholdCrossingEntityList);
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
}
