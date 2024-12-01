package com.boaglio.dia51;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    private static final String FILES_DIRECTORY = "msxfiles";

    @Autowired
    private VectorStore vectorStore;

    public void loadVectorDatabase() {

            var startTime = System.currentTimeMillis();

            log.info("Carregando Vector Database a partir de arquivos...");

            try {
                var baseDir = Paths.get(FILES_DIRECTORY).toFile();
                if (!baseDir.isDirectory()) {
                    throw new IllegalArgumentException("O caminho fornecido não é um diretório");
                }

                var totalFiles = Files.walk(baseDir.toPath())
                        .filter(Files::isRegularFile)
                        .count();

                log.info("Total de arquivos a serem processados: {}", totalFiles);

                var fileCounter = new AtomicInteger(1);
                Files.walk(baseDir.toPath())
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::getFileName))
                        .forEach(path -> {
                            try {
                                log.info("Processando arquivo %d/%d : %s".formatted(fileCounter.getAndIncrement(),totalFiles, path.getFileName()));

                                var textReader = new TextReader(new FileSystemResource(path.toFile()));
                                textReader.getCustomMetadata().put("filename", path.getFileName().toString());
                                var text = textReader.get();
                                List<Document> allDocuments = new ArrayList<>(text);
                                var textSplitter = new TokenTextSplitter();
                                List<org.springframework.ai.document.Document> splitDocuments = textSplitter.apply(allDocuments);

                                log.info("Gravando : {}", splitDocuments.size());

                                vectorStore.add(splitDocuments);

                                Files.delete(path);
                                log.info("Arquivo {} processado e removido com sucesso.", path.getFileName());

                            } catch (Exception e) {
                                log.warn("Falha ao processar arquivo: " + path, e);
                            }
                        });

            } catch (Exception e) {
                log.error("Erro ao acessar diretório de arquivos: ", e);
                throw new RuntimeException("Erro ao carregar arquivos do diretório 'files'");
            }

            var endTime = System.currentTimeMillis();
            var executionTime = endTime - startTime;
            log.info("Tempo de execução : {} ms", executionTime);
        }

}
