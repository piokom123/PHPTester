package pl.hws.phptester.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceResultEntity {
    public ServiceResultEntity() {
        
    }

    public ServiceResultEntity(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public ServiceResultEntity(Boolean status, String message, String log) {
        this.status = status;
        this.message = message;
        this.log = log;
    }

    private Boolean status;

    private String message;

    private String log;
}
