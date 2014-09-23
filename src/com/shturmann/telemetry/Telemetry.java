package com.shturmann.telemetry;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by igor on 03.07.14.
 */
public class Telemetry implements Serializable
{
    static final long serialVersionUID = 1L;
    /** ID маршрута, по которому едет ТС */
    public String route;
    /** ID транспортного средства */
    public String vehicle;
    /** Время формирования телеметрических данных. Должно заполняться поставщиком данных */
    public DateTime time;
    /** Широта */
    public double lat;
    /** Долгота */
    public double lon;
    /** Скорость */
    public int speed;
    /** Азимут */
    public int azimuth;
    /** Тип транспорта */
    public int transport;
    /** Признак пригодности для инвалидов */
    public boolean handicapped;
    /** Имя маршрута */
    public String name;

    public DateTime t1;
    public DateTime t2;

    public Telemetry() {
    }

    public Telemetry(Telemetry t) {
        this.route = t.route;
        this.vehicle = t.vehicle;
        this.time = t.time;
        this.lat = t.lat;
        this.lon = t.lon;
        this.speed = t.speed;
        this.azimuth = t.azimuth;
        this.transport = t.transport;
        this.handicapped = t.handicapped;
        this.name = t.name;
        this.t1 = t.t1;
        this.t2 = t.t2;
    }
}
