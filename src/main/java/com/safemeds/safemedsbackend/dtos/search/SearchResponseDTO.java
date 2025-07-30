package com.safemeds.safemedsbackend.dtos.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private List<SearchSuggestionDTO> suggestions;
    private String query;
    private String category;
    private boolean hasMore;
    private String source;
}