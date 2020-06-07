package com.kdk.MultichannelReceiver.dataPersist;




import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class ThresholdCrossingEntity {

//TODO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false, nullable = false)
    private RecordEntity recordEntity;

    //@NotNull
    @Column(name = "record_id", unique = true)
    private long recordId;



}
