package com.shturmann.telemetry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.MarkerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by igor on 27.06.14.
 */
public class TaskTelemetry extends TaskGeneral
{
    public Long old_checksum = null;
    private MarshesParser marshes = new MarshesParser();
    private ITopic<ArrayList<Telemetry>> topic;

    public TaskTelemetry(String worker_name, JedisPool jedisPool, HazelcastInstance hz)
    {
        super(worker_name, jedisPool, hz);
    }

    public void process_update() throws MalformedURLException, InterruptedException, ExecutionException
    {
        int time_zone = Config.getInt(worker_name, "time-zone", 240);
        NariadParser nariad;
        Future<NariadParser> future_nariad;
        UnitsParser units;
        Future<UnitsParser> future_units;
        CheckSumParser checksum;
        Future<CheckSumParser> future_checksum;
        if (!Config.getString(worker_name, "synchro", "").isEmpty())
            synchro();
        long t0 = System.currentTimeMillis();
        if (format == Format.XML)
        {
            nariad = new NariadXMLParser();
            URL url = new URL(this.part_url + "/getNariad.php");
            future_nariad = threadPool.submit(new URLParserXMLTask<>(nariad, url, this));
            units = new UnitsXMLParser(DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), worker_name);
            url = new URL(this.part_url + "/getUnits.php");
            future_units = threadPool.submit(new URLParserXMLTask<>(units, url, this));
            url = new URL(this.part_url + "/getChecksum.php?cs_tablename=tbmarshes");
            checksum = new CheckSumXMLParser();
            future_checksum = threadPool.submit(new URLParserXMLTask<>(checksum, url, this));

        }
        else
        {
            nariad = new NariadCSVParser();
            URL url = new URL(this.part_url + "/getNariad.php?fmt=csv");
            future_nariad = threadPool.submit(new URLParserCSVTask<>(nariad, url, this));
            units = new UnitsCSVParser(DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), worker_name);
            url = new URL(this.part_url + "/getUnits.php?fmt=csv");
            future_units = threadPool.submit(new URLParserCSVTask<>(units, url, this));
            url = new URL(this.part_url + "/getChecksum.php?cs_tablename=tbmarshes&fmt=csv");
            checksum = new CheckSumCSVParser();
            future_checksum = threadPool.submit(new URLParserCSVTask<>(checksum, url, this));
        }
        checksum = future_checksum.get();
        log(String.format("request getCheckSum completed %d ms", System.currentTimeMillis() - t0), 2);
        long t1 = System.currentTimeMillis();
        if (old_checksum == null || old_checksum != checksum.get("tbmarshes"))
        {
            Future<MarshesParser> future_marshes;
            MarshesParser marshes;
            if (format == Format.XML)
            {
                URL url = new URL(this.part_url + "/getMarshes.php");
                marshes = new MarshesXMLParser();
                future_marshes = threadPool.submit(new URLParserXMLTask<>(marshes, url, this));
            }
            else
            {
                URL url = new URL(this.part_url + "/getMarshes.php?fmt=csv");
                marshes = new MarshesCSVParser();
                future_marshes = threadPool.submit(new URLParserCSVTask<>(marshes, url, this));
            }
            this.marshes = future_marshes.get();
            log(String.format("request getMarshes completed %d ms", System.currentTimeMillis() - t1), 2);
            old_checksum = checksum.get("tbmarshes");
        }
        nariad = future_nariad.get();
        units = future_units.get();
        log(String.format("requests getNariad, getUnits completed %d ms", System.currentTimeMillis() - t0), 2);
        t0 = System.currentTimeMillis();
        ArrayList<Telemetry> arrayTelemetry = new ArrayList<>();
        int count_ts = 0;
        Jedis redis = jedisPool.getResource();
        try
        {
            Pipeline pipe = redis.pipelined();
            pipe.multi();
            String prefix_redis = Config.getString(worker_name, "prefix-redis", "tn");
            DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
            int time_live = Config.getInt(worker_name, "time-live", 60);
            for (Units unit : units.get())
            {
                Nariad nariad_one = nariad.get(unit.srv_id, unit.uniqueid);
                if (nariad_one != null)
                {
                    String name = this.marshes.get(nariad_one.mr_id);
                    if (name == null)
                    {
                        name = "-";
                        this.old_checksum = null;
                    }
                    String key = String.format("telemetry:%s:%d:%d:%d:%d", prefix_redis, group_code, nariad_one.mr_id, nariad_one.direction, unit.uniqueid);
                    String value = String.format(Locale.US, "%s,%f,%f,%d,%d,%d,%d,%s,%s,%s", fmt.print(unit.timenav), unit.lat, unit.lon, unit.speed, unit.course, unit.transport_type, unit.inv, name, fmt.print(this.start_update), fmt.print(new DateTime()));
                    pipe.set(key, value);
                    pipe.expire(key, time_live);
                    Telemetry telemetry = new Telemetry();
                    telemetry.route = String.format(Locale.US, "%d:%d:%d", group_code, nariad_one.mr_id, nariad_one.direction);
                    telemetry.name = name;
                    telemetry.azimuth = unit.course;
                    telemetry.handicapped = unit.inv != 0;
                    telemetry.lat = unit.lat;
                    telemetry.lon = unit.lon;
                    telemetry.speed = unit.speed;
                    telemetry.time = unit.timenav;
                    telemetry.t1 = start_update;
                    telemetry.t2 = new DateTime();
                    telemetry.transport = unit.transport_type;
                    telemetry.vehicle = Integer.toString(unit.uniqueid);
                    arrayTelemetry.add(telemetry);
                    count_ts++;
                }
            }
            pipe.exec();
        }
        finally
        {
            if (redis.isConnected())
            {
                jedisPool.returnResource(redis);
            }
        }
        log(String.format("insert keys in redis completed %d ms", System.currentTimeMillis() - t0), 2);
        t0 = System.currentTimeMillis();
        String name_topic = String.format(Locale.US, "telemetry.%s.%d", Config.getString(worker_name, "prefix_redis", "tn"), group_code);
        topic = hz.getTopic(name_topic);
        topic.publish(arrayTelemetry);
        log(String.format(Locale.US, "publish in %s %d telemetry %d ms", name_topic, arrayTelemetry.size(), System.currentTimeMillis() - t0), 2);
        if (count_ts > 0)
        {
            logger.info(MarkerFactory.getMarker("nagios"), String.format(Locale.US, "%s Получено %d положений ТС за %d мс", worker_name, count_ts, System.currentTimeMillis() - begin_update));

        }
        else
        {
            logger.warn(MarkerFactory.getMarker("nagios"), String.format(Locale.US, "%s Отсутствует телеметрия, обработано за %d мс", worker_name, System.currentTimeMillis() - begin_update));
        }
    }

    public void synchro() throws MalformedURLException, ExecutionException, InterruptedException
    {
        String[] start_str = Config.getString(worker_name, "synchro", "").split(",");
        int[] start_sec = new int[start_str.length];
        for(int i = 0; i < start_str.length; ++i)
        {
            start_sec[i] = Integer.parseInt(start_str[i]);
        }
        DateTime sys_time;
        if (this.sysinfo == null)
        {
            Future<SysInfoParser> future_sysinfo;
            SysInfoParser sys_info;
            if (format == Format.XML)
            {
                URL url = new URL(part_url + "/getSysInfo.php");
                sys_info = new SysInfoXMLParser();
                future_sysinfo = threadPool.submit(new URLParserXMLTask<>(sys_info, url, this));
            } else
            {
                URL url = new URL(part_url + "/getSysInfo.php?fmt=csv");
                sys_info = new SysInfoCSVParser();
                future_sysinfo = threadPool.submit(new URLParserCSVTask<>(sys_info, url, this));
            }
            sys_info = future_sysinfo.get();
            sys_time = sys_info.get_sys_time();
        }
        else
        {
            sys_time = sysinfo.get_sys_time();
        }
        int sec = sys_time.getSecondOfMinute();
        Long delay = null;
        for (int i : start_sec)
        {
            if (sec <= i)
            {
                delay = (long) (i - sec) * 1000;
                break;
            }
        }
        if (delay == null)
        {
            delay = (long) (start_sec[0] + 60 - sec) * 1000;
        }
        log(String.format(Locale.US, "time tn: %s, %d sec, delay %d ms", sys_time.toString(), sec, delay), 2);
        log(String.format(Locale.US, "delay %s ms for synchro", delay), 1);
        Thread.sleep(delay);
    }
}
