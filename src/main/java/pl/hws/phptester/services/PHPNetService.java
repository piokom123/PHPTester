package pl.hws.phptester.services;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.hws.phptester.Context;
import pl.hws.phptester.connector.Connector;
import pl.hws.phptester.connector.ConnectorResponse;
import pl.hws.phptester.entities.VersionEntity;

public class PHPNetService {
    private final Connector connector;

    public PHPNetService(Connector connector) {
        this.connector = connector;
    }

    public List<VersionEntity> getAllVersions() {
        List<VersionEntity> results = new ArrayList<>();

        ConnectorResponse response = fetchVersionsHTML();

        Document document = Jsoup.parse(response.getContent());

        Elements elements = document.getElementsByTag("h2");

        if (elements.isEmpty()) {
            Context.getInstance().showError("No versions found");

            return results;
        }

        for (Element element : elements) {
            Element ulElement = element.nextElementSibling();

            VersionEntity entity = new VersionEntity();

            entity.setReleaseDate(ulElement.child(0).text().replace("Released: ", ""));
            entity.setVersion(element.text());

            for (Element aElement : ulElement.getElementsByTag("a")) {
                if ((aElement.text().contains("source") || aElement.text().contains("tar.gz"))
                        && aElement.attr("href").contains("tar.gz")) {
                    entity.setDownloadLink(aElement.attr("href"));
                }
            }

            results.add(entity);
        }

        return results;
    }

    private ConnectorResponse fetchVersionsHTML() {
        ConnectorResponse response = connector.performGET("http://php.net/releases/index.php");

        if (response == null
                || response.getResponseCode() != 200
                || response.getContent() == null
                || response.getContent().isEmpty()) {
            Context.getInstance().showError("Failed to fetch versions list");
        }

        return response;
    }
}
