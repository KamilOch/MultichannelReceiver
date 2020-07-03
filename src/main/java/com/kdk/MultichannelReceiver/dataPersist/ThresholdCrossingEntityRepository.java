package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "threshold_records", collectionResourceRel = "threshold_records")
public interface ThresholdCrossingEntityRepository extends PagingAndSortingRepository<ThresholdCrossingEntity, Long> {

    List<ThresholdCrossingEntity> findByRecordId(long recordId);
}
