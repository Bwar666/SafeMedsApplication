package com.safemeds.safemedsbackend.mappers.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiWarningDTO;
import com.safemeds.safemedsbackend.entities.AiWarning;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiEvaluationMapper {

    AiWarning toEntity(AiWarningDTO dto);

    AiWarningDTO toDTO(AiWarning entity);

    List<AiWarningDTO> toDTOList(List<AiWarning> evaluations);
}
