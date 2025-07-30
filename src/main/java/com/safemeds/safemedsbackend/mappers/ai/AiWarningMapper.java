package com.safemeds.safemedsbackend.mappers.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiWarningResponseDTO;
import com.safemeds.safemedsbackend.entities.AiWarning;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {AiEvaluationResultMapper.class}
)
public interface AiWarningMapper {

    @Mapping(source = "medicine.name", target = "medicineName")
    @Mapping(source = "allergy.name", target = "allergyName")
    AiWarningResponseDTO toDto(AiWarning entity);
}