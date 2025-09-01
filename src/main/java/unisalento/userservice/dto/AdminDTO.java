package unisalento.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminDTO {
    private String id;
    @NotBlank(message = "'Nome' non può essere un campo vuoto")
    private String name;
    @NotBlank(message = "'Cognome' non può essere un campo vuoto")
    private String surname;
    @NotBlank(message = "'Email' non può essere un campo vuoto")
    @Email(message = "L'indirizzo email deve essere valido")
    private String email;
    @NotBlank(message = "'Password' non può essere un campo vuoto")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri")
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @NotBlank(message = "'Nome' non può essere un campo vuoto") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "'Nome' non può essere un campo vuoto") String name) {
        this.name = name;
    }

    public @NotBlank(message = "'Cognome' non può essere un campo vuoto") String getSurname() {
        return surname;
    }

    public void setSurname(@NotBlank(message = "'Cognome' non può essere un campo vuoto") String surname) {
        this.surname = surname;
    }

    public @NotBlank(message = "'Email' non può essere un campo vuoto") @Email(message = "L'indirizzo email deve essere valido") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "'Email' non può essere un campo vuoto") @Email(message = "L'indirizzo email deve essere valido") String email) {
        this.email = email;
    }

    public @NotBlank(message = "'Password' non può essere un campo vuoto") @Size(min = 8, message = "La password deve contenere almeno 8 caratteri") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "'Password' non può essere un campo vuoto") @Size(min = 8, message = "La password deve contenere almeno 8 caratteri") String password) {
        this.password = password;
    }
}
