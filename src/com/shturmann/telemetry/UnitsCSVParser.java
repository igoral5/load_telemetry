package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * Created by igor on 15.08.14.
 */
public class UnitsCSVParser extends UnitsParser
{

    public UnitsCSVParser(DateTimeZone timeZone, String worker_name)
    {
        super(timeZone, worker_name);
    }

    @Override
    public void start_doc()
    {
        clear();
    }
    @Override
    public void next(String[] line)
    {
        int srv_id = Integer.parseInt(line[0]);
        int uniqueid = Integer.parseInt(line[1]);
        int tt_id = Integer.parseInt(line[2]);
        DateTime u_timenav = fmt.parseDateTime(line[7]).withZoneRetainFields(timeZone);
        double u_lat = Double.parseDouble(line[8]);
        double u_long = Double.parseDouble(line[9]);
        int u_speed = Integer.parseInt(line[10]);
        int u_course = Integer.parseInt(line[11]);
        int u_inv = Integer.parseInt(line[12]);
        add(srv_id, uniqueid, tt_id, u_timenav, u_lat, u_long, u_speed, u_course, u_inv);
    }
}
