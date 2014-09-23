package com.shturmann.telemetry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by igor on 15.08.14.
 */
public class MarshesXMLParser extends MarshesParser
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
            int mr_id = Integer.parseInt(attributes.getValue("mr_id"));
            add(mr_id, attributes.getValue("mr_num"));
        }
    }
}
