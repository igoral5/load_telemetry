package com.shturmann.telemetry;

import com.netflix.config.DynamicPropertyFactory;

import java.util.Locale;

/**
 * Created by igor on 04.07.14.
 */

public class Config
{
    public static int getInt(String worker_name, String name_property, int default_value)
    {
        return DynamicPropertyFactory.getInstance().getIntProperty(String.format(Locale.US, "%s.%s", worker_name, name_property), default_value).get();
    }

    public static int getInt(String name_property, int default_value)
    {
        return DynamicPropertyFactory.getInstance().getIntProperty(name_property, default_value).get();
    }

    public static String getString(String worker_name, String name_property, String default_value)
    {
        return DynamicPropertyFactory.getInstance().getStringProperty(String.format(Locale.US, "%s.%s", worker_name, name_property), default_value).get();
    }

    public static String getString(String name_property, String default_value)
    {
        return DynamicPropertyFactory.getInstance().getStringProperty(name_property, default_value).get();
    }

    public static double getDouble(String worker_name, String name_property, double default_value)
    {
        return DynamicPropertyFactory.getInstance().getDoubleProperty(String.format(Locale.US, "%s.%s", worker_name, name_property), default_value).get();
    }

    public static double getDouble(String name_property, double default_value)
    {
        return DynamicPropertyFactory.getInstance().getDoubleProperty(name_property, default_value).get();
    }

    public static boolean getBoolean(String worker_name, String name_property, boolean default_value)
    {
        return DynamicPropertyFactory.getInstance().getBooleanProperty(String.format(Locale.US, "%s.%s", worker_name, name_property), default_value).get();
    }

    public static boolean getBoolean(String name_property, boolean default_value)
    {
        return DynamicPropertyFactory.getInstance().getBooleanProperty(name_property, default_value).get();
    }
}


