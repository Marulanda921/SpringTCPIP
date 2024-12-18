package com.AQT.Aqutest.Domain.repository;
import com.AQT.Aqutest.Domain.model.ReadSensorFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadSensorFormatRepository extends JpaRepository<ReadSensorFormat, Integer> {
}

