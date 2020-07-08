package com.kdk.MultichannelReceiver.dataPersist;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "received_records_one_raw")
@SequenceGenerator(name = "REC_RECORD_ONE_RAW_SEQ", sequenceName = "received_records_one_raw_id_seq", allocationSize = 1)
public class ReceivedRecordOneRowEntity {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REC_RECORD_ONE_RAW_SEQ")
    @Column(name = "id")
    private long id;

    @Column(name= "frequency")
    private String frequencyList;

    @Column(name= "signal_level")
    private String signalLevelList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false, nullable = false)
    private RecordEntity recordEntity;

    @NotNull
    @Column(name = "record_id")
    private long recordId;
}
