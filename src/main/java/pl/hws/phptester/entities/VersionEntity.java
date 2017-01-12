package pl.hws.phptester.entities;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.hws.phptester.enums.VersionStatusEnum;

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

    private VersionStatusEnum status;

    public String getStatusText() {
        if (status == null) {
            return "unknown";
        }

        switch (status) {
            case NOT_FETCHED:
                return "not fetched";
            case UNPACKED:
                return "unpacked";
            case FETCHED:
                return "fetched";
            case COMPILED:
                return "compiled";
            default:
                return "unknown";
        }
    }
}
