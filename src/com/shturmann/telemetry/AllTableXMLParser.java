package com.shturmann.telemetry;

import org.joda.time.DateTimeZone;
import redis.clients.jedis.Jedis;

/**
 * Created by igor on 30.06.14.
 */
public class AllTableXMLParser extends PredictionXMLParser
{
    public AllTableXMLParser(Jedis redis, DateTimeZone timeZone, TaskPrediction task, String worker_name)
    {
        super(redis, timeZone, task, worker_name);
    }

    protected String get_name_field()
    {
        return "ta_arrivetime";
    }
}
