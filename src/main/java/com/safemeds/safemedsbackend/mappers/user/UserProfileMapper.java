package com.safemeds.safemedsbackend.mappers.user;


import com.safemeds.safemedsbackend.dtos.user.*;
import com.safemeds.safemedsbackend.entities.UserProfile;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = { AllergyMapper.class })
public interface UserProfileMapper {

    UserProfile toEntity(UserProfileRequestDTO dto);

    UserProfileResponseDTO toResponseDTO(UserProfile entity);

    List<UserProfileResponseDTO> toResponseDTOList(List<UserProfile> entities);
}
