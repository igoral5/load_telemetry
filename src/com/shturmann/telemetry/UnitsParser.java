package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;


/**
 * Created by igor on 27.06.14.
 */
public class UnitsParser extends XMLCSVHandler
{
    protected static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyy-MM-dd HH:mm:ss");
    protected final DateTimeZone timeZone;
    protected ArrayList<Units> units = new ArrayList<>();
    protected final Transport_types transport_types = new Transport_types();
    protected final boolean correct_transport_type;


    public UnitsParser(DateTimeZone timeZone, String worker_name)
    {
        this.timeZone = timeZone;
        this.correct_transport_type = Config.getBoolean(worker_name, "correct-transport-type", false);
    }

    protected void clear()
    {
        units.clear();
    }

    protected void add(int srv_id, int uniqueid, int tt_id, DateTime timenav, double lat, double lon, int speed, int course, int inv, String statenum)
    {
        if (correct_transport_type && (tt_id == 2 || tt_id == 3))
            tt_id = 1;
        Integer transport_type = transport_types.get(tt_id);
        if (transport_type == null)
            return;
        statenum = statenum.replaceAll("\\s+", "").toUpperCase();
        units.add(new Units(srv_id, uniqueid, transport_type, timenav, lat, lon, speed, course, inv, statenum));
    }

    public ArrayList<Units> get()
    {
        return units;
    }
}
