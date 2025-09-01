package unisalento.userservice.dto;

public class ResetPasswordMessageUserDTO {

    private UserDTO user;
    private String resetPasswordLink;

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getResetPasswordLink() {
        return resetPasswordLink;
    }

    public void setResetPasswordLink(String resetPasswordLink) {
        this.resetPasswordLink = resetPasswordLink;
    }
}
