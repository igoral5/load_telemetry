package com.shturmann.telemetry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created by igor on 27.06.14.
 */
public class CheckSumParser extends XMLCSVHandler
{
    private HashMap<String, Long> map = new HashMap<>();

    protected void clear()
    {
        map.clear();
    }

    protected void add(String tablename, long checksum)
    {
        map.put(tablename, checksum);
    }

    public long get(String nametable)
    {
        return this.map.get(nametable);
    }
}
