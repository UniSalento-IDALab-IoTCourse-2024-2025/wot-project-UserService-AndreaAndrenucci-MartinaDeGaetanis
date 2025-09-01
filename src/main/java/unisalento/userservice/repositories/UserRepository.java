package unisalento.userservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import unisalento.userservice.domain.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

}
