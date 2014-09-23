package com.shturmann.telemetry;

import java.util.HashMap;

/**
 * Created by igor on 27.06.14.
 */
public class Group_codes
{
    private HashMap<String, Integer> map = new HashMap<>();
    public Group_codes()
    {
        map.put("podolsk", 46246000);
        map.put("sochi", 3000000);
        map.put("krasnoyarsk", 4000000);
        map.put("moscow", 45000000);
    }
    public int get(String url)
    {
        return map.get(url);
    }
}
