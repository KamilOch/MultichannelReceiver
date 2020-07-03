package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "records", collectionResourceRel = "records")
public interface RecordEntityRepository extends PagingAndSortingRepository<RecordEntity, Long> {

    Optional<RecordEntity> findByTimeStamp(double timeStamp);

    List<RecordEntity> findAll();

    List<RecordEntity> findBySeqNumber(int seqNumber);

}
