package unisalento.userservice.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String VERIFICATION_CODE_QUEUE = "verification_code_queue";
    public static final String WELCOME_QUEUE = "welcome_queue";
    public static final String RESET_PASSWORD_QUEUE = "reset_password_queue";
    public static final String DOMAIN_DELETION_QUEUE = "domain_deletion_queue";
    public static final String NEW_ACCREDITED_DOMAIN = "new_accredited_domain_queue";
    public static final String CHANGE_USER_TYPE = "change_user_type_queue";


    //Configurazione VERIFICATION_CODE_QUEUE
    @Bean
    public Queue queueVerificationCode(){
        return new Queue(VERIFICATION_CODE_QUEUE, false);
    }

    //Configurazione WELCOME_QUEUE
    @Bean
    public Queue queueWelcome(){
        return new Queue(WELCOME_QUEUE, false);
    }

    //Configurazione RESET_PASSWORD_QUEUE
    @Bean
    public Queue queueResetPassword(){
        return new Queue(RESET_PASSWORD_QUEUE, false);
    }

    //Configurazione DOMAIN_DELETION_QUEUE
    @Bean
    public Queue queueDomainDeletion(){
        return new Queue(DOMAIN_DELETION_QUEUE, false);
    }

    //Configurazione NEW_ACCREDITED_DOMAIN
    @Bean
    public Queue queueNewAccreditedDomain(){
        return new Queue(NEW_ACCREDITED_DOMAIN, false);
    }

    //Configurazione CHANGE_USER_TYPE
    @Bean
    public Queue queueChangeUserType(){
        return new Queue(CHANGE_USER_TYPE, false);
    }

    //Configurazione di Jackson per la serializzazione e quindi conversione degli oggetti Java in JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //Configurazione RabbitTamplate per l'utilizzo del convertitore JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }


}
