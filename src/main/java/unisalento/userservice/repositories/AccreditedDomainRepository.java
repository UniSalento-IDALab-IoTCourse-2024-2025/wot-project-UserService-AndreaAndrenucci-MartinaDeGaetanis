package unisalento.userservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import unisalento.userservice.domain.AccreditedDomain;

import java.util.Optional;


public interface AccreditedDomainRepository extends MongoRepository<AccreditedDomain, String> {

    Optional<AccreditedDomain> findAccreditedDomainByAccreditedDomain(String accreditedDomain);
}
