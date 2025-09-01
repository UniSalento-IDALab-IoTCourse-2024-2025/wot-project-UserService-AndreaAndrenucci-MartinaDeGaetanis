package unisalento.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import unisalento.userservice.domain.Admin;
import unisalento.userservice.domain.User;
import unisalento.userservice.repositories.AdminRepository;
import unisalento.userservice.repositories.UserRepository;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserDetails userDetails;

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && "Invalid-Id:Fallback_Method".equals(user.get().getId())) {
            throw new UsernameNotFoundException("Il database non è disponibile");
        }
        if (user.isPresent()) {
            userDetails = org.springframework.security.core.userdetails.User.withUsername(user.get().getEmail()).password(user.get().getPassword()).roles("USER").build();
            return userDetails;
        }

        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            userDetails = org.springframework.security.core.userdetails.User.withUsername(admin.get().getEmail()).password(admin.get().getPassword()).roles("ADMIN").build();
        } else {
            throw new UsernameNotFoundException(email);
        }

        return userDetails;
    }
}
