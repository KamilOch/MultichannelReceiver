package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
/***
 * Interfejs pojedynczego rekordu który przekroczył próg detekcji.
 * @author Kamil Ochnik
 * @deprecated z powodu niskiej wydajności Interfejs zastąpiona przez Interfejs @see ThresholdCrossingEntityOneRawRepository
 */
@RepositoryRestResource(path = "threshold_records", collectionResourceRel = "threshold_records")
public interface ThresholdCrossingEntityRepository extends PagingAndSortingRepository<ThresholdCrossingEntity, Long> {

    List<ThresholdCrossingEntity> findByRecordId(long recordId);
}
