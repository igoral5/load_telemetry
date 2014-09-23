package com.shturmann.telemetry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.joda.time.DateTimeZone;
import org.slf4j.MarkerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by igor on 30.06.14.
 */
public class TaskPrediction extends TaskGeneral
{
    public Set<String> old_redis_keys = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    public Set<String> redis_keys = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    public ConcurrentHashMap<String, Prediction> for_publish = new ConcurrentHashMap<>();
    private ITopic<ArrayList<Prediction>> topic;

    public TaskPrediction(String worker_name, JedisPool jedisPool, HazelcastInstance hz)
    {
        super(worker_name, jedisPool, hz);
    }


    public void process_update() throws MalformedURLException, ExecutionException, InterruptedException
    {
        long t0 = System.currentTimeMillis();
        for_publish.clear();
        if (Config.getBoolean(worker_name, "gettablecur2", false))
        {
            PredictionParser curtable = null;
            try
            {
                Future<PredictionParser> future_curtable;
                if (format == Format.XML)
                {
                    curtable = new CurTableXMLParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    URL url = new URL(part_url + "/getTableCur2.php");
                    future_curtable = threadPool.submit(new URLParserXMLTask<>(curtable, url, this));
                } else
                {
                    curtable = new PredictionCSVParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    URL url = new URL(part_url + "/getTableCur2.php?fmt=csv");
                    future_curtable = threadPool.submit(new URLParserCSVTask<>(curtable, url, this));
                }
                log("create future_curtable", 3);
                curtable = future_curtable.get();
            }
            finally
            {
                if (curtable != null)
                {
                    jedisPool.returnResource(curtable.redis);
                    log("curtable.redis return in pool", 3);
                }
            }
            log("future_curtable complet", 3);
            log(String.format("request getTableCur2 completed %d ms", System.currentTimeMillis() - t0), 2);
        }
        else
        {
            PredictionParser alltable = null;
            PredictionParser curtable = null;
            try
            {
                Future<PredictionParser> future_alltable;
                Future<PredictionParser> future_curtable;
                if (format == Format.XML)
                {
                    URL url = new URL(this.part_url + "/getTableAll.php");
                    alltable = new AllTableXMLParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    future_alltable = threadPool.submit(new URLParserXMLTask<>(alltable, url, this));
                    url = new URL(this.part_url + "/getTableCur.php");
                    curtable = new CurTableXMLParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    future_curtable = threadPool.submit(new URLParserXMLTask<>(curtable, url, this));
                } else
                {
                    URL url = new URL(this.part_url + "/getTableAll.php?csv=fmt");
                    alltable = new PredictionCSVParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    future_alltable = threadPool.submit(new URLParserCSVTask<>(alltable, url, this));
                    url = new URL(this.part_url + "/getTableCur.php?fmt=csv");
                    curtable = new PredictionCSVParser(jedisPool.getResource(), DateTimeZone.forOffsetHoursMinutes(time_zone / 60, time_zone % 60), this, worker_name);
                    future_curtable = threadPool.submit(new URLParserCSVTask<>(curtable, url, this));
                }
                curtable = future_curtable.get();
                alltable = future_alltable.get();
            }
            finally
            {
                if (alltable != null)
                {
                    jedisPool.returnResource(alltable.redis);
                    log("alltable.redis return in pool", 3);
                }
                if (curtable != null)
                {
                    jedisPool.returnResource(curtable.redis);
                    log("curtable.redis return in pool", 3);
                }
            }
            log(String.format("request getTableAll, getTableCur completed %d ms", System.currentTimeMillis() - t0), 2);
        }
        t0 = System.currentTimeMillis();
        String name_topic = String.format(Locale.US, "predictions.%s.%d", Config.getString(worker_name, "prefix_redis", "tn"), group_code);
        topic = hz.getTopic(name_topic);
        ArrayList<Prediction> post = new ArrayList<>();
        for (Prediction prediction : for_publish.values())
        {
            post.add(prediction);
        }
        topic.publish(post);
        log(String.format("publish in %s %d predictions %d ms", name_topic, post.size(), System.currentTimeMillis() - t0), 2);
        t0 = System.currentTimeMillis();
        Jedis redis = jedisPool.getResource();
        Pipeline pipe = redis.pipelined();
        pipe.multi();
        for (String key : this.old_redis_keys)
        {
            pipe.del(key);
        }
        log(String.format("delete old keys in redis %d ms", System.currentTimeMillis() - t0), 2);
        t0 = System.currentTimeMillis();
        pipe.exec();
        jedisPool.returnResource(redis);
        log(String.format("transaction redis completed %d ms", System.currentTimeMillis() - t0), 2);
        this.old_redis_keys = this.redis_keys;
        this.redis_keys = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        if (post.isEmpty())
        {
            //log(String.format(Locale.US, "nagios Отсутствуют прогнозы, обработано за %d мс", System.currentTimeMillis() - begin_update), 0);
            logger.warn(MarkerFactory.getMarker("nagios"), String.format(Locale.US, "%s Отсутствуют прогнозы, обработано за %d мс", worker_name, System.currentTimeMillis()- begin_update));
        }
        else
        {
            //log(String.format(Locale.US, "nagios Получено %d прогнозов за %d мс", post.size(), System.currentTimeMillis() - begin_update), 1);
            logger.info(MarkerFactory.getMarker("nagios"), String.format(Locale.US, "%s Получено %d прогнозов за %d мс", worker_name, post.size(), System.currentTimeMillis() - begin_update));
        }
    }
}
