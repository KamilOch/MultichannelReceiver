package dataPersist;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "records", collectionResourceRel = "records")
public interface RecordEntityRepository extends PagingAndSortingRepository<RecordEntity, Long> {
}
