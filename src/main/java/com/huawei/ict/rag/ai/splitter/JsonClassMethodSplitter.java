package com.huawei.ict.rag.ai.splitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonClassMethodSplitter {

    private final int chunkSize;

    public JsonClassMethodSplitter(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public List<Document> split(String jsonContent) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonContent);

        List<Document> documents = new ArrayList<>();
        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON 根节点应为数组");
        }

        for (JsonNode classNode : root) {
            List<JsonNode> methodList = new ArrayList<>();
            JsonNode methodsNode = classNode.get("methods");
            if (methodsNode != null && methodsNode.isArray()) {
                for (JsonNode method : methodsNode) {
                    methodList.add(method);
                }
            }

            // 分割处理
            documents.addAll(splitMethodsRecursive(classNode, methodList));
        }

        return documents;
    }

    private List<Document> splitMethodsRecursive(JsonNode classNode, List<JsonNode> methods) {
        List<Document> result = new ArrayList<>();

        if (methods.isEmpty()) {
            result.add(buildDocument(classNode, methods));
            return result;
        }

        Document doc = buildDocument(classNode, methods);
        if (Objects.requireNonNull(doc.getText()).length() <= chunkSize || methods.size() == 1) {
            result.add(doc);
            return result;
        }

        // 对 methods 分半处理
        int mid = methods.size() / 2;
        List<JsonNode> firstHalf = methods.subList(0, mid);
        List<JsonNode> secondHalf = methods.subList(mid, methods.size());

        result.addAll(splitMethodsRecursive(classNode, new ArrayList<>(firstHalf)));
        result.addAll(splitMethodsRecursive(classNode, new ArrayList<>(secondHalf)));

        return result;
    }

    private Document buildDocument(JsonNode classNode, List<JsonNode> methodsSubset) {
        ObjectMapper mapper = new ObjectMapper();

        // 构造新的类结构（浅拷贝原 classNode 中非 methods 的字段）
        ObjectNode newClassNode = mapper.createObjectNode();
        newClassNode.put("class", classNode.path("class").asText());

        if (classNode.has("description")) {
            newClassNode.put("description", classNode.get("description").asText());
        }

        // 添加分割后的 methods 数组
        ArrayNode newMethodsArray = mapper.createArrayNode();
        for (JsonNode method : methodsSubset) {
            newMethodsArray.add(method);
        }
        newClassNode.set("methods", newMethodsArray);

        try {
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newClassNode);
            return new Document(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("构建 Document JSON 失败", e);
        }
    }
}
