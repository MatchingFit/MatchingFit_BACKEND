package com.example.matching_fit.global.initalizer.dto;

import com.example.matching_fit.domain.score.entity.Category;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "keyword")
@Getter
@Setter
public class KeywordYamlDto {

    private String yamlPath;

    private Map<String, Map<String, Double>> competencyKeywords;

    private Map<String, Map<String, Double>> technicalKeywords;

    public Map<Category, Map<String, Double>> getTechnicalKeywordsAsEnumMap() {
        return technicalKeywords.entrySet().stream()
                .filter(e -> {
                    try {
                        Category.valueOf(e.getKey());
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .collect(
                        java.util.stream.Collectors.toMap(
                                e -> Category.valueOf(e.getKey()),
                                Map.Entry::getValue
                        )
                );
    }
}

