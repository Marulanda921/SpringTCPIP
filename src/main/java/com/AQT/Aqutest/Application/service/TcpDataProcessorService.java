package com.AQT.Aqutest.Application.service;
import com.AQT.Aqutest.Domain.model.ReadSensorFormat;
import com.AQT.Aqutest.Domain.repository.ReadSensorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Enumeration;

@Slf4j
@Service
public class TcpDataProcessorService {
    private final ReadSensorRepository repository;
    private final int port;
    private volatile boolean serverStarted = false;


    @Autowired
    public TcpDataProcessorService(ReadSensorRepository repository,
                                   TcpNioServerConnectionFactory connectionFactory) {
        this.repository = repository;
        this.port = connectionFactory.getPort();

        log.info("TcpDataProcessorService inicializado - Puerto configurado: {}", port);
        if (connectionFactory.isRunning()) {
            log.info("Servidor TCP ya está en ejecución");
        } else {
            log.warn("Servidor TCP NO está en ejecución");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void displayConnectionInfo() {
        try {
            log.info("\n==== INFORMACIÓN DE CONEXIÓN TCP ====");
            log.info("Puerto TCP: {}", port);
            log.info("Estado del servidor: {}", serverStarted ? "INICIADO" : "NO INICIADO");

            // Verificar si el puerto está en uso
            try (ServerSocket testSocket = new ServerSocket(port)) {
                log.warn("¡ADVERTENCIA! El puerto {} está disponible - posiblemente el servidor TCP no está escuchando", port);
            } catch (BindException e) {
                log.info("Puerto {} está en uso - esto es esperado si el servidor está funcionando", port);
            } catch (IOException e) {
                log.error("Error al verificar el puerto: ", e);
            }

            log.info("Direcciones IP disponibles:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && !iface.isLoopback()) {
                    log.info("Interfaz encontrada: {} ({})",
                            iface.getDisplayName(),
                            iface.isUp() ? "ACTIVA" : "INACTIVA");

                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr.getHostAddress().indexOf(':') == -1) {
                            log.info("  → IP: {} ({})",
                                    addr.getHostAddress(),
                                    addr.isReachable(1000) ? "ACCESIBLE" : "NO ACCESIBLE");
                        }
                    }
                }
            }
            log.info("=====================================\n");

            serverStarted = true;
        } catch (Exception e) {
            log.error("Error crítico al inicializar el servidor TCP: ", e);
            serverStarted = false;
        }
    }

    public MessageHandler createMessageHandler() {
        return message -> {
            try {
                log.info("→ Conexión TCP recibida");

                // Validación del mensaje y su payload
                if (message == null) {
                    log.error("❌ Mensaje TCP es NULL");
                    return;
                }

                if (message.getPayload() == null) {
                    log.error("❌ Payload del mensaje es NULL");
                    return;
                }

                if (!(message.getPayload() instanceof byte[])) {
                    log.error("❌ Tipo de payload incorrecto. Esperado: byte[], Recibido: {}",
                            message.getPayload().getClass());
                    return;
                }

                byte[] payload = (byte[]) message.getPayload();
                log.info("✓ Payload recibido: {} bytes", payload.length);

                // Log de datos en formato HEX para depuración
                if (payload.length > 0) {
                    log.info("✓ Datos HEX: {}", bytesToHex(payload));
                } else {
                    log.warn("⚠️ Payload vacío");
                }

                // Validación del tamaño del payload
                if (payload.length < 32) {
                    log.error("❌ Payload incompleto: {} bytes (se esperan 32)", payload.length);
                    return;
                }

                // Procesamiento del payload
                ReadSensorFormat sensorData = processData(payload);
                log.info("✓ Datos procesados: {}", sensorData);

                // Guardado en base de datos
                ReadSensorFormat savedData = repository.save(sensorData);
                log.info("✓ Datos guardados. ID: {}", savedData.getId());

            } catch (Exception e) {
                log.error("❌ Error en procesamiento TCP: {}", e.getMessage());
                log.debug("Stack trace completo: ", e);
            }
        };
    }


    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private ReadSensorFormat processData(byte[] data) {
        if (data == null || data.length < 32) {
            throw new IllegalArgumentException("Datos inválidos: payload incompleto");
        }

        ReadSensorFormat format = new ReadSensorFormat();

        // PlotSize (2 bytes)
        format.setPlotSize(String.format("%02X %02X", data[0], data[1]));

        // PlotVersion (1 byte)
        format.setPlotVersion(String.format("%02X", data[2]));

        // EncodeType (1 byte)
        format.setEncodeType(String.format("%02X", data[3]));

        // PlotIntegrity (3 bytes)
        format.setPlotIntegrity(String.format("%02X %02X %02X",
                data[4], data[5], data[6]));

        // AquaSerial (4 bytes)
        format.setAquaSerial(String.format("%02X %02X %02X %02X",
                data[7], data[8], data[9], data[10]));

        // Master (1 byte)
        format.setMaster(String.format("%02X", data[11]));

        // SensorCode (1 byte)
        format.setSensorCode(String.format("%02X", data[12]));

        // Channel (1 byte)
        format.setChannel(String.format("%02X", data[13]));

        // SystemCommand (1 byte)
        format.setSystemCommand(String.format("%02X", data[14]));

        // ResponseCode (1 byte)
        format.setResponseCode(String.format("%02X", data[15]));

        try {
            int year = data[16] + 2000;
            int month = data[17];
            int day = data[18];
            int hour = data[19];
            int minute = data[20];
            int second = data[21];
            int millisecond = data[22];

            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second, millisecond * 1000000);
            format.setDateReadSensor(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
            format.setDataReadService(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error procesando fecha: ", e);
            format.setDataReadService(LocalDateTime.now());
        }

        // NUT (4 bytes)
        format.setNut(Integer.parseInt(String.format("%02X%02X%02X%02X",
                data[23], data[24], data[25], data[26]), 16));

        // Alert (1 byte)
        format.setAlert(String.format("%02X", data[27]));

        // TypeMessage (4 bytes)
        format.setTypeMessage(String.format("%02X %02X %02X %02X",
                data[28], data[29], data[30], data[31]));

        return format;
    }
}

