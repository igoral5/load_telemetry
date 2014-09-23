package com.shturmann.telemetry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by igor on 15.08.14.
 */
public class CheckSumXMLParser extends CheckSumParser
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
            long checksum = Long.parseLong(attributes.getValue("cs_checksum"));
            add(attributes.getValue("cs_tablename"), checksum);
        }
    }
}
