package com.shturmann.telemetry;

import javafx.util.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created by igor on 27.06.14.
 */
public class NariadParser extends XMLCSVHandler
{
    private HashMap<Pair<Integer, Integer>, Nariad> map = new HashMap<>();

    protected void clear()
    {
        map.clear();
    }

    protected void add(int srv_id, int uniqueid, int mr_id, int direction)
    {
        map.put(new Pair<Integer, Integer>(srv_id, uniqueid), new Nariad(mr_id, direction));
    }

    public Nariad get(int srv_id, int uniqueid)
    {
        return map.get(new Pair<Integer, Integer>(srv_id,uniqueid));
    }
}
