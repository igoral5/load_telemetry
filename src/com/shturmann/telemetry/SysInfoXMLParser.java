package com.shturmann.telemetry;

import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by igor on 15.08.14.
 */
public class SysInfoXMLParser extends SysInfoParser
{
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase("row"))
        {
            DateTime sys_time = fmt.parseDateTime(attributes.getValue("SysTime"));
            DateTime last_upadte = fmt.parseDateTime(attributes.getValue("LastUpdateTime"));
            DateTime nav_update;
            if (attributes.getValue("NavUpdateTime") != null)
            {
                nav_update = fmt.parseDateTime(attributes.getValue("NavUpdateTime"));
            }
            else
            {
                nav_update = new DateTime(0);
            }
            set(sys_time, last_upadte, nav_update);
        }
    }
}
