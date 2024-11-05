package com.keisa1333.rosepvp.filemanager;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionDOM {
    private Map<String, Location> points;
    private Map<String, Map<String, Integer>> amountPerson;
    private Map<String, Double> progressDominate;

    public SessionDOM() {
        this.points = new HashMap<>();
        this.amountPerson = new HashMap<>();
        this.progressDominate = new HashMap<>();
    }
}

class SessionCQ {
    private Map<String, Location> points;
    private Map<String, Map<String, Integer>> amountPerson;
    private Map<String, Double> progressConquest;
    private Map<String, List<Location>> spawnsCP;
    private String cpRed;
    private String cpBlue;
}