package unisalento.userservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unisalento.userservice.configuration.RabbitConfig;
import unisalento.userservice.dto.ResetPasswordMessageUserDTO;
import unisalento.userservice.dto.UserDTO;


@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendVerificationCode(UserDTO userDto) {

        UserDTO user = new UserDTO();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        user.setVerificationCode(userDto.getVerificationCode());

        rabbitTemplate.convertAndSend(RabbitConfig.VERIFICATION_CODE_QUEUE, user);
    }

    public void sendWelcomeUser(UserDTO userDto) {

        UserDTO user = new UserDTO();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setUserType(userDto.getUserType());
        user.setEmail(userDto.getEmail());

        rabbitTemplate.convertAndSend(RabbitConfig.WELCOME_QUEUE, user);
    }

    public void sendResetPasswordUser(UserDTO user, String resetPasswordLink){

        ResetPasswordMessageUserDTO resetPass = new ResetPasswordMessageUserDTO();
        resetPass.setUser(user);
        resetPass.setResetPasswordLink(resetPasswordLink);
        rabbitTemplate.convertAndSend(RabbitConfig.RESET_PASSWORD_QUEUE, resetPass);
    }

    public void sendDomainDeletion(UserDTO userDto){

        UserDTO user = new UserDTO();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());

        rabbitTemplate.convertAndSend(RabbitConfig.DOMAIN_DELETION_QUEUE, user);
    }

    public void sendNewAccreditedDomain(UserDTO userDto){

        UserDTO user = new UserDTO();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());

        rabbitTemplate.convertAndSend(RabbitConfig.NEW_ACCREDITED_DOMAIN, user);
    }

    public void sendChangeUserType(UserDTO userDto) {

        UserDTO user = new UserDTO();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setUserType(userDto.getUserType());
        user.setEmail(userDto.getEmail());

        rabbitTemplate.convertAndSend(RabbitConfig.CHANGE_USER_TYPE, user);
    }
}
