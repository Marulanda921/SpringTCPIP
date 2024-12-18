package com.AQT.Aqutest.Application.service;

import com.AQT.Aqutest.Domain.model.ReadSensorFormat;
import com.AQT.Aqutest.Domain.repository.ReadSensorFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class ReadSensorFormatService {
    private static final Logger logger = LoggerFactory.getLogger(ReadSensorFormatService.class);

    @Autowired
    private ReadSensorFormatRepository readSensorFormatRepository;

    @Transactional
    public ReadSensorFormat saveReadSensorFormat(ReadSensorFormat readSensorFormat) {
        logger.debug("Received ReadSensorFormat to save: {}", readSensorFormat);
        ReadSensorFormat savedReadSensorFormat = readSensorFormatRepository.save(readSensorFormat);
        logger.debug("Successfully saved ReadSensorFormat: {}", savedReadSensorFormat);
        return savedReadSensorFormat;
    }
}
