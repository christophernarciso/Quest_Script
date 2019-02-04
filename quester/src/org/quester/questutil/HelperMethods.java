package org.quester.questutil;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.Locatable;
import org.quantumbot.api.entities.Player;
import org.quantumbot.api.map.Tile;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.interfaces.Logger;

public class HelperMethods implements Logger {

    private QuantumBot context;

    public HelperMethods(QuantumBot context) {
        this.context = context;
    }

    public Player myPlayer(){
        return context.getPlayers().getLocal();
    }

    public Tile myPosition(){
        return context.getPlayers().getLocal().getTile();
    }

    public WebWalkEvent getWeb(Locatable... locations){
        return new WebWalkEvent(context, locations);
    }

}
