package com.huawei.ict.rag.ai.message.dto;

import lombok.Data;

@Data
public class AiMessageWrapper {
    AiMessageInput message;
    AiMessageParams params;
}
