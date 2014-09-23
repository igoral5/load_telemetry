package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by igor on 15.08.14.
 */
public class UnitsXMLParser extends UnitsParser
{
    public UnitsXMLParser(DateTimeZone timeZone, String worker_name)
    {
        super(timeZone, worker_name);
    }
    @Override
    public void startDocument() throws SAXException
    {
        clear();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase("row"))
        {
            int srv_id = Integer.valueOf(attributes.getValue("srv_id"));
            int uniqueid = Integer.valueOf(attributes.getValue("uniqueid"));
            DateTime u_timenav = fmt.parseDateTime(attributes.getValue("u_timenav")).withZoneRetainFields(timeZone);
            double lat = Double.valueOf(attributes.getValue("u_lat"));
            double lon = Double.valueOf(attributes.getValue("u_long"));
            int speed = Integer.valueOf(attributes.getValue("u_speed"));
            int course = Integer.valueOf(attributes.getValue("u_course"));
            int tt_id = Integer.valueOf(attributes.getValue("tt_id"));
            int inv = Integer.valueOf(attributes.getValue("u_inv"));
            add(srv_id, uniqueid, tt_id, u_timenav, lat, lon, speed, course, inv);
        }
    }
}
