package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Locale;

/**
 * Created by igor on 02.07.14.
 */
public class PredictionParser extends XMLCSVHandler
{
    public Jedis redis;
    protected final DateTimeFormatter fmt_in = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    protected final DateTimeZone timeZone;
    private TaskPrediction task;
    private final int group_code;
    private String prefix_redis;
    private int time_live;
    private final DateTimeFormatter fmt_out = ISODateTimeFormat.dateTimeNoMillis();
    private Pipeline pipe;

    public PredictionParser(Jedis redis, DateTimeZone timeZone, TaskPrediction task, String worker_name)
    {
        this.redis = redis;
        this.timeZone = timeZone;
        this.task = task;
        this.group_code = (new Group_codes()).get(Config.getString(worker_name, "url", "/podolsk").substring(1));
        this.prefix_redis = Config.getString(worker_name, "prefix_redis", "tn");
        this.time_live = Config.getInt(worker_name, "time-live", 60);
    }

    protected void start()
    {
        pipe = redis.pipelined();
        pipe.multi();
    }

    protected void add(int mr_id, int st_id, int direction, int uniqueid, DateTime arrivetime)
    {
        String key = String.format("prediction:%s:route:%d:%d:%d:%d:%d:%d", prefix_redis, group_code, mr_id, direction, group_code, st_id, uniqueid);
        String value = fmt_out.print(arrivetime);
        pipe.set(key, value);
        pipe.expire(key, time_live);
        task.redis_keys.add(key);
        task.old_redis_keys.remove(key);
        Prediction prediction = new Prediction();
        prediction.when = arrivetime;
        prediction.route = String.format(Locale.US, "%d:%d:%d", group_code, mr_id, direction);
        prediction.station = String.format(Locale.US, "%d:%d", group_code, st_id);
        prediction.vehicle = Integer.toString(uniqueid);
        task.for_publish.put(String.format(Locale.US, "%d:%d:%d", mr_id, direction, st_id), prediction);
    }

    protected void end()
    {
        pipe.exec();
    }
}
