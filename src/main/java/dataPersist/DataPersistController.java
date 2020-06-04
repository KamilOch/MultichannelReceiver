package dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DataPersistController {

    private final RecordService recordService;

    @Autowired
    public DataPersistController(RecordService recordService) {
        this.recordService = recordService;
    }
}
