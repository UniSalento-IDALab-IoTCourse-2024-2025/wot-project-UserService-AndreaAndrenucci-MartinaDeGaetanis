package unisalento.userservice.dto;

import java.util.List;

public class UsersListDTO {
    private List<UserDTO> users;

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
