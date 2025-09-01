package unisalento.userservice.restcontrollers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unisalento.userservice.domain.AccreditedDomain;
import unisalento.userservice.domain.Admin;
import unisalento.userservice.domain.User;
import unisalento.userservice.domain.UserType;
import unisalento.userservice.dto.*;
import unisalento.userservice.repositories.AccreditedDomainRepository;
import unisalento.userservice.repositories.AdminRepository;
import unisalento.userservice.repositories.UserRepository;
import unisalento.userservice.service.RabbitService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AdminRestController {

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    AccreditedDomainRepository accreditedDomainRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RabbitService rabbitService;

    @RequestMapping(value = "/admins/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminsListDTO getAll() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminDTO> list = new ArrayList<>();

        for (Admin admin : admins) {
            AdminDTO adminDTO = new AdminDTO();
            adminDTO.setId(admin.getId());
            adminDTO.setName(admin.getName());
            adminDTO.setSurname(admin.getSurname());
            adminDTO.setEmail(admin.getEmail());
            list.add(adminDTO);
        }

        AdminsListDTO adminsListDTO = new AdminsListDTO();
        adminsListDTO.setAdminsList(list);
        return adminsListDTO;
    }


    @RequestMapping(value = "/admins/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findById(@Valid @PathVariable String id){
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Admin con ID " + id + " non esiste"));
        }

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(admin.get().getId());
        adminDTO.setName(admin.get().getName());
        adminDTO.setSurname(admin.get().getSurname());
        adminDTO.setEmail(admin.get().getEmail());

        return ResponseEntity.ok().body(adminDTO);
    }


    @RequestMapping(value = "/add-domain", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addDomain(@RequestBody AccreditedDomainDTO accreditedDomainDTO) {
        Optional<AccreditedDomain> domain = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(accreditedDomainDTO.getAccreditedDomain());
        if (domain.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Il dominio " + accreditedDomainDTO.getAccreditedDomain() + " è già accreditato"));
        }

        AccreditedDomain accreditedDomain = new AccreditedDomain();
        accreditedDomain.setAccreditedDomain(accreditedDomainDTO.getAccreditedDomain());
        accreditedDomainRepository.save(accreditedDomain);
        accreditedDomainDTO.setId(accreditedDomain.getId());

        int count = 0;

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUserType() == UserType.REGULAR){
                String email = user.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    if (accreditedDomainDTO.getAccreditedDomain().equals(domainUser)){
                        user.setUserType(UserType.RESEARCHER);
                        userRepository.save(user);
                        UserDTO userDTO = new UserDTO();
                        userDTO.setName(user.getName());
                        userDTO.setSurname(user.getSurname());
                        userDTO.setEmail(user.getEmail());
                        rabbitService.sendNewAccreditedDomain(userDTO);
                        count ++;
                    }
                }
            }
        }

        accreditedDomainDTO.setNumClients(count);

        return ResponseEntity.status(HttpStatus.CREATED).body(accreditedDomainDTO);

    }


    @RequestMapping(value = "/delete-domain", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteDomain(@RequestBody AccreditedDomainDTO accreditedDomainDTO) {
        Optional<AccreditedDomain> domain = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(accreditedDomainDTO.getAccreditedDomain());
        if (domain.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Il dominio " + accreditedDomainDTO.getAccreditedDomain() + " non esiste"));
        }

        accreditedDomainRepository.delete(domain.get());
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUserType() == UserType.RESEARCHER){
                String email = user.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    if (accreditedDomainDTO.getAccreditedDomain().equals(domainUser)){
                        user.setUserType(UserType.REGULAR);
                        userRepository.save(user);
                        UserDTO userDTO = new UserDTO();
                        userDTO.setName(user.getName());
                        userDTO.setSurname(user.getSurname());
                        userDTO.setEmail(user.getEmail());
                        rabbitService.sendDomainDeletion(userDTO);
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Il dominio è stato eliminato con successo"));
    }


    @RequestMapping(value = "/domains", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AccreditedDomainsDTO getAllDomains() {
        List<AccreditedDomain> domains = accreditedDomainRepository.findAll();
        List<AccreditedDomainDTO> list = new ArrayList<>();

        for (AccreditedDomain domain : domains) {
            AccreditedDomainDTO domainDTO = new AccreditedDomainDTO();
            domainDTO.setId(domain.getId());
            domainDTO.setAccreditedDomain(domain.getAccreditedDomain());
            domainDTO.setNumClients(domain.getNumClients());

            list.add(domainDTO);
        }

        AccreditedDomainsDTO listDomainDTO = new AccreditedDomainsDTO();
        listDomainDTO.setAccreditedDomainList(list);
        return listDomainDTO;
    }


}
