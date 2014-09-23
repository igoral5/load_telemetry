package com.shturmann.telemetry;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.ArrayList;
import java.util.Timer;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        try
        {
            com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();
            HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
            ArrayList<Timer> arrayTimer = new ArrayList<>();
            boolean present_worker = false;
            for (String worker_name : Config.getString("workers", "").split(","))
            {
                if (! worker_name.isEmpty())
                {
                    if (Config.getString(worker_name, "type", "").equalsIgnoreCase("telemetry"))
                    {
                        Timer timer = new Timer();
                        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), Config.getString(worker_name, "host-redis", "localhost"), Config.getInt(worker_name, "port-redis", 6379), Protocol.DEFAULT_TIMEOUT, null, Config.getInt(worker_name, "db-redis", 0));
                        timer.schedule(new TaskTelemetry(worker_name, jedisPool, hz), 0, Config.getInt(worker_name, "time-cycle", 30) * 1000);
                        //System.out.write(String.format("Created worker \"%s\" type: telemetry", worker_name).getBytes());
                        logger.info("Created worker \"{}\" type: telemetry", worker_name);
                        arrayTimer.add(timer);
                        present_worker = true;
                    } else
                    {
                        if (Config.getString(worker_name, "type", "").equalsIgnoreCase("prediction"))
                        {
                            Timer timer = new Timer();
                            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), Config.getString(worker_name, "host-redis", "localhost"), Config.getInt(worker_name, "port-redis", 6379), Protocol.DEFAULT_TIMEOUT, null, Config.getInt(worker_name, "db-redis", 0));
                            timer.schedule(new TaskPrediction(worker_name, jedisPool, hz), 0, Config.getInt(worker_name, "time-cycle", 30) * 1000);
                            //System.out.write(String.format("Created worker \"%s\" type: prediction", worker_name).getBytes());
                            logger.info("Created worker \"{}\" type: prediction", worker_name);
                            arrayTimer.add(timer);
                            present_worker = true;
                        } else
                        {
                            //System.out.write(String.format("worker \"%s\" type \"%s\" not known", worker_name, Config.getString(worker_name, "type", "")).getBytes());
                            logger.error("Check configuration, worker \"{}\" type \"{}\" not known", worker_name, Config.getString(worker_name, "type", ""));
                        }
                    }
                }
            }
            if (!present_worker)
            {
                //System.out.write("Not found workers\n".getBytes());
                logger.error("Check configuration, not found workers");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            logger.error("Error initialization", e);
            System.exit(-1);
        }
    }
}
