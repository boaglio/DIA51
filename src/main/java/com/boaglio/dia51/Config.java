package com.boaglio.dia51;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration
public class Config {

    @Value("${spring.ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${spring.ai.ollama.embedding.model}")
    private String model;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean
    public EmbeddingModel embeddingModel(ObservationRegistry observationRegistry) {

        OllamaApi ollamaApi = new OllamaApi(ollamaUrl);
        OllamaOptions ollamaOptions = OllamaOptions.builder().withModel(model).build();
        ModelManagementOptions managementOptions = ModelManagementOptions.builder().build();

        return new OllamaEmbeddingModel(ollamaApi, ollamaOptions, observationRegistry, managementOptions);
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(datasourceUrl)
                .username(datasourceUsername)
                .password(datasourcePassword)
                .build();
    }

    @Bean
    public VectorStore postgresVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        // return new PgVectorStore(jdbcTemplate, embeddingModel);
          return new PgVectorStore(jdbcTemplate, embeddingModel,-1, PgVectorStore.PgDistanceType.COSINE_DISTANCE,false, PgVectorStore.PgIndexType.HNSW,false);
        }

}