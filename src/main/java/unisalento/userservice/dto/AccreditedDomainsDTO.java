package unisalento.userservice.dto;

import java.util.List;

public class AccreditedDomainsDTO {

    private List<AccreditedDomainDTO> accreditedDomainList;

    public List<AccreditedDomainDTO> getAccreditedDomainList() {
        return accreditedDomainList;
    }

    public void setAccreditedDomainList(List<AccreditedDomainDTO> accreditedDomainList) {
        this.accreditedDomainList = accreditedDomainList;
    }
}
