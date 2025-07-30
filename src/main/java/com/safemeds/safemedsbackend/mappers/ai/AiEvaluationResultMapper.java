package com.safemeds.safemedsbackend.mappers.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiEvaluationResponseDTO;
import com.safemeds.safemedsbackend.entities.AiEvaluationResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AiEvaluationResultMapper {

    AiEvaluationResultMapper INSTANCE = Mappers.getMapper(AiEvaluationResultMapper.class);

    AiEvaluationResponseDTO toDto(AiEvaluationResult entity);

    AiEvaluationResult toEntity(AiEvaluationResponseDTO dto);
}
