package com.huawei.ict.rag.ai.evolinstruct.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.huawei.ict.rag.ai.evolinstruct.model.InstructionData;
import com.huawei.ict.rag.ai.evolinstruct.prompts.BreadthPrompts;
import com.huawei.ict.rag.ai.evolinstruct.prompts.DepthPrompts;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
@AllArgsConstructor
public class EvolService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatModel chatModel;
    private final Random random = new Random();

    // 主流程方法
    public void evolveInstructions(String inputFilePath, String outputFilePath) throws IOException {
        // 读取JSON文件
        String jsonContent = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        InstructionData[] allObjs = objectMapper.readValue(jsonContent, InstructionData[].class);

        List<InstructionData> evolObjs = new ArrayList<>();

        for (InstructionData curObj : allObjs) {
            // 合并instruction和input
            String instruction = curObj.getInstruction().strip() + "\n" + curObj.getInput().strip();

            // 80%概率深度演化，20%概率广度演化
            String evolPrompt;
            if (random.nextDouble() < 0.8) {
                // 深度演化（随机选择一种）
                evolPrompt = DepthPrompts.createRandomDepthPrompt(instruction);
            } else {
                // 广度演化
                evolPrompt = BreadthPrompts.createBreadthPrompt(instruction);
            }
            ChatClient chatClient = ChatClient.create(chatModel);
            // 调用ChatGPT生成新指令
            String evolInstruction = chatClient
                    .prompt(evolPrompt)
                    .call()
                    .content();

            // 获取新指令的答案
            String answer = chatClient
                    .prompt(evolInstruction)
                    .call()
                    .content();
            ;

            // 创建新对象
            InstructionData evolObj = new InstructionData();
            evolObj.setInstruction(evolInstruction);
            evolObj.setOutput(answer);
            evolObjs.add(evolObj);
        }

        // 写入新JSON文件
        objectMapper.writeValue(new File(outputFilePath), evolObjs);
    }
}
