package com.AQT.Aqutest.Domain.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "readsensorformat")
@Entity
public class ReadSensorFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Integer alert;
    private Integer aquaSerial;
    private Integer channel;
    private LocalDateTime dataReadService;
    private LocalDateTime dateReadSensor;
    private Integer encodeType;
    private Integer master;
    private int nut;
    private Integer plotIntegrity;
    private Integer plotSize;
    private Integer plotVersion;
    private Integer responseCode;
    private Integer sensorCode;
    private Integer systemCommand;
    private Integer typeMessage;
}



