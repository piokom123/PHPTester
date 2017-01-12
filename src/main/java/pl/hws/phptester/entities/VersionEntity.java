package pl.hws.phptester.entities;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class VersionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public VersionEntity() {
        
    }

    public VersionEntity(String version, String releaseDate, String downloadLink) {
        this.version = version;
        this.releaseDate = releaseDate;
        this.downloadLink = downloadLink;
    }

    private String version;

    private String releaseDate;

    private String downloadLink;
}
