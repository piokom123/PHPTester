package pl.hws.phptester.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import pl.hws.phptester.connector.Connector;
import pl.hws.phptester.connector.ConnectorResponse;

public class TestPHPNetService {
    @Test
    public void testGetAvailableVersions() {
        Connector connector = Mockito.mock(Connector.class);
        ConnectorResponse response;

        try {
            URL url = getClass().getResource("/phpnet/versions.html");
            Path path = Paths.get(url.toURI());

            response = new ConnectorResponse();
            response.setResponseCode(200);
            response.setContent(new String(Files.readAllBytes(path)));
        } catch (IOException | URISyntaxException ex) {
            System.err.println("Failed to prepare testGetAvailableVersions");

            throw new RuntimeException(ex);
        }

        when(connector.performGET("http://php.net/releases/index.php"))
                .thenReturn(response);

        PHPNetService service = new PHPNetService(connector);

        assertEquals(null, service.getAllVersions());
    }
}
