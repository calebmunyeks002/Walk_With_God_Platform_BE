package org.walkwithgod.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, UUID> {

    Optional<PlatformSetting> findByKey(String key);

    List<PlatformSetting> findAllByOrderByCategoryAscKeyAsc();
}