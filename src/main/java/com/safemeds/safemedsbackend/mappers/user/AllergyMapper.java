
package com.safemeds.safemedsbackend.mappers.user;

import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.AllergyResponseDTO;
import com.safemeds.safemedsbackend.entities.Allergy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AllergyMapper {

    Allergy toEntity(AllergyRequestDTO dto);

    AllergyResponseDTO toResponseDTO(Allergy entity);

    AllergyRequestDTO toRequestDTO(Allergy entity);

    List<AllergyResponseDTO> toResponseDTOList(List<Allergy> allergies);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Allergy allergy, AllergyRequestDTO dto);
}