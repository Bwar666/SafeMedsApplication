package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.entities.IntakeEvent;
import java.util.List;
import java.util.UUID;

public interface IntakeEventService {
    List<IntakeEvent> generateIntakeEvents(Medicine medicine);
    void deleteIntakeEventsForMedicine(UUID medicineId);
    void updateIntakeEvents(Medicine medicine);
}

