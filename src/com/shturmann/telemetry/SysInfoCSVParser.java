package com.shturmann.telemetry;

import org.joda.time.DateTime;
import sun.security.util.Length;

import java.util.Date;

/**
 * Created by igor on 15.08.14.
 */
public class SysInfoCSVParser extends  SysInfoParser
{
    @Override
    public void start_doc()
    {
    }

    @Override
    public void next(String[] line)
    {
        DateTime sys_date = fmt.parseDateTime(line[0]);
        DateTime last_update = fmt.parseDateTime(line[1]);
        DateTime nav_update;
        if (line.length >= 3)
        {
            nav_update = fmt.parseDateTime(line[2]);
        }
        else
        {
            nav_update = new DateTime(0);
        }
        set(sys_date, last_update, nav_update);
    }
}
