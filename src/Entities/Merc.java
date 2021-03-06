package Entities;

import World.Location;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class Merc extends Human {

    {
        setHealth(150);
    }
    public Merc(String name) {
        super(name);
        getLocation().setXY(90,90);
    }

    public void show() {}

    public Merc(String name, Location location) {
        super(name, location);
    }

    public Merc(String name, Location location, LocalDateTime date) {
        super(name, location, date);
    }

    public void shoot() {
        super.shoot();
    }

}
