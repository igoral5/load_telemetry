package com.shturmann.telemetry;

import javafx.util.Pair;

import java.util.HashMap;

/**
 * Created by igor on 15.08.14.
 */
public class NariadCSVParser extends NariadParser
{
    @Override
    public void start_doc()
    {
        clear();
    }

    @Override
    public void next(String[] line)
    {
        int srv_id = Integer.parseInt(line[0]);
        int uniqueid = Integer.parseInt(line[1]);
        int mr_id = Integer.parseInt(line[2]);
        int direction = line[5].charAt(0) - 'A';
        add(srv_id, uniqueid, mr_id, direction);
    }
}
