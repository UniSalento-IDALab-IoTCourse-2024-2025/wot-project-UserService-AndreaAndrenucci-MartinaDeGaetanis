package unisalento.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unisalento.userservice.domain.User;
import unisalento.userservice.service.ResetPasswordService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Controller
@RequestMapping
public class ResetPasswordController {

    @Autowired
    ResetPasswordService resetPasswordService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/api/authenticate/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {

        User user = resetPasswordService.getByRestPasswordTokenUser(token);

        if (user == null) {
            return "Invalid Token";
        }

        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/DigitAIR_favicon.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            model.addAttribute("faviconBase64", "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("faviconBase64", null);
        }

        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/DigitAIR.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            model.addAttribute("logoBase64", "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("logoBase64", null);
        }

        model.addAttribute("token", token);
        return "reset-password-form";
    }


    @PostMapping("/api/authenticate/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {

        User user = resetPasswordService.getByRestPasswordTokenUser(token);

        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/DigitAIR_favicon.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            model.addAttribute("faviconBase64", "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("faviconBase64", null);
        }

        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/DigitAIR.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            model.addAttribute("logoBase64", "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("logoBase64", null);
        }

        if (user != null) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                resetPasswordService.updatePasswordUser(user, password);
                return "ok-reset-password";
            } else {
                model.addAttribute("error", "La nuova password non può essere uguale alla precedente");
            }
        } else {
            return "not-reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password-form";
    }

}
