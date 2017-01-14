package pl.hws.phptester.connector;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorResponse {
    private Integer responseCode;

    private Long responseTime;

    private String content;

    private Integer contentLength;

    private byte[] byteContent;

    private String latestRedirectUrl;

    public Boolean isBlocked() {
        if (content.contains("Access Denied")
                || content.contains("Proibido o Acesso")
                || content.contains("Acceso Denegado")
                || content.contains("The requested URL could not be retrieved")
                || content.contains("<title>ERROR: Cache Access Denied</title>")
                || content.contains("Na tentativa de recuperar a URL")
                || content.contains("Site Bloqueado")
                || content.contains("The URL you requested has been blocked")
                || content.contains("CSA-proxy web")
                || content.contains("This page error has the following parameters")
                || content.contains("DNS resolving failed")
                || content.contains("ERR_ACCESS_DENIED")
                || content.contains("Squid did not receive any data for this request")
                || content.contains("Welcome To Zscaler Directory Authentication")) {
            return true;
        }

        return false;
    }
}
