package unisalento.userservice.restcontrollers;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import unisalento.userservice.domain.Admin;
import unisalento.userservice.domain.User;
import unisalento.userservice.domain.UserType;
import unisalento.userservice.dto.AuthenticationDTO;
import unisalento.userservice.dto.AuthenticationResultDTO;
import unisalento.userservice.dto.UserDTO;
import unisalento.userservice.repositories.AdminRepository;
import unisalento.userservice.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import unisalento.userservice.security.JwtUtilities;
import unisalento.userservice.service.RabbitService;
import unisalento.userservice.service.ResetPasswordService;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/authenticate")
public class AuthenticationRestController {

    @Autowired
    JwtUtilities jwtUtilities;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ResetPasswordService resetPasswordService;

    @Autowired
    RabbitService rabbitService;

    @RequestMapping(value = "/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResultDTO> createAuthenticationToken(@RequestBody AuthenticationDTO authenticationDTO) {
        AuthenticationResultDTO authenticationResultDTO = new AuthenticationResultDTO();

        Optional<User> user = userRepository.findByEmail(authenticationDTO.getEmail());
        Optional<Admin> admin = adminRepository.findByEmail(authenticationDTO.getEmail());

        if (user.isEmpty() && admin.isEmpty()) {
            authenticationResultDTO.setResult(AuthenticationResultDTO.EMAIL_NOT_FOUND);
            return new ResponseEntity<>(authenticationResultDTO, HttpStatus.BAD_REQUEST);
        }

        if (user.isPresent() && !user.get().isEnabled()) {
            authenticationResultDTO.setResult(AuthenticationResultDTO.NOT_ENABLED);
            return new ResponseEntity<>(authenticationResultDTO, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationDTO.getEmail(),
                            authenticationDTO.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            authenticationResultDTO.setResult(AuthenticationResultDTO.INCORRECT_PASSWORD);
            return new ResponseEntity<>(authenticationResultDTO, HttpStatus.BAD_REQUEST);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Object> claims = new HashMap<>();
        final String jwt;
        if (user.isEmpty()) {
            claims.put("userId", admin.get().getId());
            claims.put("role", "ADMIN");
            jwt = jwtUtilities.generateToken(admin.get().getEmail(), claims);
        } else {
            claims.put("userId", user.get().getId());
            if (user.get().getUserType() == UserType.REGULAR) {
                claims.put("role", "REGULAR");
            } else {
                claims.put("role", "RESEARCHER");
            }
            jwt = jwtUtilities.generateToken(user.get().getEmail(), claims);
        }

        authenticationResultDTO.setJwt(jwt);
        authenticationResultDTO.setResult(0);
        return new ResponseEntity<>(authenticationResultDTO, HttpStatus.CREATED);
    }


    //Richiesta di invio email per reimpostare la password
    @RequestMapping(value = "/forgot-password",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processForgotPassword(@RequestParam String email, HttpServletRequest request){

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","L'utente non esiste"));
        }

        String token = RandomStringUtils.randomAlphanumeric(30);
        resetPasswordService.updateResetPasswordToken(token, email);
        String resetPasswordLink = "http://localhost:8080/api/authenticate/reset-password?token=" + token;
        try {
            UserDTO user = new UserDTO();
            user.setEmail(email);
            user.setName(optionalUser.get().getName());
            user.setSurname(optionalUser.get().getSurname());
            user.setResetPasswordToken(optionalUser.get().getResetPasswordToken());
            rabbitService.sendResetPasswordUser(user, resetPasswordLink);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Si è verificato un errore durante l'invio dell'email: " + e));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Abbiamo inviato un link per il reset password al tuo indirizzo email"));
    }

//    private String getSiteURL(HttpServletRequest request) {
//        String siteURL = request.getRequestURL().toString();
//        return siteURL.replace(request.getServletPath(), "");
//    }




}
