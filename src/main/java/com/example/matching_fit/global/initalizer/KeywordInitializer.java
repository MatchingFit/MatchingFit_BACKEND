package com.example.matching_fit.global.initalizer;

import com.example.matching_fit.global.initalizer.service.KeywordInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordInitializer {

    private final KeywordInitService keywordInitService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        keywordInitService.initializeAllKeywords();

    }
}
