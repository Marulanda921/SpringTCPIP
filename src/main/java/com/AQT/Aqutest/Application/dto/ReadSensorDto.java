package com.AQT.Aqutest.Application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ReadSensorDto {
    private String id;
    private String plotSize;
    private String plotVersion;
    private String encodeType;
    private String plotIntegrity;
    private String aquaSerial;
    private String master;
    private String sensorCode;
    private String channel;
    private String systemCommand;
    private String responseCode;
    private Date dateReadSensor;
    private LocalDateTime dataReadService;
    private int nut;
    private String alert;
    private String typeMessage;
}
