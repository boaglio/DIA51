package com.boaglio.dia51;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Tag(name = "DIA51")
@RestController
public class DIA51Controller {

    private final ChatClient chatClient;

    private static final Logger log = LoggerFactory.getLogger(DIA51Controller.class);

    @Value("classpath:/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    private final RagConfiguration ragConfiguration;

    public DIA51Controller(ChatClient.Builder builder, RagConfiguration ragConfiguration) {
        this.ragConfiguration = ragConfiguration;
        this.chatClient = builder.build();
    }

    @GetMapping("/api/dia51/carregue-os-arquivos")
    public String load(){

        ragConfiguration.getSimpleVectorStore();

        return "Ok";
    }

    @GetMapping("/api/dia51/pergunte")
    public String generate(@RequestParam(value = "message", defaultValue = "O que é MSX ?") String message) throws IOException {

        List<Document> similarDocuments = ragConfiguration.getSimpleVectorStore().similaritySearch(SearchRequest.query(message).withTopK(2));
        List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
        var promptParameters = new HashMap<String,Object>();
        promptParameters.put("input",message);
        promptParameters.put("documents",String.join("\n",contentList));
        var prompt = promptTemplate.create(promptParameters);

        log.info(prompt.getContents());

        return chatClient.prompt(prompt).call().content();
    }



}