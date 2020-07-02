package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        RecordEntity recordEntityFromDb = recordEntityRepository.findByTimeStamp(timeStamp);
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

}
