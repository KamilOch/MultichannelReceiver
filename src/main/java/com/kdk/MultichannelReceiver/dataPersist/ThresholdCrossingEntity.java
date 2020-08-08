package com.kdk.MultichannelReceiver.dataPersist;


import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
/***
 * Klasa pojedynczego rekordu który przekroczył próg detekcji.
 * Obiekty tej klasy reprezentują sposób zapisu do bazy danych
 * @author Kamil Ochnik
 * @deprecated z powodu niskiej wydajności Klasa zastąpiona przez Klase @see ThresholdCrossingOneRawEntity
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "threshold_records")
@SequenceGenerator(name = "THR_RECORD_SEQ", sequenceName = "threshold_records_id_seq", allocationSize = 1)
public class ThresholdCrossingEntity {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "THR_RECORD_SEQ")
    @Column(name = "id")
    private long id;

    @Column(name = "frequency")
    private double frequency;

    @Column(name = "signal_level")
    private double signalLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false, nullable = false)
    private RecordEntity recordEntity;

    @NotNull
    @Column(name = "record_id")
    private long recordId;


}
