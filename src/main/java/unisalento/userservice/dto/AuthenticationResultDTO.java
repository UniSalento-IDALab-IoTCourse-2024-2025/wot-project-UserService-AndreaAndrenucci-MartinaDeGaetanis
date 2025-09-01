package unisalento.userservice.dto;

public class AuthenticationResultDTO {

    public final static int OK = 0;
    public final static int EMAIL_NOT_FOUND = 1;
    public final static int INCORRECT_PASSWORD = 2;
    public final static int GENERIC_ERROR = 99;
    public static final int NOT_ENABLED = 3;

    private String jwt;
    private String message;
    private int result;

    public AuthenticationResultDTO() {
        this.jwt = "";
        this.message = "";
        this.result = -1;
    }

    public void setResult(int result) {
        this.result = result;
        switch (result){
            case EMAIL_NOT_FOUND: message = "Email non trovata"; break;
            case INCORRECT_PASSWORD: message = "Password incorretta"; break;
            case OK: message = "Accesso effettuato con successo"; break;
            case GENERIC_ERROR: message = "Errore! Riprovare"; break;
            case NOT_ENABLED: message = "Utente non abilitato"; break;
        }
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResult() {
        return result;
    }
}
