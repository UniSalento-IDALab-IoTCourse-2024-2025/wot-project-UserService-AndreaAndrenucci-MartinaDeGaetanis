package unisalento.userservice.dto;

import java.util.List;

public class AdminsListDTO {

    private List<AdminDTO> adminsList;

    public List<AdminDTO> getAdminsList() {
        return adminsList;
    }

    public void setAdminsList(List<AdminDTO> adminsList) {
        this.adminsList = adminsList;
    }
}
