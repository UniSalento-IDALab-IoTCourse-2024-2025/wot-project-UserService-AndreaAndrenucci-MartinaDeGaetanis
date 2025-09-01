package unisalento.userservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import unisalento.userservice.domain.Admin;

import java.util.Optional;

public interface AdminRepository  extends MongoRepository<Admin, String> {
   Optional<Admin> findByEmail(String email);
}
