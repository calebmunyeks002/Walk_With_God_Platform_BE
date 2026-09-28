package org.walkwithgod.prayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PrayerRepository extends JpaRepository<Prayer, UUID> {

    Optional<Prayer> findBySlotAndPrayerDate(String slot, LocalDate date);

    boolean existsBySlotAndPrayerDate(String slot, LocalDate date);
}