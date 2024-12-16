package com.AQT.Aqutest.Domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Readsensorformat")
@Entity
public class ReadSensorFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String alert;
    private String aquaSerial;
    private String channel;
    private LocalDateTime dataReadService;
    private Date dateReadSensor;
    private String encodeType;
    private String master;
    private int nut;
    private String plotIntegrity;
    private String plotSize;
    private String plotVersion;
    private String responseCode;
    private String sensorCode;
    private String systemCommand;
    private String typeMessage;
}



