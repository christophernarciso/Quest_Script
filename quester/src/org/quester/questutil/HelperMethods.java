package org.quester.questutil;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.containers.Item;
import org.quantumbot.api.entities.*;
import org.quantumbot.api.interfaces.Interactable;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.interactions.GroundItemInteractEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.events.interactions.NPCInteractEvent;
import org.quantumbot.events.interactions.ObjectInteractEvent;

import java.util.function.Predicate;

public class HelperMethods {

    private QuantumBot context;

    public HelperMethods(QuantumBot context) {
        this.context = context;
    }

    public QuantumBot getContext() {
        return this.context;
    }

    public Player myPlayer() {
        return context.getPlayers().getLocal();
    }

    public Tile myPosition() {
        return context.getPlayers().getLocal().getTile();
    }

    public WebWalkEvent getWeb(Locatable... locations) {
        return new WebWalkEvent(context, locations);
    }

    public WebWalkEvent getWeb(Area area) {
        return new WebWalkEvent(context, area);
    }

    public WebWalkEvent getWeb(Tile tile) {
        return new WebWalkEvent(context, tile);
    }

    public InteractEvent getInteractEvent(Interactable interactable, String... actions) {
        return new InteractEvent(context, interactable, actions);
    }

    public DialogueEvent getDialogue(String... options) {
        return options == null ? new DialogueEvent(context) : new DialogueEvent(context, options);
    }

    public boolean talkTo(String npcName) throws InterruptedException {
        return new NPCInteractEvent(context, npcName, "Talk-to").executed();
    }

    public boolean interactNPC(String npcName, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcName, actions).executed();
    }

    public boolean interactNPC(Predicate<NPC> npcPredicate, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcPredicate, actions).executed();
    }

    public boolean interactObject(String objectName, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectName, actions).executed();
    }

    public boolean interactObject(Predicate<GameObject> objectPredicate, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectPredicate, actions).executed();
    }

    public boolean interactGroundItem(String groundItemName, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemName, actions).executed();
    }

    public boolean interactGroundItem(Predicate<GroundItem> groundItemPredicate, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemPredicate, actions).executed();
    }

    public boolean interactInventory(String itemName, String... actions) throws InterruptedException {
        return new InventoryInteractEvent(context, itemName, actions).executed();
    }

    public boolean interactInventory(Predicate<Item> itemPredicate, String... actions) throws InterruptedException {
        return new InventoryInteractEvent(context, itemPredicate, actions).executed();
    }

    public boolean inArea(Area area) {
        return area.contains(myPlayer());
    }

    public boolean atExactPosition(Tile tile) {
        return myPlayer().getTile().equals(tile);
    }

}
