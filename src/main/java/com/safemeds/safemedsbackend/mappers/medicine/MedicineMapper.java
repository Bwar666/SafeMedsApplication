package com.safemeds.safemedsbackend.mappers.medicine;


import com.safemeds.safemedsbackend.dtos.medicine.MedicineRequestDTO;
import com.safemeds.safemedsbackend.dtos.medicine.MedicineResponseDTO;
import com.safemeds.safemedsbackend.entities.Medicine;
import org.mapstruct.*;

import java.util.List;

@Mapper( componentModel = "spring" )
public interface MedicineMapper {

    Medicine toEntity(MedicineRequestDTO dto);

    MedicineResponseDTO toResponseDTO(Medicine entity);

    List<MedicineResponseDTO> toResponseDTOList(List<Medicine> medicines);
}
