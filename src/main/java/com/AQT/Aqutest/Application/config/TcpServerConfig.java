package com.AQT.Aqutest.Application.config;

import com.AQT.Aqutest.Application.service.TcpDataProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

@Configuration
public class TcpServerConfig {
    private static final Logger log = LoggerFactory.getLogger(TcpServerConfig.class);

    @Bean
    public TcpNioServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(8090);
        factory.setSingleUse(false);
        HexDataDeserializer deserializer = new HexDataDeserializer();
        factory.setSerializer(deserializer);
        factory.setDeserializer(deserializer);
        return factory;
    }
    //<Summary>
    //abro un buffer para mejorar la eficiencia de la lectura de los bytes
    //Arreglo de bytes llamado data para leer los bytes del inputStream
    //si el tamaño del buffer es mayor o igual a 22 bytes, se sale del ciclo
    //convierto el contenido del buffer a un arreglo de bytes
    //construye una cadena hexString con los bytes recibidos
    //divide la cadena hexString en valores individiuales hexadecimales
    //convierte cada valor hexadecimal a un byte y lo guarda en un arreglo de bytes
    public static class HexDataDeserializer extends AbstractByteArraySerializer {
        @Override
        public byte[] deserialize(InputStream inputStream) throws IOException {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                byte[] data = new byte[1024];
                int bytesRead;

                while ((bytesRead = bufferedInputStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                    if (buffer.size() >= 22) {
                        break;
                    }
                }

                byte[] receivedBytes = buffer.toByteArray();
                StringBuilder hexString = new StringBuilder();

                for (byte b : receivedBytes) {
                    if (Character.digit(b, 16) != -1 || b == ' ') {
                        hexString.append((char) b);
                    }
                }
                String[] hexValues = hexString.toString().trim().split("\\s+");
                byte[] result = new byte[hexValues.length];
                for (int i = 0; i < hexValues.length; i++) {
                    result[i] = (byte) Integer.parseInt(hexValues[i], 16);
                }
                return result;
            }
        }

        @Override
        public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
            outputStream.write(bytes);
            outputStream.flush();
        }

        private static String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString().trim();
        }
    }

    @Bean
    public DirectChannel inboundChannel() {
        return new DirectChannel();
    }

    //<Summary>
    //crea una instancia de TcpInboundGateway
    //establece la conexión con el factory
    //establece el canal de entrada
    //suscribe el canal de entrada al manejador de mensajes
    //retorna la instancia de TcpInboundGateway
    @Bean
    public TcpInboundGateway inboundGateway(TcpNioServerConnectionFactory connectionFactory,
                                            DirectChannel inboundChannel,
                                            TcpDataProcessorService processorService) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(connectionFactory);
        gateway.setRequestChannel(inboundChannel);
        inboundChannel.subscribe(processorService.createMessageHandler());
        return gateway;
    }
}