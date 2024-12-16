package com.AQT.Aqutest.Domain.repository;
import com.AQT.Aqutest.Domain.model.ReadSensorFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReadSensorRepository extends JpaRepository<ReadSensorFormat, Integer> {
}

