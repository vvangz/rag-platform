package com.huawei.ict.rag.ai.document;

import com.huawei.ict.rag.ai.splitter.JsonClassMethodSplitter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RequestMapping("document")
@RestController
@AllArgsConstructor
public class DocumentController {
    private final VectorStore vectorStore;

    /**
     * 嵌入文件
     *
     * @param file 待嵌入的文件
     * @return 是否成功
     */
    @SneakyThrows
    @PostMapping("embedding")
    public Boolean embedding(@RequestParam MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        List<Document> splitDocuments;

        //如果为json文件，按照自定义分割器进行处理，其他类型使用Tika和TokenTextSplitter分割
        if (fileName != null && fileName.endsWith(".json") || "application/json".equals(contentType)) {
            // 读取 JSON 文件内容
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            JsonClassMethodSplitter jsonSplitter = new JsonClassMethodSplitter(1000); // 可自定义 chunkSize
            splitDocuments = jsonSplitter.split(json);
        } else {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
            splitDocuments = new TokenTextSplitter().apply(tikaDocumentReader.read());
        }
        // 存入向量数据库，这个过程会自动调用embeddingModel,将文本变成向量再存入。
        vectorStore.add(splitDocuments);
        return true;
    }

}
