package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/***
 * Interfejs pojedynczego rekordu.
 * @author Kamil Ochnik
 * @deprecated z powodu niskiej wydajności Interfejs zastąpiona przez Interfejs @see ReceivedRecordOneRawEntityRepository
 */
@RepositoryRestResource(path = "received_records", collectionResourceRel = "received_records")
public interface ReceivedRecordEntityRepository extends PagingAndSortingRepository<ReceivedRecordEntity, Long> {

    List<ReceivedRecordEntity> findByRecordId(long recordId);

}
