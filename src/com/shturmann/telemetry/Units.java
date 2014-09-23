package com.shturmann.telemetry;

import org.joda.time.DateTime;

/**
 * Created by igor on 01.07.14.
 */
public class Units
{
    public int srv_id;
    public int uniqueid;
    public int transport_type;
    public DateTime timenav;
    public double lat;
    public double lon;
    public int speed;
    public int course;
    public int inv;
    public Units(int srv_id, int uniqueid, int transport_type, DateTime timenav, double lat, double lon, int speed, int course, int inv)
    {
        this.srv_id = srv_id;
        this.uniqueid = uniqueid;
        this.transport_type = transport_type;
        this.timenav = timenav;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.course = course;
        this.inv = inv;
    }

}
