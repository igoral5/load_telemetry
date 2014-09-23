package com.shturmann.telemetry;

import java.util.HashMap;

/**
 * Created by igor on 15.08.14.
 */
public class CheckSumCSVParser extends CheckSumParser
{
    @Override
    public void start_doc()
    {
        clear();
    }

    @Override
    public void next(String[] line)
    {
        long checksum = Long.parseLong(line[1]);
        add(line[0], checksum);
    }
}
