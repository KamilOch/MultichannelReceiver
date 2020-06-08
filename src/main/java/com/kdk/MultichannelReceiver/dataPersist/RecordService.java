package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordService {

    @Autowired
    public RecordService() {
        System.out.println("RecordService konstruktor");
    }

    public void addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
                          double freqStep, double threshold) {
        System.out.println("Dodaje record w RecordService");
        System.out.println("Data size = " + dataSize);
        System.out.println("Seq Numer = " + seqNumber);
        System.out.println("Time Stamp = " + timeStamp);

        System.out.println("FreQ start = " + freqStart);
        System.out.println("FreQ step = " + freqStep);
        System.out.println("Threshold = " + threshold);

    }


//    private final RecordEntityRepository recordEntityRepository;
//    private final ReceivedRecordEntityRepository receivedRecordEntityRepository;
//    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository;
//
//
//    @Autowired
//    public RecordService(RecordEntityRepository recordEntityRepository, ReceivedRecordEntityRepository receivedRecordEntityRepository, ThresholdCrossingEntityRepository thresholdCrossingEntityRepository) {
//        this.recordEntityRepository = recordEntityRepository;
//        this.receivedRecordEntityRepository = receivedRecordEntityRepository;
//        this.thresholdCrossingEntityRepository = thresholdCrossingEntityRepository;
//    }
//
//    public void addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
//                          double freqStep, double threshold) {
//
//        FrequencyTable frequencyTable = new FrequencyTable(dataSize, freqStart, freqStep);
//
//        RecordEntity newRecord = RecordEntity.builder().timeStamp(timeStamp).seqNumber(seqNumber).threshold(threshold).build();
//        recordEntityRepository.save(newRecord);
//
//        for (int i = 0; i < dataSize; i++) {
//            ReceivedRecordEntity newReceivedRecordEntity = ReceivedRecordEntity.builder().frequency(frequencyTable.frequency[i]).signalLevel(receivedData[i]).recordId(recordEntityRepository.findByTimeStamp(timeStamp).getId()).build();
//            receivedRecordEntityRepository.save(newReceivedRecordEntity);
//
//            if (receivedData[i] > threshold) {
//                ThresholdCrossingEntity newThresholdCrossingEntity = ThresholdCrossingEntity.builder().frequency(frequencyTable.frequency[i]).signalLevel(receivedData[i]).recordId(recordEntityRepository.findByTimeStamp(timeStamp).getId()).build();
//                thresholdCrossingEntityRepository.save(newThresholdCrossingEntity);
//            }
//        }
//    }

}
