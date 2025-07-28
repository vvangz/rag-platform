package com.huawei.ict.rag.ai.evolinstruct.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class InstructionData {
    @JsonProperty("instruction")
    private String instruction;

    @JsonProperty("input")
    private String input;

    @JsonProperty("output")
    private String output;
}
