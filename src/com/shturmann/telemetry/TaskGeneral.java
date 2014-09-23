package com.shturmann.telemetry;

import com.hazelcast.core.HazelcastInstance;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import redis.clients.jedis.JedisPool;

import java.net.URL;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by igor on 30.06.14.
 */


abstract public class TaskGeneral extends TimerTask
{
    public String worker_name;
    protected JedisPool jedisPool;
    protected ExecutorService threadPool = Executors.newCachedThreadPool();
    protected String part_url;
    protected DateTime old_update = null;
    protected DateTime start_update;
    protected HazelcastInstance hz;
    protected int group_code;
    protected String url_str;
    protected int time_zone;
    protected Format format = Format.XML;
    protected SysInfoParser sysinfo = null;
    protected long begin_update;


    protected static final Logger logger = LoggerFactory.getLogger(TaskGeneral.class);

    public TaskGeneral(String worker_name, JedisPool jedisPool, HazelcastInstance hz)
    {
        this.worker_name = worker_name;
        this.jedisPool = jedisPool;
        this.hz = hz;
    }
    @Override
    public void run()
    {
        try
        {
            set_format();
            url_str = Config.getString(worker_name, "url", "/podolsk");
            part_url = String.format("http://%s:%s@%s%s", Config.getString(worker_name, "user", "asipguest"), Config.getString(worker_name, "passwd", "asipguest"), Config.getString(worker_name, "host", "asip.office.transnavi.ru"), url_str);
            group_code = (new Group_codes()).get(url_str.substring(1));
            start_update = new DateTime();
            time_zone = Config.getInt(worker_name, "time-zone", 240);
            log("start update", 1);
            begin_update = System.currentTimeMillis();
            if (Config.getBoolean(worker_name, "getsysinfo", false))
            {
                Future<SysInfoParser> future_sysinfo;
                if (format == Format.XML)
                {
                    URL url = new URL(part_url + "/getSysInfo.php");
                    sysinfo = new SysInfoXMLParser();
                    future_sysinfo = threadPool.submit(new URLParserXMLTask<>(sysinfo, url, this));
                }
                else
                {
                    URL url = new URL(part_url + "/getSysInfo.php?fmt=csv");
                    sysinfo = new SysInfoCSVParser();
                    future_sysinfo = threadPool.submit(new URLParserCSVTask<>(sysinfo, url, this));
                }
                sysinfo = future_sysinfo.get();
                log(String.format("request getSysInfo completed %d ms", System.currentTimeMillis() - begin_update), 2);
                if (old_update == null || sysinfo.get_last_update().isAfter(old_update))
                {
                    log(String.format(Locale.US, "need update old_update=%s last_update=%s", (old_update == null) ? "null": old_update, sysinfo.get_last_update()), 3);
                    process_update();
                    old_update = sysinfo.get_last_update();
                    log(String.format("finish update %d ms", System.currentTimeMillis() - begin_update), 1);
                }
                else
                {
                    log(String.format("update not need %d ms", System.currentTimeMillis() - begin_update), 1);
                }
            }
            else
            {
                sysinfo = null;
                process_update();
                log(String.format("finish update %d ms", System.currentTimeMillis() - begin_update), 1);
            }
        }
        catch (Exception e)
        {
            //log(String.format(Locale.US, "nagios %s", e.getLocalizedMessage()), -1);
            logger.error(MarkerFactory.getMarker("nagios"), String.format(Locale.US, "%s %s", worker_name, e.getLocalizedMessage()), e);
        }
    }
    public void log(String str, int level)
    {
        if (Config.getInt(worker_name, "debug-time", 0) >= level)
        {
            switch (level)
            {
                case -1:
                    logger.error("{} {}", worker_name, str);
                    break;
                case 0:
                    logger.warn("{} {}", worker_name, str);
                    break;
                case 1:
                    logger.info("{} {}", worker_name, str);
                    break;
                case 2:
                    logger.debug("{} {}", worker_name, str);
                    break;
                default:
                    logger.trace("{} {}", worker_name, str);
                    break;
            }
        }
    }

    private void set_format()
    {
        String name_format = Config.getString(worker_name, "format", "xml");
        if (name_format.equalsIgnoreCase("xml"))
        {
            format = Format.XML;
            log("use XML format", 2);
        }
        else
        {
            if (name_format.equalsIgnoreCase("csv"))
            {
                format = Format.CSV;
                log("use CSV format", 2);
            }
            else
            {
                log(String.format(Locale.US, "unknown format %s in configuration, set default format xml", name_format), 0);
                format = Format.XML;
            }
        }
    }

    abstract void process_update() throws Exception;
}
