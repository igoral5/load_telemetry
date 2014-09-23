package com.shturmann.telemetry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created by igor on 27.06.14.
 */
public class MarshesParser extends XMLCSVHandler
{
    private HashMap<Integer, String> map = new HashMap<>();

    protected void clear()
    {
        map.clear();
    }
    protected void add(int mr_id, String mr_num)
    {
        map.put(mr_id, mr_num);
    }
    public String get(int mr_id)
    {
        return map.get(mr_id);
    }
}
