package com.shturmann.telemetry;

import au.com.bytecode.opencsv.CSVReader;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

/**
 * Created by igor on 15.08.14.
 */
public class URLParserCSVTask<T extends XMLCSVHandler, S extends TaskGeneral> implements Callable<T>
{
    private T handler;
    private S upTask;
    private URL url;

    public URLParserCSVTask(T handler, URL url, S upTask)
    {
        this.handler=handler;
        this.url = url;
        this.upTask = upTask;
    }

    @Override
    public T call() throws IOException
    {
        long t0 = System.currentTimeMillis();
        upTask.log(String.format("start [%s]", url.toString()), 3);
        final HttpURLConnection host = (HttpURLConnection) url.openConnection();
        upTask.log(String.format("create HttpConnection [%s]", url.toString()), 3);
        if (url.getUserInfo() != null)
        {
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
            host.setRequestProperty("Authorization", basicAuth);
        }
        host.setRequestProperty("Accept-Encoding", "gzip");
        upTask.log(String.format("set Request Property [%s]", url.toString()), 3);
        host.setConnectTimeout(Config.getInt(upTask.worker_name, "http-timeout", 120000));
        host.setReadTimeout(Config.getInt(upTask.worker_name, "read-timeout", 120000));
        upTask.log(String.format("set timeout [%s]", url.toString()), 3);
        InputStream is = "gzip".equals(host.getContentEncoding()) ? new GZIPInputStream(host.getInputStream()) : host.getInputStream();
        upTask.log(String.format("get InputStream [%s]", url.toString()), 3);
        CSVReader reader;
        if (Config.getInt(upTask.worker_name, "debug-time", 0) > 2)
        {
            final StringBuilder buf = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")))
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    buf.append(line);
                    buf.append('\n');
                }
            }
            upTask.log(String.format("download [%s] in %d ms", url.toString(), System.currentTimeMillis() - t0), 3);
            t0 = System.currentTimeMillis();
            reader = new CSVReader(new StringReader(buf.toString()));
        }
        else
        {
            upTask.log(String.format("download [%s] in %d ms", url.toString(), System.currentTimeMillis() - t0), 3);
            reader = new CSVReader(new InputStreamReader(is, "UTF-8"));
        }
        handler.start_doc();
        String[] nextString = reader.readNext(); //remove header
        if (nextString != null)
        {
            while ((nextString = reader.readNext()) != null)
            {
                handler.next(nextString);
            }
        }
        handler.end_doc();
        upTask.log(String.format("parsed [%s] in %d ms", url.toString(), System.currentTimeMillis() - t0), 3);
        return handler;
    }
}
