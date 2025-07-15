package com.safemeds.safemedsbackend.mappers.user;

import com.safemeds.safemedsbackend.dtos.user.AllergyDTO;
import com.safemeds.safemedsbackend.entities.Allergy;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AllergyMapper {

    Allergy toEntity(AllergyDTO dto);

    AllergyDTO toDTO(Allergy entity);

    List<AllergyDTO> toDTOList(List<Allergy> allergies);
}
