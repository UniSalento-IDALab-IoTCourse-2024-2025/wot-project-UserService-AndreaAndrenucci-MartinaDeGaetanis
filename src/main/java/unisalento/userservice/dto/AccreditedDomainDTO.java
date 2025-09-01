package unisalento.userservice.dto;

public class AccreditedDomainDTO {

    private String id;
    private String accreditedDomain;
    private int numClients;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccreditedDomain() {
        return accreditedDomain;
    }

    public void setAccreditedDomain(String accreditedDomain) {
        this.accreditedDomain = accreditedDomain;
    }

    public int getNumClients() {
        return numClients;
    }

    public void setNumClients(int numClients) {
        this.numClients = numClients;
    }
}
