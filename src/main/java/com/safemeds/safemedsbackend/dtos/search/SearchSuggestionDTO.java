package com.safemeds.safemedsbackend.dtos.search;

import lombok.*;


@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionDTO {
    private String value;
    private String displayName;
    private String description;
    private String source;
    private String category;
    private String code;
}