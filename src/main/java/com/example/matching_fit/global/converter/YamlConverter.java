package com.example.matching_fit.global.converter;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConverter {

    public static Map<String, Map<String, Double>> castNestedMap(Object input) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        if (input instanceof Map<?, ?> outerMap) {
            for (Map.Entry<?, ?> outerEntry : outerMap.entrySet()) {
                String outerKey = outerEntry.getKey().toString();
                Map<String, Double> innerResult = new LinkedHashMap<>();

                if (outerEntry.getValue() instanceof Map<?, ?> innerMap) {
                    for (Map.Entry<?, ?> innerEntry : innerMap.entrySet()) {
                        String innerKey = innerEntry.getKey().toString();
                        Object value = innerEntry.getValue();

                        // float 또는 int → double로 변환 처리
                        double doubleValue;
                        if (value instanceof Number number) {
                            doubleValue = number.doubleValue();
                        } else {
                            continue; // skip invalid value
                        }

                        innerResult.put(innerKey, doubleValue);
                    }
                }

                result.put(outerKey, innerResult);
            }
        }

        return result;
    }
}
