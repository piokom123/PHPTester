package pl.hws.phptester.connector;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import pl.hws.phptester.Context;

public class Connector implements AutoCloseable {
    private final Integer connectionTimeout = 17000;
    private final Integer maxTriesCount = 3;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final CloseableHttpClient client;

    @Getter
    private final List<String> browsers = new ArrayList<String>() {{
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; de-DE; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 GTB5 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.1; de; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        add("Opera/9.80 (X11; Linux i686; U; de) Presto/2.2.15 Version/10.00");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; UCLBC)");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30618)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; it; rv:1.9.0.13) Gecko/2009073022 Firefox/2.0.0.17;MEGAUPLOAD 1.0 ImageShackToolbar/5.0.0 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; pl; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.16) Gecko/20080702 Firefox/2.0.0.16");
        add("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.13) Gecko/2009080315 Linux Mint/7 (Gloria) Firefox/3.0.13");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.0.13) Gecko/2009080200 SUSE/3.0.13-0.1.2 Firefox/3.0.13");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.13) Gecko/2009073022 Firefox/3.0.13 (.NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; AllgÃ¤uer Medien Zentrum)");
        add("Mozilla/5.0 (X11; U; Linux x86_64; de; rv:1.9.0.14) Gecko/2009090217 Ubuntu/9.04 (jaunty) Firefox/3.0.14");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; rv:1.9.0.14) Gecko/2009090217 Firefox/3.0.14");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.0.14) Gecko/2009082707 Firefox/3.0.14 (.NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.1.3) Gecko/20090909 SUSE/3.5.3-1.2 Firefox/3.5.3");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.590; .NET CLR 3.5.20706)");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; Mozilla/4.0 (compatible; MSIE 8.0; Win32; GMX); .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; de; rv:1.8.0.12) Gecko/20070508 Firefox/1.5.0.12");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; GTB6.3)");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.0.14) Gecko/2009090216 Ubuntu/8.10 (intrepid) Firefox/3.0.14");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 GTB5 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; de; rv:1.9.0.14) Gecko/2009082706 Firefox/3.0.14");
        add("Mozilla/4.0 (compatible; MSIE 7.0; TOB 6.07; Windows NT 5.1; Trident/4.0; GTB6; .NET CLR 1.1.4322; MSN Optimized;DE; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; T-Brand-Final)");
        add("Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.207.0 Safari/532.0");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.21 Safari/532.0");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; es-AR; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.1.3) Gecko/20090824 YFF35 Firefox/3.5.3");
        add("Mozilla/4.05 [en] (X11; I; OSF1 V4.0 alpha)");
        add("Mozilla/5.0 (X11; U; Linux i686; de-DE; rv:1.9.1.1) Gecko/20090714 SUSE/3.5.1-1.1 Firefox/3.5.1");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible;MSIE 5.5; Windows NT 5.0; H010818)");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.0.14) Gecko/2009082707 Firefox/3.0.14 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.1 (KHTML, like Gecko) Chrome/4.0.213.1 Safari/532.1");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.0.14) Gecko/2009090216 Ubuntu/9.04 (jaunty) Firefox/3.0.14 FirePHP/0.3");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; InfoPath.1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.1; .NET CLR 3.0.04506.648)");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.1.5pre) Gecko/20091007 Ubuntu/9.04 (jaunty) Firefox/3.5");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2; MS-RTC LM 8; .NET CLR 3.0.04506.648)");
        add("Mozilla/4.0 (compatible;  MSIE 6.0;  Windows NT 5.2;  SLCC1;  .NET CLR 1.1.4325;  .NET CLR 2.0.40607;  .NET CLR 3.0.30729;  .NET CLR 3.5.30707;  InfoPath.2)");
        add("Opera/9.80 (Windows NT 6.0; U; de) Presto/2.2.15 Version/10.00");
        add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3");
        add("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.0.14) Gecko/2009090216 Firefox/3.0.14");
        add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; CIO CU; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.1; MS-RTC LM 8)");
        add("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.3) Gecko/20090919 Gentoo Firefox/3.5.3");
        add("Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.8.1.20) Gecko/20081217 Firefox/2.0.0.20 (.NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows NT 6.2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1553.0 Safari/537.36 SUSE/30.0.1553.0");
        add("Mozilla/5.0 (Windows NT 6.1; rv:23.0) Gecko/20100101 Firefox/23.0 AlexaToolbar/alxf-2.18");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; MS-RTC LM 8; .NET4.0C; .NET4.0E)");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.66 (KHTML, like Gecko) Version/6.1 Safari/537.66");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.14 Safari/537.36");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2;  SLCC1;  .NET CLR 1.1.4325;  .NET CLR 2.0.40607;  .NET CLR 3.0.04506.648)");
        add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.57 Safari/537.36 OPR/16.0.1196.73");
        add("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 5.1; rv:23.0) Gecko/20130405 Firefox/23.0");
        add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;  SLCC1;  .NET CLR 1.1.4325;  .NET CLR 2.0.40607;  .NET CLR 3.0.30729;  .NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.65 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        add("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)");
        add("Opera/9.80 (X11; Linux x8664) Presto/2.12.388 Version/12.16");
        add("Opera/12.80 (Windows NT 5.1; U; en) Presto/2.10.289 Version/12.02");
        add("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:21.0) Gecko/20100101 Firefox/21.0");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.37 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.65 Safari/537.36");
        add("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/6.0)");
        add("Opera/9.80 (Windows NT 6.1) Presto/2.12.388 Version/12.16");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.186 Safari/535.1");
        add("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36 CoolNovo/2.0.9.20");
        add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.63 Safari/537.31 Mozilla/5.12345 (Windows 8.23; U; Windows 8.45; es; rv:1.9.1.899) Gecko/2009989768 Firefox/3.5.09887");
        add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        add("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux ppc; rv:5.0) Gecko/20100101 Firefox/5.0");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0");
        add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 6.2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20130309 Firefox/19.0");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0; +http://netzware.net/) Gecko/20100101 Firefox/25.0");
        add("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20100101 Firefox/24.0");
    }};

    public Connector() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(connectionTimeout)
            .setSocketTimeout(connectionTimeout)
            .setConnectTimeout(connectionTimeout)
            .setRedirectsEnabled(true)
            .setRelativeRedirectsAllowed(true)
            .setMaxRedirects(3)
            .build();

        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(browsers.get(ThreadLocalRandom.current().nextInt(browsers.size())))
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(new BasicCookieStore());

        client = builder.build();
    }

    @Override
    public void close() {
        try {
            client.close();

            executor.shutdown();
        } catch (IOException ex) {
            Context.getInstance().showError("Can't close Connector object");
            Context.getInstance().showError(ex);
        }
    }

    public ConnectorResponse performGET(String url) {
        return performGET(url, url, null, 1);
    }

    public ConnectorResponse performGET(String url, String referer) {
        return performGET(url, referer, null, 1);
    }

    public ConnectorResponse performGET(String url, String referer, Map<String, String> headers, Integer triesCount) {
        Context.getInstance().showMessage("Performing GET to " + url + " (try: " + Integer.toString(triesCount) + ")");

        ConnectorResponse result = new ConnectorResponse();

        HttpGet connection = new HttpGet(url);

        for (Header header : getBasicHeaders()) {
            connection.addHeader(header);
        }

        connection.setHeader("Referer", referer);

        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                connection.setHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            Future<Boolean> futureResponse = executor.submit(() -> {
                Context.getInstance().showMessage("Starting new connection for GET");

                HttpEntity entity = null;

                HttpClientContext clientContext = HttpClientContext.create();

                try (
                    CloseableHttpResponse response = client.execute(connection, clientContext);
                ) {
                    if (response == null) {
                        return false;
                    }

                    entity = response.getEntity();

                    if (entity == null) {
                        return false;
                    }

                    result.setResponseCode(response.getStatusLine().getStatusCode());

                    if (result.getResponseCode().equals(307)) {
                        Context.getInstance().showError("307 redirect!");

                        return false;
                    }

                    String content = EntityUtils.toString(entity);

                    result.setContent(content);

                    String location = connection.getURI().toString();

                    if (result.getResponseCode().equals(301) || result.getResponseCode().equals(302)) {
                        location = response.getLastHeader("Location").getValue();
                    }

                    RedirectLocations locations = (RedirectLocations) clientContext.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
                    if (locations != null) {
                        location = locations.getAll().get(locations.getAll().size() - 1).toString();
                    }

                    result.setLatestRedirectUrl(location);

                    Context.getInstance().showMessage("Connection for GET should close soon");

                    return true;
                } catch (NoHttpResponseException ex) {
                    Context.getInstance().showError("NoHttpResponse received");

                    return false;
                } catch (SocketTimeoutException | ConnectTimeoutException ex) {
                    Context.getInstance().showError("Timeout occured");

                    return false;
                } catch (IOException | ParseException ex) {
                    Context.getInstance().showError("Request failed");
                    Context.getInstance().showError(ex);

                    return false;
                } finally {
                    if (entity != null) {
                        try {
                            EntityUtils.consume(entity);
                        } catch (IOException ex) {
                            Context.getInstance().showError("Can't release response entity");
                            Context.getInstance().showError(ex);
                        }
                    }
                }
            });

            Boolean status = futureResponse.get(connectionTimeout, TimeUnit.MILLISECONDS);

            if (status) {
                return result;
            }

            throw new Exception("Request failed, passing to catch block");
        } catch (Exception ex) {
            if (triesCount <= maxTriesCount) {
                Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on GET. Retrying.");

                if (ex instanceof TimeoutException) {
                    Context.getInstance().showError("Timeout occured");
                } else {
                    Context.getInstance().showError(ex);
                }

                return performGET(url, referer, headers, ++triesCount);
            }

            Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on GET. Max retries count reached, stopping.");

            if (ex instanceof TimeoutException) {
                Context.getInstance().showError("Timeout occured");
            } else {
                Context.getInstance().showError(ex);
            }

            return result;
        }
    }

    public ConnectorResponse performHEAD(String url) {
        return performHEAD(url, url, null, 1);
    }

    public ConnectorResponse performHEAD(String url, String referer) {
        return performHEAD(url, referer, null, 1);
    }

    public ConnectorResponse performHEAD(String url, String referer, Map<String, String> headers, Integer triesCount) {
        Context.getInstance().showMessage("Performing HEAD to " + url + " (try: " + Integer.toString(triesCount) + ")");

        ConnectorResponse result = new ConnectorResponse();

        HttpHead connection = new HttpHead(url);

        for (Header header : getBasicHeaders()) {
            connection.addHeader(header);
        }

        connection.setHeader("Referer", referer);

        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                connection.setHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            Future<Boolean> futureResponse = executor.submit(() -> {
                Context.getInstance().showMessage("Starting new connection for HEAD");

                long startTime = System.currentTimeMillis();

                HttpClientContext clientContext = HttpClientContext.create();

                try (
                    CloseableHttpResponse response = client.execute(connection, clientContext);
                ) {
                    if (response == null) {
                        return false;
                    }

                    result.setResponseCode(response.getStatusLine().getStatusCode());

                    if (result.getResponseCode().equals(307)) {
                        Context.getInstance().showError("307 redirect!");

                        return false;
                    }

                    result.setResponseTime(System.currentTimeMillis() - startTime);

                    String location = connection.getURI().toString();

                    if (result.getResponseCode().equals(301) || result.getResponseCode().equals(302)) {
                        location = response.getLastHeader("Location").getValue();
                    }

                    RedirectLocations locations = (RedirectLocations) clientContext.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
                    if (locations != null) {
                        location = locations.getAll().get(locations.getAll().size() - 1).toString();
                    }

                    result.setLatestRedirectUrl(location);
                    result.setContentLength(Integer.parseInt(response.getLastHeader("Content-length").getValue()));

                    Context.getInstance().showMessage("Connection for GET should close soon");

                    return true;
                } catch (NoHttpResponseException ex) {
                    Context.getInstance().showError("NoHttpResponse received");

                    return false;
                } catch (SocketTimeoutException | ConnectTimeoutException ex) {
                    Context.getInstance().showError("Timeout occured");

                    return false;
                } catch (IOException | ParseException ex) {
                    Context.getInstance().showError("Request failed");
                    Context.getInstance().showError(ex);

                    return false;
                }
            });

            Boolean status = futureResponse.get(connectionTimeout, TimeUnit.MILLISECONDS);

            if (status) {
                return result;
            }

            throw new Exception("Request failed, passing to catch block");
        } catch (Exception ex) {
            if (triesCount <= maxTriesCount) {
                Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on HEAD. Retrying.");

                if (ex instanceof TimeoutException) {
                    Context.getInstance().showError("Timeout occured");
                } else {
                    Context.getInstance().showError(ex);
                }

                return performGET(url, referer, headers, ++triesCount);
            }

            Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on HEAD. Max retries count reached, stopping.");

            if (ex instanceof TimeoutException) {
                Context.getInstance().showError("Timeout occured");
            } else {
                Context.getInstance().showError(ex);
            }

            return result;
        }
    }

    public ConnectorResponse performBinaryGET(String url) {
        return performBinaryGET(url, null, null, url, 1);
    }

    public ConnectorResponse performBinaryGET(String url, Path destination, SimpleDoubleProperty downloadProgress) {
        return performBinaryGET(url, destination, downloadProgress, url, 1);
    }

    public ConnectorResponse performBinaryGET(String url, Path destination, SimpleDoubleProperty downloadProgress, Integer triesCount) {
        return performBinaryGET(url, destination, downloadProgress, url, triesCount);
    }

    public ConnectorResponse performBinaryGET(String url, Path destination, SimpleDoubleProperty downloadProgress, String referer, Integer triesCount) {
        Context.getInstance().showMessage("Performing binary GET to " + url + " (try: " + Integer.toString(triesCount) + ")");

        ConnectorResponse result = new ConnectorResponse();

        try {
            HttpGet connection = new HttpGet(url);

            connection = (HttpGet) this.addBasicHeaders(connection);

            connection.setHeader("Referer", referer);

            HttpClientContext clientContext = HttpClientContext.create();

            CloseableHttpResponse response = this.client.execute(connection, clientContext);

            result.setContentLength(Integer.parseInt(response.getLastHeader("Content-length").getValue()));

            HttpEntity entity = response.getEntity();

            if (destination == null) {
                try (InputStream buffer = new BufferedInputStream(entity.getContent());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                    byte[] byteChunk = new byte[4096];
                    int n;

                    while ((n = buffer.read(byteChunk)) > 0) {
                        baos.write(byteChunk, 0, n);
                    }

                    result.setByteContent(baos.toByteArray());
                }
            } else {
                Path tempDestination = destination.getParent().resolve(destination.getFileName() + ".tmp");

                try (InputStream buffer = new BufferedInputStream(entity.getContent());
                        OutputStream writer = Files.newOutputStream(tempDestination);) {
                    Integer downloaded = 0;
                    byte[] byteChunk = new byte[4096];
                    int n;

                    while ((n = buffer.read(byteChunk)) > 0) {
                        writer.write(byteChunk, 0, n);

                        downloaded += n;

                        Double progress = downloaded.doubleValue() / result.getContentLength().doubleValue();

                        Platform.runLater(() -> {
                            downloadProgress.set(progress);
                        });
                    }
                }

                Files.move(tempDestination, destination);
            }

            EntityUtils.consume(entity);

            String location = connection.getURI().toString();

            result.setResponseCode(response.getStatusLine().getStatusCode());

            if (result.getResponseCode().equals(301) || result.getResponseCode().equals(302)) {
                location = response.getLastHeader("Location").getValue();
            }

            RedirectLocations locations = (RedirectLocations) clientContext.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
            if (locations != null) {
                location = locations.getAll().get(locations.getAll().size() - 1).toString();
            }

            result.setLatestRedirectUrl(location);

            return result;
        } catch (IOException ex) {
            if (triesCount <= maxTriesCount) {
                Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on binary GET. Retrying.");
                Context.getInstance().showError(ex);

                return performBinaryGET(url, destination, downloadProgress, referer, ++triesCount);
            }

            Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on binary GET. Max retries count reached, stopping. Message: " + ex.getMessage());

            return null;
        }
    }

    public ConnectorResponse performPOST(String url, List<NameValuePair> parameters) {
        return performPOST(url, parameters, null, null, 1);
    }

    public ConnectorResponse performPOST(String url, List<NameValuePair> parameters, Map<String, File> fileParameters) {
        return performPOST(url, parameters, fileParameters, null, 1);
    }

    
    public ConnectorResponse performPOST(String url, List<NameValuePair> parameters, Map<String, File> fileParameters, String referer) {
        return performPOST(url, parameters, fileParameters, referer, 1);
    }

    public ConnectorResponse performPOST(String url, List<NameValuePair> parameters, Map<String, File> fileParameters, String referer, Integer triesCount) {
        ConnectorResponse result = new ConnectorResponse();

        try {
            HttpPost connection = new HttpPost(url);

            connection = (HttpPost) this.addBasicHeaders(connection);

            if (referer != null) {
                connection.setHeader("Referer", referer);
            } else {
                connection.setHeader("Referer", url);
            }

            if (fileParameters != null && !fileParameters.isEmpty()) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setCharset(Charset.forName("UTF-8"));

                for (NameValuePair pair : parameters) {
                    builder.addPart(pair.getName(), new StringBody(pair.getValue(), ContentType.TEXT_PLAIN));
                }

                for (Entry<String, File> entry : fileParameters.entrySet()) {
                    builder.addPart(entry.getKey(), new FileBody(entry.getValue()));
                }

                connection.setEntity(builder.build());
            } else {
                connection.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            }

            HttpClientContext clientContext = HttpClientContext.create();

            CloseableHttpResponse response = this.client.execute(connection, clientContext);

            HttpEntity entity = response.getEntity();

            result.setResponseCode(response.getStatusLine().getStatusCode());

            String content = null;
            try {
                content = new Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A").next();
            } catch(NoSuchElementException ex) {
                if (triesCount <= maxTriesCount) {
                    Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on POST. Retrying.");

                    return performPOST(url, parameters, fileParameters, referer, ++triesCount);
                }

                Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on POST. Max retries count reached, stopping.");
            }

            result.setContent(content);

            String location = connection.getURI().toString();

            if (result.getResponseCode().equals(301) || result.getResponseCode().equals(302)) {
                location = response.getLastHeader("Location").getValue();
            }

            RedirectLocations locations = (RedirectLocations) clientContext.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
            if (locations != null) {
                location = locations.getAll().get(locations.getAll().size() - 1).toString();
            }

            result.setLatestRedirectUrl(location);
            result.setContentLength(Integer.parseInt(response.getLastHeader("Content-length").getValue()));

            EntityUtils.consume(entity);

            return result;
        } catch (IOException | NoSuchElementException ex) {
            if (triesCount <= maxTriesCount) {
                Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on POST. Retrying.");

                return performPOST(url, parameters, fileParameters,referer, ++triesCount);
            }

            Context.getInstance().showError("Try number " + Integer.toString(triesCount) + " failed on POST. Max retries count reached, stopping.");

            return result;
        }
    }

    private HttpRequestBase addBasicHeaders(HttpRequestBase connection) {
        connection.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        connection.addHeader("Accept-Language", "en-US,en;q=0.8,pl;q=0.6");
        connection.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        return connection;
    }

    private Header[] getBasicHeaders() {
        Header[] headers = new Header[3];

        headers[0] = new BasicHeader("Accept-Encoding", "gzip, deflate, sdch");
        headers[1] = new BasicHeader("Accept-Language", "en-US,en;q=0.8,pl;q=0.6");
        headers[2] = new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        return headers;
    }
}