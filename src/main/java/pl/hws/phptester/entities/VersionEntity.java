package pl.hws.phptester.entities;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VersionEntity implements Serializable {
    private String version;

    private String releaseDate;

    private String downloadLink;
}
