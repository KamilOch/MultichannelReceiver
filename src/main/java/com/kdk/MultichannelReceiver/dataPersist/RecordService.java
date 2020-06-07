package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordService {


    private final RecordEntityRepository recordEntityRepository;

    @Autowired
    public RecordService(RecordEntityRepository recordEntityRepository) {
        this.recordEntityRepository = recordEntityRepository;
    }



    public void addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
                          double freqStep, double threshold) {
        //TODO zrobic obiekty i zapisac do bazy danych .........
    }
}
