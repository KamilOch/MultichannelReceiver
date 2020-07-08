package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataPersistController {

    private final RecordService recordService;

    @Autowired
    public DataPersistController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/recordsList")
    public List<RecordEntity> recordsList() {
        return recordService.getAllRecords();
    }

    @GetMapping("/deleteAllRecords")
    public void deleteAllRecords() {
        recordService.deleteAllRecords();
    }

    @GetMapping("/getRecordsBySeqNumber/{seqNum}")
    public List<RecordEntity> getRecordsBySeqNumber(@PathVariable int seqNum) {
        return recordService.getRecordsBySeqNumber(seqNum);
    }

    @GetMapping("/deleteRecordsBySeqNumber/{seqNum}")
    public void deleteRecordsBySeqNumber(@PathVariable int seqNum) {
        recordService.deleteRecordsBySeqNumber(seqNum);
    }

    @GetMapping("/findRecordByTimeStamp/{timeStamp}")
    public RecordEntity findRecordByTimeStamp(@PathVariable double timeStamp) {
        return recordService.findRecordByTimeStamp(timeStamp);
    }

    @GetMapping("/deleteRecordByTimeStamp/{timeStamp}")
    public void deleteRecordByTimeStamp (@PathVariable double timeStamp){
        recordService.deleteRecordByTimeStamp(timeStamp);
    }

    @GetMapping("/getReceivedRecordByRecordId/{recordId}")
    public List<ReceivedRecordEntity> getReceivedRecordByRecordId(@PathVariable long recordId) {
       return recordService.getReceivedRecordByRecordId(recordId);
    }

    @GetMapping("/getReceivedRecordOneRawByRecordId/{recordId}")
    public ReceivedRecordOneRowEntity getReceivedRecordOneRawByRecordId(@PathVariable long recordId) {
        return recordService.getReceivedRecordOneRawByRecordId(recordId);
    }

    @GetMapping("/getThresholdCrossingRecordByRecordId/{recordId}")
    public List<ThresholdCrossingEntity> getThresholdCrossingRecordByRecordId(@PathVariable long recordId) {
        return recordService.getThresholdCrossingRecordByRecordId(recordId);
    }

    @GetMapping("/getThresholdCrossingRecordOneRawByRecordId/{recordId}")
    public ThresholdCrossingOneRawEntity getThresholdCrossingRecordOneRawByRecordId(@PathVariable long recordId) {
        return recordService.getThresholdCrossingRecordOneRawByRecordId(recordId);
    }
}

