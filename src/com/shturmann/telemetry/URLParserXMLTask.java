package com.shturmann.telemetry;

import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

/**
 * Created by igor on 27.06.14.
 */
public class URLParserXMLTask<T extends XMLCSVHandler, S extends TaskGeneral> implements Callable<T>
{
    private T handler;
    private URL url;
    private S upTask;
    URLParserXMLTask(T handler, URL url, S upTask)
    {
        this.handler = handler;
        this.url = url;
        this.upTask = upTask;
    }

    @Override
    public T call() throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        final HttpURLConnection host = (HttpURLConnection) url.openConnection();
        if (url.getUserInfo() != null) {
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
            host.setRequestProperty("Authorization", basicAuth);
        }
        host.setRequestProperty("Accept-Encoding", "gzip");
        host.setConnectTimeout(Config.getInt(upTask.worker_name, "http-timeout", 120000));
        host.setReadTimeout(Config.getInt(upTask.worker_name, "read-timeout", 120000));
        if (Config.getInt(upTask.worker_name, "debug-time", 0) > 2)
        {
            long t0 = System.currentTimeMillis();
            final StringBuilder buf = new StringBuilder();
            InputStream is = "gzip".equals(host.getContentEncoding()) ? new GZIPInputStream(host.getInputStream()) : host.getInputStream();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")))
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    buf.append(line);
                }
            }
            upTask.log(String.format("download [%s] in %d ms", url.toString(), System.currentTimeMillis() - t0), 3);
            t0 = System.currentTimeMillis();
            saxParser.parse( new InputSource(new StringReader(buf.toString())), handler );
            upTask.log(String.format("parsed [%s] in %d ms", url.toString(), System.currentTimeMillis() - t0), 3);
        }
        else
        {
            InputStream is = "gzip".equals(host.getContentEncoding()) ? new GZIPInputStream(host.getInputStream()) : host.getInputStream();
            saxParser.parse( is, handler );
        }
        return handler;
    }
}
