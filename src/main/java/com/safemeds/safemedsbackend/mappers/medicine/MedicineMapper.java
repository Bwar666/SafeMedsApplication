package com.safemeds.safemedsbackend.mappers.medicine;

import com.safemeds.safemedsbackend.entities.*;
import com.safemeds.safemedsbackend.dtos.medicine.*;
import org.mapstruct.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Medicine toEntity(MedicineRequestDTO dto);

    MedicineResponseDTO toResponseDTO(Medicine medicine);

    List<MedicineResponseDTO> toResponseDTOList(List<Medicine> medicines);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MedicineRequestDTO dto, @MappingTarget Medicine entity);

    default String formatDosage(Medicine medicine) {
        if (medicine.getIntakeSchedules().isEmpty()) return "";

        IntakeSchedule first = medicine.getIntakeSchedules().get(0);
        String unit = switch (medicine.getForm()) {
            case PILL -> "pill(s)";
            case LIQUID -> "ml";
            case INJECTION -> "unit(s)";
            case DROPS -> "drop(s)";
            default -> "dose(s)";
        };
        return first.getAmount() + " " + unit;
    }
}