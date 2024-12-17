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
import java.time.LocalDateTime;
import java.util.Arrays;
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
                byte[] rawPayload = (byte[]) message.getPayload();
                processMessage(rawPayload);
            } catch (Exception e) {
                System.err.println("Error procesando mensaje: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
    private void processMessage(byte[] data) {
        try {
            System.out.println("\nCampos del mensaje:");
            System.out.println(Arrays.toString(data));

        } catch (Exception e) {
            System.err.println("Error al procesar los campos del mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


