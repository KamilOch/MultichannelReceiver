package com.kdk.MultichannelReceiver.config;

import com.kdk.MultichannelReceiver.dataPersist.RecordEntityRepository;
import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DataPersistConfig {

    private RecordEntityRepository recordEntityRepository;

    @Bean(name = "stringRecordService")
    @Scope("prototype")
    public RecordService recordService() {
        return new RecordService(recordEntityRepository);
    }


}
