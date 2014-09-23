package com.shturmann.telemetry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by igor on 15.08.14.
 */
public class NariadXMLParser extends NariadParser
{
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
            int srv_id = Integer.parseInt(attributes.getValue("srv_id"));
            int uniqueid = Integer.parseInt(attributes.getValue("uniqueid"));
            int mr_id = Integer.parseInt(attributes.getValue("mr_id"));
            int direction = attributes.getValue("rl_racetype").charAt(0) - 'A';
            add(srv_id, uniqueid, mr_id, direction);
        }
    }
}
