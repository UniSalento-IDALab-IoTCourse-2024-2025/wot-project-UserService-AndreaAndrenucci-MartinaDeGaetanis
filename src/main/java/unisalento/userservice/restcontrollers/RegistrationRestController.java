package unisalento.userservice.restcontrollers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import unisalento.userservice.domain.AccreditedDomain;
import unisalento.userservice.domain.User;
import unisalento.userservice.domain.UserType;
import unisalento.userservice.dto.RegistrationResultDTO;
import unisalento.userservice.dto.UserDTO;
import unisalento.userservice.dto.VerifyUserDTO;
import unisalento.userservice.repositories.AccreditedDomainRepository;
import unisalento.userservice.repositories.UserRepository;
import unisalento.userservice.service.RabbitService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/registration")
public class RegistrationRestController {

    @Autowired
    RabbitService rabbitService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccreditedDomainRepository accreditedDomainRepository;

    @RequestMapping(value = "/user",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegistrationResultDTO> save(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult){

        RegistrationResultDTO resultDTO = new RegistrationResultDTO();

        //Verifica se ci sono errori di validazione
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();

            bindingResult.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                errorMap.put(fieldName, message);
            });

            resultDTO.setResult(RegistrationResultDTO.MISSING_FIELD);
            resultDTO.setMessage("Errore durante la registrazione");
            resultDTO.setErrors(errorMap);
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        //Verifica se l'email esiste già
        Optional<User> emailCheck = userRepository.findByEmail(userDTO.getEmail());
        if(emailCheck.isPresent()){
            resultDTO.setResult(RegistrationResultDTO.EMAIL_ALREADY_EXIST);
            resultDTO.setMessage("L'email è stata già utilizzata");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        //Verifica se il dominio con cui l'utente si è registrato è accreditato
        if (userDTO.getUserType() == UserType.RESEARCHER){
            String email = userDTO.getEmail();
            int indiceAt = email.indexOf('@');
            if (indiceAt != -1 && indiceAt < email.length() - 1) {
                String domain = email.substring(indiceAt + 1);
                System.out.println("Il dominio è: " + domain);
                Optional<AccreditedDomain> domainCheck = accreditedDomainRepository.findAccreditedDomainByAccreditedDomain(domain);
                if (domainCheck.isEmpty()){
                    resultDTO.setResult(RegistrationResultDTO.ERROR_DOMAIN);
                    resultDTO.setMessage("Il dominio specificato non è accreditato!");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
                int numClients = domainCheck.get().getNumClients();
                numClients++;
                domainCheck.get().setNumClients(numClients);
                accreditedDomainRepository.save(domainCheck.get());
            }
        }

        User user = new User();
        user.setUserType(userDTO.getUserType());
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now(ZoneId.of("Europe/Rome")).plusMinutes(15));
        user.setEnabled(false);
        user.setResetPasswordToken(null);

        user = userRepository.save(user);
        userDTO.setId(user.getId());
        userDTO.setVerificationCode(user.getVerificationCode());
        userDTO.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());
        userDTO.setResetPasswordToken(user.getResetPasswordToken());

        rabbitService.sendWelcomeUser(userDTO);
        rabbitService.sendVerificationCode(userDTO);

        resultDTO.setResult(RegistrationResultDTO.OK);
        resultDTO.setMessage("L'utente è stato registrato con successo");
        resultDTO.setUser(userDTO);

        HttpStatus status = HttpStatus.CREATED;
        return new ResponseEntity<>(resultDTO, status);
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    //HTTP POST request per verificare la correttezza del verification code
    @RequestMapping(value = "/verify",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
        Optional<User> optionalUser = userRepository.findByEmail(verifyUserDto.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","User non trovato"));
        }

        User user = optionalUser.get();
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("Europe/Rome")))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Il codice di verifica non è più valido"));
        }

        if (user.getVerificationCode().equals(verifyUserDto.getVerificationCode())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "L'account è stato verificato con successo"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Il codice di verifica è errato"));

    }

    // HTTP GET request per inviare nuovamente il verification code tramite email
    @RequestMapping(value = "/resend-email",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "L'utente non è stato trovato"));
        }

        User user = optionalUser.get();
        if (user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "L'account è stato già verificato"));
        }
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now(ZoneId.of("Europe/Rome")).plusMinutes(15));
        userRepository.save(user);

        UserDTO userDto = new UserDTO();
        userDto.setEmail(email);
        userDto.setName(optionalUser.get().getName());
        userDto.setSurname(optionalUser.get().getSurname());
        userDto.setVerificationCode(user.getVerificationCode());
        rabbitService.sendVerificationCode(userDto);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Il codice di verifica è stato rinviato"));
    }
}
