package dataPersist;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataPersistConfig {

    private RecordService recordService;

    @Bean
    public RecordService recordService() {
        return new RecordService();
    }

    @Bean
    public DataPersistController dataPersistController (RecordService recordService) {
        return new DataPersistController(recordService);
    }


}
