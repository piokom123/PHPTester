package pl.hws.phptester.test.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import pl.hws.phptester.connector.Connector;
import pl.hws.phptester.connector.ConnectorResponse;
import pl.hws.phptester.entities.VersionEntity;
import pl.hws.phptester.services.PHPNetService;

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

        List<VersionEntity> results = new ArrayList<>();

        results.add(new VersionEntity("7.0.13", "10 Nov 2016", "http://php.net/get/php-7.0.13.tar.gz/from/this/mirror"));
        results.add(new VersionEntity("7.0.12", "13 Oct 2016", "http://php.net/get/php-7.0.12.tar.gz/from/this/mirror"));
        results.add(new VersionEntity("7.0.11", "15 Sep 2016", "http://php.net/get/php-7.0.11.tar.gz/from/this/mirror"));
        results.add(new VersionEntity("7.0.10", "18 Aug 2016", "http://php.net/get/php-7.0.10.tar.gz/from/this/mirror"));
        results.add(new VersionEntity("4.3.2", "29 May 2003", "http://museum.php.net/php4/php-4.3.2.tar.gz"));
        results.add(new VersionEntity("4.0.1", "28 June 2000", "http://museum.php.net/php4/php-4.0.1pl2.tar.gz"));
        results.add(new VersionEntity("4.0.0", "22 May 2000", "http://museum.php.net/php4/php-4.0.0.tar.gz"));
        results.add(new VersionEntity("3.0.x", "20 Oct 2000", "http://museum.php.net/php3/php-3.0.18.tar.gz"));

        assertEquals(results, service.getAllVersions());
    }
}
