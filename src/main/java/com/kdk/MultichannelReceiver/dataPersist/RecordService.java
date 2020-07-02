package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecordService {

//    @Autowired
//    public RecordService() {
//        System.out.println("RecordService konstruktor");
//    }
//
//    public void addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
//                          double freqStep, double threshold) {
//        System.out.println("Dodaje record w RecordService");
//        System.out.println("receivedData lenght = " + receivedData.length);
//        System.out.println("Data size = " + dataSize);
//        System.out.println("Seq Numer = " + seqNumber);
//        System.out.println("Time Stamp = " + timeStamp);
//
//        System.out.println("FreQ start = " + freqStart);
//        System.out.println("FreQ step = " + freqStep);
//        System.out.println("Threshold = " + threshold);
//
//        double frequencyTable [] = new double[dataSize];
//        frequencyTable[0] = freqStart;
//        for (int i = 1; i < dataSize; i++) {
//            frequencyTable[i] = frequencyTable[i - 1] + freqStep;
//        }
//        System.out.println("frequencyTable lenght = " + frequencyTable.length);
//
//    }

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
        System.out.println("zapisano record do DB");

        RecordEntity recordEntityFromDb = recordEntityRepository.findByTimeStamp(timeStamp);
        long idFromDB = recordEntityFromDb.getId();

        for (int i = 0; i < dataSize; i++) {
            ReceivedRecordEntity newReceivedRecordEntity = ReceivedRecordEntity.builder()
                    .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB).build();
            receivedRecordEntityRepository.save(newReceivedRecordEntity);
//            System.out.println("zapisano  ReceivedRecord do DB: Częstotliwość: " + frequencyTable.getFrequency(i) + " Poziom Sygnału: " + receivedData[i]);

            if (receivedData[i] > threshold) {
                ThresholdCrossingEntity newThresholdCrossingEntity = ThresholdCrossingEntity.builder()
                        .frequency(frequencyTable.getFrequency(i)).signalLevel(receivedData[i]).recordId(idFromDB)
                        .build();
                thresholdCrossingEntityRepository.save(newThresholdCrossingEntity);
//                System.out.println("zapisano  ThresholdCrossing do DB: Częstotliwość: " + frequencyTable.getFrequency(i) + " Poziom Sygnału: " + receivedData[i]);

                thresholdCrossingEntityList.add(newThresholdCrossingEntity);
            }
        }
        return new ArrayList<>(thresholdCrossingEntityList);
    }

}
