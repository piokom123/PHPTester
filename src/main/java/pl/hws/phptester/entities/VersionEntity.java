package pl.hws.phptester.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VersionEntity {
    private String version;

    private String releaseDate;

    private String downloadLink;
}
