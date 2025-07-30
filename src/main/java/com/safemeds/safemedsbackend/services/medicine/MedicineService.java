package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.MedicineRequestDTO;
import com.safemeds.safemedsbackend.dtos.medicine.MedicineResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MedicineService {
    @Transactional
    MedicineResponseDTO createMedicine(UUID userId, MedicineRequestDTO dto);

    List<MedicineResponseDTO> getAllMedicinesByUser(UUID userId);

    MedicineResponseDTO getMedicineById(UUID userId, UUID medicineId);

    @Transactional
    MedicineResponseDTO updateMedicine(UUID userId, UUID medicineId, MedicineRequestDTO dto);

    @Transactional
    void deleteMedicine(UUID userId, UUID medicineId);

    List<MedicineResponseDTO> getActiveMedicines(UUID userId);

    @Transactional
    MedicineResponseDTO archiveMedicine(UUID userId, UUID medicineId);
}