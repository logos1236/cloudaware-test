package ru.armishev.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.armishev.entity.AmazonObjectEntity;

public interface AmazonObjectJPA extends JpaRepository<AmazonObjectEntity, String> {
}
