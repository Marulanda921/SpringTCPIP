package com.AQT.Aqutest.Application.config;

import com.AQT.Aqutest.Application.service.TcpDataProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;



@Configuration
public class TcpServerConfig {
    @Bean
    public TcpNioServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(8090);
        factory.setSingleUse(false);
        factory.setSerializer(new ByteArrayCrLfSerializer()); // Maneja mensajes delimitados por \n
        factory.setDeserializer(new ByteArrayCrLfSerializer());
        return factory;
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

        // Suscribe el servicio de procesamiento al canal
        inboundChannel.subscribe(processorService.createMessageHandler());
        return gateway;
    }
}
