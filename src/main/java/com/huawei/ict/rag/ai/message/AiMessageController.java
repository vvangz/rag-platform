package com.huawei.ict.rag.ai.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.ict.rag.ai.message.dto.AiMessageInput;
import com.huawei.ict.rag.ai.message.dto.AiMessageWrapper;
import com.huawei.ict.rag.ai.session.AiSession;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
@RequestMapping("message")
public class AiMessageController {
    // 历史消息存储
    private final AiMessageChatMemory chatMemory;
    // 大模型
    private final ChatModel chatModel;
    // 向量存储
    private final VectorStore vectorStore;
    // 结构体转换工具
    private final ObjectMapper objectMapper;
    // 消息服务
    private final AiMessageRepository messageRepository;


    @DeleteMapping("history/{sessionId}")
    public void deleteHistory(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
    }

    @SneakyThrows
    @PostMapping(value = "chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestPart String input) {
        AiMessageWrapper aiMessageWrapper = objectMapper.readValue(input, AiMessageWrapper.class);
        return ChatClient.create(chatModel)
                .prompt()
                .user(promptUserSpec -> toPrompt(promptUserSpec, aiMessageWrapper.getMessage()))
                .advisors(advisorSpec -> {
                    // 使用历史消息
                    useChatHistory(advisorSpec, aiMessageWrapper.getMessage().getSessionId());
                    // 使用向量数据库
                    useVectorStore(advisorSpec, aiMessageWrapper.getParams().getEnableVectorStore());
                })
                .stream()
                .chatResponse()
                .map(chatResponse -> ServerSentEvent.builder(toJson(chatResponse))
                        // 和前端监听的事件相对应
                        .event("message")
                        .build());
    }


    public void toPrompt(ChatClient.PromptUserSpec promptUserSpec, AiMessageInput input) {
        // AiMessageInput转成Message
        Message message = AiMessageChatMemory.toSpringAiMessage(input.toEntity());
        if (message instanceof UserMessage userMessage &&
                !CollectionUtils.isEmpty(userMessage.getMedia())) {
            // 用户发送的图片/语言
            Media[] medias = new Media[userMessage.getMedia().size()];
            promptUserSpec.media(userMessage.getMedia().toArray(medias));
        }
        // 用户发送的文本
        promptUserSpec.text(message.getText());
    }

    public void useChatHistory(ChatClient.AdvisorSpec advisorSpec, String sessionId) {
        // 1. 如果需要存储会话和消息到数据库，自己可以实现ChatMemory接口，这里使用自己实现的AiMessageChatMemory，数据库存储。
        // 2. 传入会话id，MessageChatMemoryAdvisor会根据会话id去查找消息。
        // 3. 只需要携带最近10条消息
        // MessageChatMemoryAdvisor会在消息发送给大模型之前，从ChatMemory中获取会话的历史消息，然后一起发送给大模型。
        advisorSpec.advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sessionId).build());
    }

    public void useVectorStore(ChatClient.AdvisorSpec advisorSpec, Boolean enableVectorStore) {
        if (!enableVectorStore) return;
        // question_answer_context是一个占位符，会替换成向量数据库中查询到的文档。QuestionAnswerAdvisor会替换。
        String promptWithContext = """
                {query}
                下面是上下文信息
                ---------------------
                {question_answer_context}
                ---------------------
                给定的上下文和提供的历史信息，而不是事先的知识，回复用户的意见。如果答案不在上下文中，告诉用户你不能回答这个问题。
                """;
        advisorSpec.advisors(QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(new PromptTemplate(promptWithContext))
                .build());
    }

    /**
     * 消息保存
     *
     * @param input 用户发送的消息/AI回复的消息
     */
    @PostMapping
    public void save(@RequestBody AiMessageInput input) {
        messageRepository.save(input.toEntity());
    }

    @SneakyThrows
    public String toJson(ChatResponse response) {
        return objectMapper.writeValueAsString(response);
    }
}
