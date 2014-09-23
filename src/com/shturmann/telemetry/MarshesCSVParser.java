package com.shturmann.telemetry;

import java.util.HashMap;

/**
 * Created by igor on 15.08.14.
 */
public class MarshesCSVParser extends MarshesParser
{

    @Override
    public void start_doc()
    {
        clear();
    }

    @Override
    public void next(String[] line)
    {
        int mr_id = Integer.parseInt(line[0]);
        add(mr_id, line[3]);
    }
}
