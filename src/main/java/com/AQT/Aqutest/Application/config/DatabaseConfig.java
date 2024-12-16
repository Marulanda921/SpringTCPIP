package com.AQT.Aqutest.Application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
public class DatabaseConfig {

    private final DataSource dataSource;

    @Autowired
    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyDatabaseConnection() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("=== Verificación de Base de Datos ===");
            log.info("Conexión exitosa a la base de datos");
            log.info("URL: {}", conn.getMetaData().getURL());
            log.info("Usuario: {}", conn.getMetaData().getUserName());
            log.info("Base de datos: {}", conn.getCatalog());

            // Verificar si la tabla existe
            ResultSet tables = conn.getMetaData().getTables(
                    conn.getCatalog(), null, "ReadSensorFormat", null);

            if (tables.next()) {
                log.info("Tabla ReadSensorFormat encontrada");
                // Mostrar estructura de la tabla
                ResultSet columns = conn.getMetaData().getColumns(
                        conn.getCatalog(), null, "ReadSensorFormat", null);
                log.info("Estructura de la tabla:");
                while (columns.next()) {
                    log.info("Columna: {} - Tipo: {}",
                            columns.getString("COLUMN_NAME"),
                            columns.getString("TYPE_NAME"));
                }
            } else {
                log.warn("Tabla ReadSensorFormat no encontrada!");
            }
            log.info("===================================");
        } catch (SQLException e) {
            log.error("Error verificando la base de datos: ", e);
        }
    }
}
