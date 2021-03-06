package com.kdk.MultichannelReceiver.dataPersist;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
/***
 * Klasa pojedynczego rekordu. Obiekty tej klasy reprezentują sposób zapisu do bazy danych
 * @author Kamil Ochnik
 * @deprecated z powodu niskiej wydajności Klasa zastąpiona przez Klase @see ReceivedRecordOneRowEntity
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "received_records")
@SequenceGenerator(name = "REC_RECORD_SEQ", sequenceName = "received_records_id_seq", allocationSize = 1)
public class ReceivedRecordEntity {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REC_RECORD_SEQ")
    @Column(name = "id")
    private long id;

    @Column(name= "frequency")
    private double frequency;

    @Column(name= "signal_level")
    private double signalLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false, nullable = false)
    private RecordEntity recordEntity;

    @NotNull
    @Column(name = "record_id")
    private long recordId;
}
