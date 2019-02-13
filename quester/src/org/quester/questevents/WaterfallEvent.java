package org.quester.questevents;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.EquipmentSlot;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.containers.EquipmentLoadout;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.Arrays;
import java.util.HashMap;

public class WaterfallEvent extends BotEvent implements Logger {

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private final String[] QUEST_DIALOGUE = {
            "How can I help?", "Where else is worth visiting around here?"
    };

    private final Area START_AREA = new Area(2518, 3503, 2523, 3492);
    private final Tile QUEST_START_TILE = new Tile(2520, 3495, 0);
    private final Area ROPE_ON_ROCK_AREA = new Area(2509, 3481, 2514, 3475);
    private final Area INTERACT_TREE_BEFORE_ENTRANCE_AREA = new Area(2510, 3469, 2514, 3465);
    private final Area WATERFALL_DUNGEON_ENTRANCE = new Area(2508, 3462, 2514, 3463);
    private final Area HOUSE_HADLEY_AREA = new Area(
            new int[][]{
                    { 2516, 3435 },
                    { 2516, 3431 },
                    { 2511, 3431 },
                    { 2511, 3427 },
                    { 2516, 3427 },
                    { 2517, 3427 },
                    { 2517, 3424 },
                    { 2520, 3424 },
                    { 2520, 3423 },
                    { 2524, 3423 },
                    { 2524, 3424 },
                    { 2524, 3427 },
                    { 2521, 3427 },
                    { 2521, 3435 }
            }
    );
    private boolean grabbedBook;

    public WaterfallEvent(QuantumBot bot, HelperMethods helperMethods) {
        super(bot);
        this.helper = helperMethods;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Mind rune", 500);
        itemReq.put("Air rune", 1000);
        itemReq.put("Cheese", 3);
        itemReq.put("Trout", 10);
        itemReq.put("Staff of fire", 1);
        itemReq.put("Leather gloves", 1);
        itemReq.put("Amulet of magic", 1);
        info("Started: " + Quest.WATERFALL_QUEST.name());
        helper.setGrabbedItems(true);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getClient().getVarp(65);

        if (!helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                helper.setGrabbedItems(helper.getBankEvent(itemReq).addReq(
                        new EquipmentLoadout()
                                .set(EquipmentSlot.WEAPON, "Staff of fire")
                                .set(EquipmentSlot.NECK, "Amulet of magic")
                                .set(EquipmentSlot.HANDS, "Leather gloves")
                ).executed());
            } else {
                // Load buy event and execute buy orders
                if (helper.getBuyableEvent(itemReq) == null) {
                    info("Failed: Not enough coins. Setting complete and stopping.");
                    setComplete();
                    getBot().stop();
                    return;
                }
                info("GE event execute");
                helper.getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 65 = " + result);
        if (getBot().getDialogues().inDialogue()) {
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption()).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                info("Handling option");
                info("QUEST_DIALOGUE");
                new DialogueEvent(getBot(), QUEST_DIALOGUE).execute();
                sleep(1000);
            } else {
                info("No dialogue???");
            }
        } else {
            switch (result) {
                case 0:
                    // Start
                    if (helper.inArea(START_AREA)) {
                        info("In start area");
                        if (helper.talkTo("Almera"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(QUEST_START_TILE).execute();
                    }
                    break;
                case 1:
                    // Talk to boy
                    GameObject closedGate = getBot().getGameObjects().closest(o -> o != null && o.hasName("Gate")
                            && o.hasAction("Open") && o.getTile().getX() == 2513 && o.getTile().getTile().getY() == 3494);
                    if (closedGate != null){
                        info("Need to open gate near raft");
                        if (new InteractEvent(getBot(), closedGate, "Open").executed()){
                            sleepUntil(3000, () -> !closedGate.exists());
                        }
                    } else if (helper.interactObject("Log raft", "Board")){
                        info("Boarding raft to lost boy");
                        sleepUntil(13000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 2:
                    // Interact barrel
                    if (helper.inArea(ROPE_ON_ROCK_AREA)){
                        if (getBot().getInventory().isSelected(i -> i.hasName("Rope"))){
                            if (helper.interactObject("Rock", "Use")){
                                info("Going across the water: Use rope on rock");
                                sleepUntil(10000, () -> helper.inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA));
                            }
                        } else if (helper.interactInventory("Rope", "Use")){
                            info("Interacting rope: Use");
                            sleepUntil(4000, () -> getBot().getInventory().isSelected(i -> i.hasName("Rope")));
                        }
                    } else if (helper.inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA)){
                        if (getBot().getInventory().isSelected(i -> i.hasName("Rope"))){
                            if (helper.interactObject("Dead tree", "Use")){
                                info("Going across the water: Use rope on dead tree");
                                sleepUntil(10000, () -> helper.inArea(WATERFALL_DUNGEON_ENTRANCE));
                            }
                        } else if (helper.interactInventory("Rope", "Use")){
                            info("Interacting rope: Use");
                            sleepUntil(4000, () -> getBot().getInventory().isSelected(i -> i.hasName("Rope")));
                        }
                    } else if (helper.inArea(WATERFALL_DUNGEON_ENTRANCE)){
                        if (helper.interactObject("Barrel", "Get in")){
                            info("Interact barrel: get in");
                            sleepUntil(15000, () -> helper.myPosition().getY() == 3413);
                        }
                    } else if (helper.inArea(HOUSE_HADLEY_AREA)){
                        if (helper.talkTo("Hadley")){
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        helper.getWeb(HOUSE_HADLEY_AREA).execute();
                    }
                    break;
                case 7:
                    // End
                    info("Finished: " + Quest.WATERFALL_QUEST.name());
                    setComplete();
                    break;
            }
        }

        // Help delay for walking : standard 800 ms
        sleep(1000);
    }

    @Override
    public void onFinish() {
        helper.setGrabbedItems(false);
    }
}
