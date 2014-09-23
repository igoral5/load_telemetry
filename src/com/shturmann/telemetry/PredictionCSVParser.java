package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import redis.clients.jedis.Jedis;


/**
 * Created by igor on 15.08.14.
 */
public class PredictionCSVParser extends PredictionParser
{


    public PredictionCSVParser(Jedis redis, DateTimeZone timeZone, TaskPrediction task, String worker_name)
    {
        super(redis, timeZone, task, worker_name);
    }

    @Override
    public void start_doc()
    {
        start();
    }

    @Override
    public void next(String[] line)
    {
        if (line[7].isEmpty())
            return;
        int mr_id = Integer.parseInt(line[3]);
        int st_id = Integer.parseInt(line[2]);
        int direction = line[4].charAt(0) - 'A';
        int uniqueid = Integer.parseInt(line[5]);
        DateTime arrivetime = fmt_in.parseDateTime(line[7]).withZoneRetainFields(this.timeZone);
        add(mr_id, st_id, direction, uniqueid, arrivetime);
    }

    @Override
    public void end_doc()
    {
        end();
    }
}
