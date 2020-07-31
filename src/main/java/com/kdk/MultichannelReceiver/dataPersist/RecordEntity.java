package com.kdk.MultichannelReceiver.dataPersist;

import lombok.*;

import javax.persistence.*;
import java.util.Set;
/***
 * Klasa pojedynczego pomiaru (bez danych pomiarowych,
 * dane pomiarowe są reprezentowane przez Klase @see ReceivedRecordOneRowEntity).
 * Obiekty tej klasy reprezentują sposób zapisu do bazy danych
 * @author Kamil Ochnik
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "records")
@SequenceGenerator(name = "RECORD_SEQ", sequenceName = "record_id_seq", allocationSize = 1)
public class RecordEntity {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECORD_SEQ")
    @Column(name = "id")
    private long id;

    @Column(name = "time_stamp")
    private double timeStamp;

    @Column(name = "seq_number")
    private int seqNumber;

    @Column(name = "threshold")
    private double threshold;

    //TODO chyba ThresholdCrossingOneRawEntity
    @OneToMany(mappedBy = "recordEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Set<ThresholdCrossingEntity> thresholdCrossings;

    //TODO chyba ReceivedRecordOneRowEntity
    @OneToMany(mappedBy = "recordEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Set<ReceivedRecordEntity> receivedRecords;

}
