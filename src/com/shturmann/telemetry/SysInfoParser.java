package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by igor on 27.06.14.
 */
public class SysInfoParser extends XMLCSVHandler
{
    public static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyy-MM-dd HH:mm:ss");
    private DateTime last_update = new DateTime(0);
    private DateTime sys_time = new DateTime(0);
    private DateTime nav_update = new DateTime(0);

    protected void set(DateTime sys_time, DateTime last_update, DateTime nav_update)
    {
        this.sys_time = sys_time;
        this.last_update = last_update;
        this.nav_update = nav_update;
    }

    public DateTime get_last_update()
    {
        return last_update;
    }

    public DateTime get_sys_time()
    {
        return sys_time;
    }

    public DateTime get_nav_update() { return  nav_update; }
}
