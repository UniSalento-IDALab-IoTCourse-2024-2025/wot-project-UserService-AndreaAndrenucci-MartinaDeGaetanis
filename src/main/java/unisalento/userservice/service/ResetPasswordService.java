package unisalento.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unisalento.userservice.domain.User;
import unisalento.userservice.repositories.UserRepository;

import java.util.Optional;

@Service
public class ResetPasswordService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void updateResetPasswordToken(String token, String email) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            user.get().setResetPasswordToken(token);
            userRepository.save(user.get());
        }

    }

    public User getByRestPasswordTokenUser(String token){
        Optional<User> user = userRepository.findByResetPasswordToken(token);
        if (user.isEmpty()){
            return null;
        }
        return user.get();
    }


    public void updatePasswordUser(User user, String newPassword){
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

}
