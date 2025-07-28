package com.example.matching_fit.global.initalizer.config;

import com.example.matching_fit.global.initalizer.dto.KeywordYamlDto;
import com.example.matching_fit.global.converter.YamlConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Configuration
public class KeywordYamlConfig {

    @Value("${keyword.yaml-path}")
    private Resource yamlResource;

    @Bean
    public KeywordYamlDto keywordYamlDto() {
        try (InputStream is = yamlResource.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(is);

            KeywordYamlDto dto = new KeywordYamlDto();

            dto.setCompetencyKeywords(YamlConverter.castNestedMap(data.get("competency_keywords")));
            dto.setTechnicalKeywords(YamlConverter.castNestedMap(data.get("technical_keywords")));

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("키워드 YAML 파일 로딩 실패", e);
        }
    }
}
