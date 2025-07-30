package com.safemeds.safemedsbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "medical.apis")
@Data
public class MedicalApiConfig {
    private OpenFda openFda = new OpenFda();
    private Icd10 icd10 = new Icd10();
    private RxNorm rxNorm = new RxNorm();
    private Fhir fhir = new Fhir();

    @Data
    public static class OpenFda {
        private String apiKey = "hAvbcWRfZGYGTn9XHLwCAy1flFaupcX5bjDhwWNL";
        private String baseUrl = "https://api.fda.gov";
    }

    @Data
    public static class Icd10 {
        private String clientId = "4335a916-5a72-4330-8f78-3d60b37f37ae_ea0f3e91-0751-4721-9db0-bce81f710e81";
        private String clientSecret = "/ZwsTSm2iimVwJ4jvTk0EkU3DhOO1RUW9I6hAnYsjM4=";
        private String baseUrl = "https://id.who.int/icd";
    }

    @Data
    public static class RxNorm {
        private String baseUrl = "https://rxnav.nlm.nih.gov/REST";
    }

    @Data
    public static class Fhir {
        private String baseUrl = "https://hapi.fhir.org/baseR4";
        private String allergyCodeSystem = "http://snomed.info/sct";
        private String substanceCodeSystem = "http://www.nlm.nih.gov/research/umls/rxnorm";
    }
}