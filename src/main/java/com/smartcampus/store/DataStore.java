package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.*;

public class DataStore {

    public static final Map<String, Room> rooms = new HashMap<>();
    public static final Map<String, Sensor> sensors = new HashMap<>();
    public static final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    static {
        Room library = new Room("LIB-301", "Library Quiet Study", 50);
        Room lab = new Room("LAB-101", "Computer Lab", 30);

        rooms.put(library.getId(), library);
        rooms.put(lab.getId(), lab);

        Sensor tempSensor = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor co2Sensor = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");

        sensors.put(tempSensor.getId(), tempSensor);
        sensors.put(co2Sensor.getId(), co2Sensor);

        library.getSensorIds().add("TEMP-001");
        lab.getSensorIds().add("CO2-001");

        sensorReadings.put("TEMP-001", new ArrayList<>());
        sensorReadings.put("CO2-001", new ArrayList<>());
    }
}