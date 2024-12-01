package com.boaglio.dia51;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@Tag(name = "DIA51")
@RestController
public class DIA51Controller {

    private static final Logger log = LoggerFactory.getLogger(DIA51Controller.class);

    @Value("classpath:/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @Autowired
    private  ChatClient.Builder chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RagConfiguration ragConfiguration;

    @GetMapping("/api/dia51/carregue-os-arquivos")
    public String load(){

        ragConfiguration.loadVectorDatabase();

        return "Ok";
    }

    @GetMapping("/api/dia51/pergunte")
    public String generate(@RequestParam(value = "message", defaultValue = "O que é MSX ?") String message) throws IOException {

        var similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(2)
        );

        var contentList = similarDocuments.stream().map(Document::getContent).toList();
        var promptTemplate = new PromptTemplate(ragPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
        var promptParameters = new HashMap<String,Object>();
        promptParameters.put("input",message);
        promptParameters.put("documents",String.join("\n",contentList));
        var prompt = promptTemplate.create(promptParameters);

        log.info(prompt.getContents());

        return chatClient.build().prompt(prompt).call().content();
    }


    @GetMapping("/api/dia51/pergunte-sem-msxfiles")
    public String generateOllamaOnly(@RequestParam(value = "message", defaultValue = "O que é MSX ?") String message) throws IOException {
        return chatClient.build().prompt(message).call().content();
    }

}