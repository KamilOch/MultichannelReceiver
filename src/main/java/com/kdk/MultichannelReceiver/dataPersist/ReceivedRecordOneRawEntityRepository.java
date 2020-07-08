package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "received_records_one_raw", collectionResourceRel = "received_records_one_raw")
public interface ReceivedRecordOneRawEntityRepository extends PagingAndSortingRepository<ReceivedRecordOneRowEntity, Long> {

    ReceivedRecordOneRowEntity findByRecordId(long recordId);

}
