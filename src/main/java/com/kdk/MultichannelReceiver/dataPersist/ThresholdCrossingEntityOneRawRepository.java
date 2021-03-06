package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
/***
 * Interfejs pojedynczego rekordu który przekroczył próg detekcji.
 * @author Kamil Ochnik
 */
@RepositoryRestResource(path = "threshold_records_one_raw", collectionResourceRel = "threshold_records_one_raw")
public interface ThresholdCrossingEntityOneRawRepository extends PagingAndSortingRepository<ThresholdCrossingOneRawEntity, Long> {

    ThresholdCrossingOneRawEntity findByRecordId(long recordId);

}
