package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordService {


    private final RecordEntityRepository recordEntityRepository;
    private final ReceivedRecordEntityRepository receivedRecordEntityRepository;
    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository;



    @Autowired
    public RecordService(RecordEntityRepository recordEntityRepository, ReceivedRecordEntityRepository receivedRecordEntityRepository, ThresholdCrossingEntityRepository thresholdCrossingEntityRepository) {
        this.recordEntityRepository = recordEntityRepository;
        this.receivedRecordEntityRepository = receivedRecordEntityRepository;
        this.thresholdCrossingEntityRepository = thresholdCrossingEntityRepository;
    }



    public void addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
                          double freqStep, double threshold) {
        //TODO zrobic obiekty i zapisac do bazy danych .........
    }
}
