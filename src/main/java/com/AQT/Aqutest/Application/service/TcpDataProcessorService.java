package com.AQT.Aqutest.Application.service;

import com.AQT.Aqutest.Domain.model.ReadSensorFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TcpDataProcessorService {
    @Autowired
    private ReadSensorFormatService readSensorFormatService;

    public MessageHandler createMessageHandler() {
        return message -> {
            try {
                byte[] rawPayload = (byte[]) message.getPayload();
                log.info("Mensaje recibido (hex): {}", bytesToHex(rawPayload));
                processBytes(rawPayload);
            } catch (Exception e) {
                log.error("Error procesando mensaje: ", e);
            }
        };
    }

    private void processBytes(byte[] bytes) {
        log.debug("Procesando bytes: {}", bytesToHex(bytes));
        if (bytes.length >= 22) {
            ReadSensorFormat readSensorFormat = new ReadSensorFormat();
            readSensorFormat.setPlotSize(bytes[0] & 0xFF);
            readSensorFormat.setPlotVersion(bytes[1] & 0xFF);
            readSensorFormat.setEncodeType(bytes[2] & 0xFF);
            readSensorFormat.setPlotIntegrity((bytes[3] & 0xFF) << 16 | (bytes[4] & 0xFF) << 8 | (bytes[5] & 0xFF));
            readSensorFormat.setAquaSerial((bytes[6] & 0xFF) << 24 | (bytes[7] & 0xFF) << 16 | (bytes[8] & 0xFF) << 8 | (bytes[9] & 0xFF));
            readSensorFormat.setMaster(bytes[10] & 0xFF);
            readSensorFormat.setSensorCode(bytes[11] & 0xFF);
            readSensorFormat.setChannel(bytes[12] & 0xFF);
            readSensorFormat.setSystemCommand(bytes[13] & 0xFF);
            readSensorFormat.setResponseCode(bytes[14] & 0xFF);
            readSensorFormat.setDateReadSensor(LocalDateTime.now());
            readSensorFormat.setDataReadService(LocalDateTime.now());
            readSensorFormat.setNut((bytes[15] & 0xFF) << 24 | (bytes[16] & 0xFF) << 16 | (bytes[17] & 0xFF) << 8 | (bytes[18] & 0xFF));
            readSensorFormat.setAlert(bytes[19] & 0xFF);
            readSensorFormat.setTypeMessage((bytes[20] & 0xFF) << 8 | (bytes[21] & 0xFF));

            log.debug("ReadSensorFormat creado: {}", readSensorFormat);
            readSensorFormatService.saveReadSensorFormat(readSensorFormat);
        } else {
            log.warn("Bytes insuficientes para procesar: {}", bytesToHex(bytes));
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}