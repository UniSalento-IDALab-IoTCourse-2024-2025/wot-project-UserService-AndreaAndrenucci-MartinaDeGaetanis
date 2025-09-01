package unisalento.userservice.dto;

import java.util.Map;

public class ChangePasswordResultDTO {

    public final static int SUCCESS = 0;
    public final static int MISSING_OLD_PASSWORD = 1;
    public final static int MISSING_NEW_PASSWORD = 2;
    public final static int MISSING_CONFIRMATION_PASSWORD = 3;
    public static final int INCORRECT_CONFIRMATION_PASSWORD = 4;
    public static final int LENGHT_ERROR = 5;
    public static final int WRONG_OLD_PASSWORD = 6;
    public static final int SAME_PASSWORD = 7;
    public static final int USER_NOT_FOUND = 8;

    private int result;
    private String message;


    public void setResult(int result) {
        this.result = result;
        switch (result){
            case SUCCESS: message = "Password cambiata con successo"; break;
            case MISSING_OLD_PASSWORD: message = "La password attuale è obbligatoria"; break;
            case MISSING_NEW_PASSWORD: message = "La nuova password è obbligatoria"; break;
            case MISSING_CONFIRMATION_PASSWORD: message = "La conferma della nuova password è obbligatoria"; break;
            case INCORRECT_CONFIRMATION_PASSWORD: message = "La nuova password e la conferma non coincidono"; break;
            case LENGHT_ERROR: message = "La password deve essere di almeno 8 caratteri"; break;
            case WRONG_OLD_PASSWORD: message = "La password vecchia non è corretta"; break;
            case SAME_PASSWORD: message = "La nuova password deve essere diversa da quella vecchia"; break;
            case USER_NOT_FOUND: message = "L'utente non esiste";
        }
    }

    public int getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
