package com.shturmann.telemetry;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by igor on 08.07.14.
 */
public class Prediction implements Serializable
{
   static final long serialVersionUID = 1L;
    /** ID транспортного средства */
   public String vehicle;
   /** ID маршрута, по которому едет ТС */
   public String route;
   /** ID остановки, к которой относится прогноз */
   public String station;
   /** Время прибытия на остановку */
   public DateTime when;
}
