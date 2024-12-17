package com.AQT.Aqutest.Application.config;

import com.AQT.Aqutest.Application.service.TcpDataProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Configuration
public class TcpServerConfig {

    @Bean
    public TcpNioServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(8090);
        factory.setSingleUse(false);
        CustomMessageDeserializer deserializer = new CustomMessageDeserializer();
        factory.setSerializer(deserializer);
        factory.setDeserializer(deserializer);
        return factory;
    }

    public static class CustomMessageDeserializer extends AbstractByteArraySerializer {
        private static final int MESSAGE_LENGTH = 32;

        @Override
        public byte[] deserialize(InputStream inputStream) throws IOException {
            byte[] message = new byte[MESSAGE_LENGTH];
            int bytesRead = 0;
            int totalBytesRead = 0;

            while (totalBytesRead < MESSAGE_LENGTH &&
                    (bytesRead = inputStream.read(message, totalBytesRead, MESSAGE_LENGTH - totalBytesRead)) != -1) {
                totalBytesRead += bytesRead;
            }

            if (totalBytesRead != MESSAGE_LENGTH) {
                throw new IOException("Mensaje incompleto: se esperaban " +
                        MESSAGE_LENGTH + " bytes, se recibieron " + totalBytesRead);
            }

            return message;
        }

        @Override
        public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }

    @Bean
    public DirectChannel inboundChannel() {
        return new DirectChannel();
    }

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