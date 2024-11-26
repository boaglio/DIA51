package com.boaglio.dia51;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public  class RagConfiguration {

        private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

        @Value("vectorstore.json")
        private String vectorStoreName;

        private static final String FILES_DIRECTORY = "msxfiles";

        private final EmbeddingModel embeddingModel;

        public RagConfiguration(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public SimpleVectorStore getSimpleVectorStore() {

            var simpleVectorStore = new SimpleVectorStore(embeddingModel);
            var vectorStoreFile = getVectorStoreFile();
            if (vectorStoreFile.exists()) {

                log.info("Usando arquivo do Vector Store");
                simpleVectorStore.load(vectorStoreFile);

            } else {

                long startTime = System.currentTimeMillis();

                log.info("Criando Vector Store, carregando...");
                List<Document> allDocuments = new ArrayList<>();

                try {

                    File baseDir = Paths.get(FILES_DIRECTORY).toFile();
                    if (!baseDir.isDirectory()) {
                        throw new IllegalArgumentException("O caminho fornecido não é um diretório");
                    }

                    Files.walk(baseDir.toPath())
                            .filter(Files::isRegularFile)
                            .forEach(path -> {
                                try {
                                    log.info("Processando arquivo: " + path.getFileName());
                                    var textReader = new TextReader(new FileSystemResource(path.toFile()));
                                    textReader.getCustomMetadata().put("filename", path.getFileName().toString());
                                    var text = textReader.get();
                                    allDocuments.addAll(text);
                                } catch (Exception e) {
                                    log.warn("Falha ao processar arquivo: " + path, e);
                                }
                            });

                } catch (Exception e) {
                    log.error("Erro ao acessar diretório de arquivos: ", e);
                    throw new RuntimeException("Erro ao carregar arquivos do diretório 'files'");
                }

                log.info("Total de AI Documents: %d".formatted(allDocuments.size()));

                var textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(allDocuments);
                simpleVectorStore.add(splitDocuments);

                log.info("Total de Tokens: %d".formatted(splitDocuments.size()));
                simpleVectorStore.save(vectorStoreFile);

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                log.info("Tempo de execução : " + executionTime + " ms");

            }
            return simpleVectorStore;
        }

        private File getVectorStoreFile() {

            var path = Paths.get("src", "main", "resources");
            var absolutePath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
            return new File(absolutePath);

        }

    }