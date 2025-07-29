package com.huawei.ict.rag.ai.evolinstruct.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.huawei.ict.rag.ai.evolinstruct.model.InstructionData;
import com.huawei.ict.rag.ai.evolinstruct.prompts.BreadthPrompts;
import com.huawei.ict.rag.ai.evolinstruct.prompts.DepthPrompts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class EvolService {
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;
    private final Executor asyncExecutor = Executors.newFixedThreadPool(20); // 自定义线程池

    public void evolveInstructions(String inputFilePath, String outputFilePath) throws IOException {
        // 读取JSON文件
        InstructionData[] allObjs = objectMapper.readValue(new File(inputFilePath), InstructionData[].class);
        List<CompletableFuture<InstructionData>> futures = new ArrayList<>();

        for (InstructionData curObj : allObjs) {
            // 提交异步任务
            CompletableFuture<InstructionData> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String instruction = curObj.getInstruction().strip() + "\n" + curObj.getInput().strip();
                    String evolPrompt = generateEvolPrompt(instruction); // 生成演化提示

                    // 流式调用生成新指令
                    ChatClient chatClient = ChatClient.create(chatModel);
                    StringBuilder evolInstruction = new StringBuilder();
                    chatClient.prompt(evolPrompt)
                            .stream()
                            .chatResponse()
                            .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                            .doOnNext(evolInstruction::append) // 流式拼接响应
                            .blockLast(); // 等待流结束

                    // 流式调用获取答案
                    StringBuilder answer = new StringBuilder();
                    chatClient.prompt(evolInstruction.toString())
                            .stream()
                            .chatResponse()
                            .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                            .doOnNext(answer::append)
                            .blockLast();

                    // 返回新对象
                    InstructionData evolObj = new InstructionData();
                    evolObj.setInstruction(evolInstruction.toString());
                    evolObj.setOutput(answer.toString());
                    return evolObj;
                } catch (Exception e) {
                    log.error("Processing failed for object: {}", curObj, e);
                    return null; // 失败返回null，后续过滤
                }
            }, asyncExecutor);
            futures.add(future);
        }

        // 等待所有任务完成并收集结果
        List<InstructionData> evolObjs = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // 过滤失败任务
                .collect(Collectors.toList());

        // 写入文件
        objectMapper.writeValue(new File(outputFilePath), evolObjs);
    }

    private String generateEvolPrompt(String instruction) {
        double rand = ThreadLocalRandom.current().nextDouble(); // 线程安全随机数
        return (rand < 0.8) ?
                DepthPrompts.createRandomDepthPrompt(instruction) :
                BreadthPrompts.createBreadthPrompt(instruction);
    }
}
