package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import redis.clients.jedis.Jedis;

/**
 * Created by igor on 15.08.14.
 */
public abstract class PredictionXMLParser extends PredictionParser
{
    protected String name_field;

    public PredictionXMLParser(Jedis redis, DateTimeZone timeZone, TaskPrediction task, String worker_name)
    {
        super(redis, timeZone, task, worker_name);
        name_field = get_name_field();
    }

    @Override
    public void startDocument() throws SAXException
    {
        start();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase("row"))
        {
            if (!attributes.getValue(name_field).isEmpty())
            {
                int mr_id = Integer.parseInt(attributes.getValue("mr_id"));
                int st_id = Integer.parseInt(attributes.getValue("st_id"));
                int direction = attributes.getValue("rl_racetype").charAt(0) - 'A';
                int uniqueid = Integer.parseInt(attributes.getValue("uniqueid"));
                DateTime arrivetime = fmt_in.parseDateTime(attributes.getValue(name_field)).withZoneRetainFields(this.timeZone);
                add(mr_id, st_id, direction, uniqueid, arrivetime);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        end();
    }
    protected abstract String get_name_field();

}
