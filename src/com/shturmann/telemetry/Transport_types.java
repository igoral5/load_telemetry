package com.shturmann.telemetry;

import java.util.HashMap;

/**
 * Created by igor on 27.06.14.
 */
public class Transport_types
{
    private HashMap<Integer, Integer> map = new HashMap<>();
    public Transport_types()
    {
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 4);
        map.put(4 ,16);
    }
    public Integer get(int tt_id)
    {
        return map.get(tt_id);
    }
}
