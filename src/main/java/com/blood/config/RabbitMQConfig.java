package com.blood.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "bloodbank.email.queue";

    @Bean
    public Queue emailQueue() {
        //Tham số 'true' có nghĩa là queue này sẽ được lưu lại (durable),
        //không bị mất dữ liệu nếu server RabbitMQ bị restart
        return new Queue(EMAIL_QUEUE, true);
    }
}
