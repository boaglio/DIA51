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
    public String generate(@RequestParam(value = "message", defaultValue = "liste alguns emuladores") String message) throws IOException {

        var similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(25)
        );

        var contentList = similarDocuments.stream()
                .filter(doc -> doc.getContent() != null)
                .map(doc -> {
                    String filteredContent =  cleanMessage(doc.getContent());
                    return new Document(filteredContent);
                })
                .toList();

        log.info("Documentos Recuperados para [%s]:".formatted(message));
        contentList.forEach(doc -> log.info(doc.getContent()));

        log.info("Total: %d documentos".formatted(contentList.size()));

        var promptTemplate = new PromptTemplate(ragPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
        var promptParameters = new HashMap<String,Object>();
        promptParameters.put("input",message);
        promptParameters.put("documents",contentList);
        var prompt = promptTemplate.create(promptParameters);

        return chatClient
                .build()
                .prompt(prompt)
                .call()
                .content();
    }


    @GetMapping("/api/dia51/pergunte-sem-msxfiles")
    public String generateOllamaOnly(@RequestParam(value = "message", defaultValue = "liste alguns emuladores") String message) throws IOException {
        return chatClient.build().prompt(message).call().content();
    }

    public   String cleanMessage(String initialMessage) {
        if (initialMessage == null || initialMessage.isEmpty()) {
            return initialMessage;
        }

        String[] linhas = initialMessage.split("\n");

        StringBuilder mensagemFiltrada = new StringBuilder();

        for (String linha : linhas) {
            if  (!linha.contains("yahoo.com")
                    && !linha.contains("gmail.com")
                    && !linha.contains("hotmail.com")
                    && !linha.contains("spaceymail-a1.g.dreamhost.com")
                    && !linha.contains("8@terra.com.br")
                    && !linha.contains("mailman")
                    && !linha.contains("pipermail")
                    && !linha.contains("Lista de discussao MSXBR")
                    && !linha.contains("Lista brasileira de disc")
                    && !linha.contains("listas.msx.org.br")
                    && !linha.contains("@finalboss.com>")
                    && !linha.contains("@AthlonDual>")
                    && !linha.contains("@tababook>")
                    && !linha.contains("@phx.gbl>")
                    && !linha.contains("Administrador: ")
                    && !linha.contains("References: <")
                    && !linha.contains("Message-ID:")
                    && !linha.contains("In-Reply-To:")
                    && !linha.contains("From:")
                    && !linha.contains("Sent:")
                    && !linha.contains("@caetano.eng.br")
                    && !linha.contains("=?iso-8859-1")
                    && !linha.contains("Um anexo em HTML")
                    && !linha.contains("message truncated")
                    && !linha.contains("--------------")
                    && !linha.contains("Enviado via ")
                    && !linha.contains("Enviado do Gmail")
                    && !linha.contains("Enviada em: ")
                    && !linha.contains("Date: ")
                    && !linha.contains("Subject: ")
                    && !linha.contains("Assunto: ")
                    && !linha.contains(">>")
                    && !linha.contains("> >")
                    && !linha.contains("This message has been")
                    && !linha.contains("Linux User ")
                    && !linha.isBlank()
            )
            {
                mensagemFiltrada.append(linha).append("\n");
            }
        }

        if (!mensagemFiltrada.isEmpty()) {
            mensagemFiltrada.setLength(mensagemFiltrada.length() - 1);
        }

        return mensagemFiltrada.toString();
    }

}