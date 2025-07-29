package com.huawei.ict.rag.ai.evolinstruct;



import com.huawei.ict.rag.ai.evolinstruct.service.EvolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("evol")
@RestController
@AllArgsConstructor
@Slf4j
public class EvolController {
    private final EvolService evolService;

    @PostMapping
    public void evol() throws IOException {
        evolService.evolveInstructions("src/main/resources/evol/data/alpaca_data_cleaned.json","src/main/resources/evol/data/alpaca_data_evoled.json");
    }
}
