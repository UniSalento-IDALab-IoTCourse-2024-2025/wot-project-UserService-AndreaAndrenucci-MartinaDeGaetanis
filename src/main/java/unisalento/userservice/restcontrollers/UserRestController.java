package unisalento.userservice.restcontrollers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import unisalento.userservice.domain.AccreditedDomain;
import unisalento.userservice.domain.User;
import unisalento.userservice.domain.UserType;
import unisalento.userservice.dto.*;
import unisalento.userservice.repositories.AccreditedDomainRepository;
import unisalento.userservice.repositories.UserRepository;
import unisalento.userservice.service.RabbitService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccreditedDomainRepository accreditedDomainRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UsersListDTO getAll(){
        List<User> users = userRepository.findAll();
        List<UserDTO> list = new ArrayList<>();

        for (User user : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUserType(user.getUserType());
            userDTO.setName(user.getName());
            userDTO.setSurname(user.getSurname());
            userDTO.setEmail(user.getEmail());

            userDTO.setVerificationCode(user.getVerificationCode());
            userDTO.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());
            userDTO.setEnabled(user.isEnabled());
            userDTO.setResetPasswordToken(user.getResetPasswordToken());

            list.add(userDTO);
        }

        UsersListDTO usersListDTO = new UsersListDTO();
        usersListDTO.setUsers(list);

        return usersListDTO;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findById(@Valid @PathVariable String id){

        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente con ID " + id + " non esiste"));
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.get().getId());
        userDTO.setUserType(user.get().getUserType());
        userDTO.setName(user.get().getName());
        userDTO.setSurname(user.get().getSurname());
        userDTO.setEmail(user.get().getEmail());
        userDTO.setVerificationCode(user.get().getVerificationCode());
        userDTO.setVerificationCodeExpiresAt(user.get().getVerificationCodeExpiresAt());
        userDTO.setEnabled(user.get().isEnabled());
        userDTO.setResetPasswordToken(user.get().getResetPasswordToken());

        return ResponseEntity.ok(userDTO);
    }


    @RequestMapping(value = "/search-email", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findByEmail(@Valid @RequestParam String email){

        if (email.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Specificare l'indirizzo email"));
        }

        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente con indirizzo email " + email + " non esiste"));
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.get().getId());
        userDTO.setUserType(user.get().getUserType());
        userDTO.setName(user.get().getName());
        userDTO.setSurname(user.get().getSurname());
        userDTO.setEmail(user.get().getEmail());
        userDTO.setVerificationCode(user.get().getVerificationCode());
        userDTO.setVerificationCodeExpiresAt(user.get().getVerificationCodeExpiresAt());
        userDTO.setEnabled(user.get().isEnabled());
        userDTO.setResetPasswordToken(user.get().getResetPasswordToken());
        return ResponseEntity.ok(userDTO);
    }


    @RequestMapping(value = "/search-domain", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?>findByDomain(@RequestParam String domain){

        if (domain.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Specificare il dominio"));
        }

        List<User> users = userRepository.findAll();
        List<UserDTO> list = new ArrayList<>();

        for (User user : users) {
            if (user.getUserType() == UserType.RESEARCHER){
                String email = user.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    if (domainUser.equals(domain)){
                        UserDTO userDTO = new UserDTO();
                        userDTO.setId(user.getId());
                        userDTO.setUserType(user.getUserType());
                        userDTO.setName(user.getName());
                        userDTO.setSurname(user.getSurname());
                        userDTO.setEmail(user.getEmail());

                        userDTO.setVerificationCode(user.getVerificationCode());
                        userDTO.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());
                        userDTO.setEnabled(user.isEnabled());
                        userDTO.setResetPasswordToken(user.getResetPasswordToken());

                        list.add(userDTO);
                    }
                }
            }
        }

        UsersListDTO usersListDTO = new UsersListDTO();
        usersListDTO.setUsers(list);

        return ResponseEntity.ok(usersListDTO);

    }


    @RequestMapping(value = "/{id}/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO){

        if(userDTO == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Specificare il body della richiesta"));
        }

        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente con ID " + id + " non esiste"));
        }

        //Verifica se l'utente ha intenzione di cambiare la password
        if (!passwordEncoder.matches(userDTO.getPassword(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Il campo 'password' non può essere modificato"));
        }

        //Verifica l'esistenza del dominio accreditato se si intende diventare Ricercatore
        List<AccreditedDomain> domains = accreditedDomainRepository.findAll();

        if (!userDTO.getUserType().equals(user.get().getUserType())) {
            if (userDTO.getUserType().equals(UserType.RESEARCHER)){
                String email = userDTO.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    boolean domainFound = false;
                    for (AccreditedDomain accreditedDomain : domains) {
                        if (accreditedDomain.getAccreditedDomain().equals(domainUser)) {
                            domainFound = true;
                            int numClients = accreditedDomain.getNumClients();
                            numClients++;
                            accreditedDomain.setNumClients(numClients);
                            accreditedDomainRepository.save(accreditedDomain);
                            break;
                        }
                    }
                    if (domainFound) {
                        rabbitService.sendChangeUserType(userDTO);

                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Il dominio del tuo indirizzo email non è accreditato"));
                    }
                }
            } else {
                String email = userDTO.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    Optional<AccreditedDomain> accreditedDomain = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(domainUser);
                    int numClients = accreditedDomain.get().getNumClients();
                    numClients--;
                    accreditedDomain.get().setNumClients(numClients);
                    accreditedDomainRepository.save(accreditedDomain.get());
                }
                rabbitService.sendChangeUserType(userDTO);
            }
        }

        user.get().setName(userDTO.getName());
        user.get().setSurname(userDTO.getSurname());
        user.get().setEmail(userDTO.getEmail());
        user.get().setUserType(userDTO.getUserType());

        userRepository.save(user.get());
        userDTO.setId(user.get().getId());
        userDTO.setVerificationCode(user.get().getVerificationCode());
        userDTO.setVerificationCodeExpiresAt(user.get().getVerificationCodeExpiresAt());
        userDTO.setEnabled(user.get().isEnabled());
        userDTO.setResetPasswordToken(user.get().getResetPasswordToken());

        return ResponseEntity.ok(userDTO);
    }


    @PatchMapping(value = "/{id}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserField(@PathVariable String id, @RequestBody Map<String, Object> patch)
            throws JsonProcessingException {

        if (patch == null || patch.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Invalido patch payload"));
        }

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente con ID " + id + " non esiste"));
        }

        User user = userOptional.get();
        UserType userType = user.getUserType();
        JsonNode userNode = objectMapper.convertValue(user, JsonNode.class);
        ObjectNode objectNode = (ObjectNode) userNode;

        String protectedField = "password";

        for (Map.Entry<String, Object> field : patch.entrySet()) {
            if (field.getKey().equals(protectedField)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Il campo 'password' non può essere modificato!"));
            }
            objectNode.put(field.getKey(), field.getValue().toString());
        }

        User userPatched = objectMapper.treeToValue(objectNode, User.class);

        //Verifica l'esistenza del dominio accreditato se si intende diventare Ricercatore
        List<AccreditedDomain> domains = accreditedDomainRepository.findAll();

        if (!userPatched.getUserType().equals(userType)) {
            UserDTO userDTO = new UserDTO();
            userDTO.setName(userPatched.getName());
            userDTO.setSurname(userPatched.getSurname());
            userDTO.setEmail(userPatched.getEmail());
            userDTO.setUserType(userPatched.getUserType());
            if (userDTO.getUserType().equals(UserType.RESEARCHER)){
                String email = userDTO.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    boolean domainFound = false;
                    for (AccreditedDomain accreditedDomain : domains) {
                        if (accreditedDomain.getAccreditedDomain().equals(domainUser)) {
                            domainFound = true;
                            int numClients = accreditedDomain.getNumClients();
                            numClients++;
                            accreditedDomain.setNumClients(numClients);
                            accreditedDomainRepository.save(accreditedDomain);
                            break;
                        }
                    }
                    if (domainFound) {
                        rabbitService.sendChangeUserType(userDTO);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","Il dominio del tuo indirizzo email non è accreditato"));
                    }
                }
            } else {
                String email = userDTO.getEmail();
                int indiceAt = email.indexOf('@');
                if (indiceAt != -1 && indiceAt < email.length() - 1) {
                    String domainUser = email.substring(indiceAt + 1);
                    Optional<AccreditedDomain> accreditedDomain = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(domainUser);
                    int numClients = accreditedDomain.get().getNumClients();
                    numClients--;
                    accreditedDomain.get().setNumClients(numClients);
                    accreditedDomainRepository.save(accreditedDomain.get());
                }
                rabbitService.sendChangeUserType(userDTO);            }
        }
        userRepository.save(userPatched);

        return ResponseEntity.ok(userPatched);
    }

    @RequestMapping(value="/{id}/change-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChangePasswordResultDTO> changePassword (@PathVariable String id, @RequestBody ChangePasswordDTO changePasswordDTO){

        ChangePasswordResultDTO changePasswordResultDTO = new ChangePasswordResultDTO();

        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.USER_NOT_FOUND);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.NOT_FOUND);
        }

        if (changePasswordDTO.getOldPassword() == null || changePasswordDTO.getOldPassword().isEmpty()) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.MISSING_OLD_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        if (changePasswordDTO.getNewPassword() == null || changePasswordDTO.getNewPassword().isEmpty()) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.MISSING_NEW_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        if (changePasswordDTO.getConfirmNewPassword() == null || changePasswordDTO.getConfirmNewPassword().isEmpty()) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.MISSING_CONFIRMATION_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.INCORRECT_CONFIRMATION_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        if (changePasswordDTO.getNewPassword().length() < 8) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.LENGHT_ERROR);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        //Verifica che la password vecchia è corretta
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.get().getPassword())) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.WRONG_OLD_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        //Verifica che la nuova password è diversa da quella vecchia
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.get().getPassword())) {
            changePasswordResultDTO.setResult(ChangePasswordResultDTO.SAME_PASSWORD);
            return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.BAD_REQUEST);
        }

        user.get().setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user.get());

        changePasswordResultDTO.setResult(ChangePasswordResultDTO.SUCCESS);
        return new ResponseEntity<>(changePasswordResultDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUser(@Valid @PathVariable String id){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente con ID " + id + " non esiste"));
        }

        String email = user.get().getEmail();
        int indiceAt = email.indexOf('@');
        if (indiceAt != -1 && indiceAt < email.length() - 1) {
            String domainUser = email.substring(indiceAt + 1);
            Optional<AccreditedDomain> accreditedDomain = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(domainUser);
            int numClients = accreditedDomain.get().getNumClients();
            numClients--;
            accreditedDomain.get().setNumClients(numClients);
            accreditedDomainRepository.save(accreditedDomain.get());
        }

        userRepository.delete(user.get());
        return ResponseEntity.ok().body(Map.of("message","L'utente è stato eliminato con successo"));
    }


    @RequestMapping(value = "/researcher", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UsersListDTO getAllResearchers(){

        List<User> users = userRepository.findAll();
        List<UserDTO> list = new ArrayList<>();

        for (User user : users) {
            if (user.getUserType() == UserType.RESEARCHER) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUserType(user.getUserType());
                userDTO.setName(user.getName());
                userDTO.setSurname(user.getSurname());
                userDTO.setEmail(user.getEmail());

                userDTO.setVerificationCode(user.getVerificationCode());
                userDTO.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());
                userDTO.setEnabled(user.isEnabled());
                userDTO.setResetPasswordToken(user.getResetPasswordToken());

                list.add(userDTO);
            }
        }

        UsersListDTO usersListDTO = new UsersListDTO();
        usersListDTO.setUsers(list);

        return usersListDTO;
    }


    @RequestMapping(value = "/regular", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UsersListDTO getAllUserRegular(){

        List<User> users = userRepository.findAll();
        List<UserDTO> list = new ArrayList<>();

        for (User user : users) {
            if (user.getUserType() == UserType.REGULAR) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUserType(user.getUserType());
                userDTO.setName(user.getName());
                userDTO.setSurname(user.getSurname());
                userDTO.setEmail(user.getEmail());

                userDTO.setVerificationCode(user.getVerificationCode());
                userDTO.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());
                userDTO.setEnabled(user.isEnabled());
                userDTO.setResetPasswordToken(user.getResetPasswordToken());

                list.add(userDTO);
            }
        }

        UsersListDTO usersListDTO = new UsersListDTO();
        usersListDTO.setUsers(list);

        return usersListDTO;
    }


}
