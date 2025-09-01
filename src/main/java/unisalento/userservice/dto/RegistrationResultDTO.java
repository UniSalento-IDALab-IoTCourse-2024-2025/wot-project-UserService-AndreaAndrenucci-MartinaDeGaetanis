package unisalento.userservice.dto;

import java.util.Map;

public class RegistrationResultDTO {

    public final static int OK = 0;
    public final static int EMAIL_ALREADY_EXIST = 1;
    public final static int MISSING_FIELD = 2;
    public final static int ERROR_DOMAIN = 3;
    public static final int SERVER_DOWN = 4;

    private int result;
    private String message;
    private UserDTO user;

    private Map<String, String> errors;


    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
