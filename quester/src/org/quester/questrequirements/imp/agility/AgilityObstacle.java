package org.quester.questrequirements.imp.agility;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;

public class AgilityObstacle {

    private QuantumBot ctx;
    private String name, action;
    private Area area;

    public AgilityObstacle(QuantumBot ctx, String name, String action, Area area) {
        this.ctx = ctx;
        this.name = name;
        this.action = action;
        this.area = area;
    }

    public AgilityObstacle(QuantumBot ctx, String name, String action, Tile position, Area area) {
        this.ctx = ctx;
        this.name = name;
        this.action = action;
        this.area = area;
    }

    public String getName(){
        return this.name;
    }

    public String getAction() {
        return this.action;
    }

    public Area getArea() {
        return this.area;
    }

    public GameObject asTarget() {
        return this.ctx.getGameObjects().closest(rs2Object -> rs2Object != null && rs2Object.getName().equals(this.name) && rs2Object.hasAction(this.action) && area.contains(rs2Object));
    }

}