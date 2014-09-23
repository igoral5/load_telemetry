package com.shturmann.telemetry;

import org.joda.time.DateTime;

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
        set(sys_date, last_update);
    }
}
