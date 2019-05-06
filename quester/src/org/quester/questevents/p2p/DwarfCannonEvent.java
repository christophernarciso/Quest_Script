package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;
import java.util.function.Predicate;

public class DwarfCannonEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Sure, I'd be honoured to join.", "Okay, I'll see what I can do.",
            "Okay then, just for you!"
    };
    private final Tile[] BROKEN_RAILING_LOCATIONS = {
            new Tile(2577, 3457, 0), new Tile(2573, 3457, 0),
            new Tile(2563, 3457, 0), new Tile(2559, 3458, 0),
            new Tile(2557, 3468, 0), new Tile(2555, 3479, 0)
    };
    private final Area START_AREA = new Area(2564, 3462, 2571, 3457);
    private final Area REMAINS_AREA = new Area(2567, 3444, 2571, 3442, 2);
    private final Area DUNGEON_ENTRANCE_AREA = new Area(2620, 3394, 2627, 3389);
    private final Area LOST_BOY_AREA = new Area(2560, 9856, 2573, 9839);
    private final Area NOTES_AREA = new Area(3008, 3454, 3014, 3452);

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public DwarfCannonEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Games necklace(1~8)", 1);
        itemReq.put("Falador teleport", 2);
        itemReq.put("Camelot teleport", 2);
        info("Started: " + Quest.DWARF_CANNON.name());
        helper.setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(0);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw;
                helper.setGrabbedItems(helper.getBankEvent(itemReq).executed());
            } else {
                // Load buy event and execute buy orders
                if (helper.getBuyableEvent(itemReq) == null) {
                    info("Failed: Not enough coins. Setting complete and stopping.");
                    setComplete();
                    getBot().stop();
                    return;
                }
                //info("GE event execute");
                helper.getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 0 = " + result);
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
                case 10:
                    // Return to Captain Lawgof
                case 8:
                    // Return to Captain Lawgof
                case 6:
                    // Return to Captain Lawgof
                case 3:
                    // Grab remains
                    if (helper.inArea(REMAINS_AREA) && !getBot().getInventory().contains("Dwarf remains")) {
                        info("Grabbing dwarf remains");
                        if (helper.interactObject("Dwarf remains", "Take"))
                            sleepUntil(3000, () -> getBot().getInventory().contains("Dwarf remains"));
                        break;
                    }
                case 1:
                    // Repair fences
                    if (getBot().getInventory().contains("Railing")) {
                        int index = 0;
                        while (index < BROKEN_RAILING_LOCATIONS.length) {
                            int finalIndex = index, last = helper.getQuantity(getBot().getInventory(), "Railing");
                            Predicate<GameObject> gameObjectPredicate = o -> o != null && o.getTile().equals(BROKEN_RAILING_LOCATIONS[finalIndex]);

                            if (helper.interactObject(gameObjectPredicate, "Inspect")) {
                                sleepUntil(2000, () -> getBot().getDialogues().inDialogue());

                                if (getBot().getDialogues().isPendingContinuation())
                                    new DialogueEvent(getBot()).execute();

                                if (sleepUntil(3500, () -> helper.getQuantity(getBot().getInventory(), "Railing") < last)) {
                                    info("Fixed railing at " + BROKEN_RAILING_LOCATIONS[index].toString());
                                    index++;
                                }
                            }
                        }
                        sleepGameCycle();
                    }
                case 0:
                    // Start
                    if (helper.inArea(START_AREA)) {
                        if (helper.talkTo("Captain Lawgof"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 2:
                    // Go to remains
//                    if (helper.inArea(REMAINS_AREA)){
//                        info("Grabbing dwarf remains");
//                        if (helper.interactGroundItem("Dwarf remains", "Take"))
//                            sleepUntil(3000, () -> getBot().getInventory().contains("Dwarf remains"));
//                    } else {
//
//                    }
                    info("Walking to dwarf remains");
                    helper.getWeb(REMAINS_AREA).execute();
                    break;
                case 4:
                    // Go find lost boy
                    if (helper.myPosition().getY() < 9000) {
                        if (helper.inArea(DUNGEON_ENTRANCE_AREA)) {
                            if (helper.interactObject("Cave Entrance", "Enter"))
                                sleepUntil(3000, () -> !helper.inArea(DUNGEON_ENTRANCE_AREA));
                        } else {
                            info("Walking to dungeon entrance");
                            helper.getWeb(DUNGEON_ENTRANCE_AREA).execute();
                        }
                    } else {
                        info("Walking to lost boy");
                        helper.getWeb(new Tile(2571, 9851, 0)).execute();
                        sleepGameCycle();
                    }
                    break;
                case 5:
                    // Open Crate for lost boy
                    if (helper.interactObject("Crate", "Search")) {
                        sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 7:
                    Widget fix = getBot().getWidgets().first(widget -> widget != null && widget.getText() != null
                            && widget.getText().equals("Choose a tool to use on each of the moving parts."));
                    if (fix != null && fix.isVisible()) {
                        Widget gear = getBot().getWidgets().first(widget -> widget != null && widget.getTooltip() != null
                                && widget.getTooltip().equals("Gear"));
                        Widget safety = getBot().getWidgets().first(widget -> widget != null && widget.getTooltip() != null
                                && widget.getTooltip().equals("Safety switch"));
                        Widget spring = getBot().getWidgets().first(widget -> widget != null && widget.getTooltip() != null
                                && widget.getTooltip().equals("Spring"));
                        Widget tool;
                        info("Repair screen open.");
                        if (gear != null && gear.isVisible()) {
                            info("Fixing gear.");
                            tool = getBot().getWidgets().get(409, 1, -1);

                            if (tool != null && helper.getInteractEvent(tool, "Select").executed()) {
                                sleep(2000);
                                if (helper.getInteractEvent(gear, "Gear").executed())
                                    sleep(3000);
                            }
                        }
                        if (safety != null && safety.isVisible()) {
                            info("Fixing safety switch.");
                            tool = getBot().getWidgets().get(409, 2, -1);

                            if (tool != null && helper.getInteractEvent(tool, "Select").executed()) {
                                sleep(2000);
                                if (helper.getInteractEvent(safety, "Safety switch").executed())
                                    sleep(3000);
                            }
                        }
                        if (spring != null && spring.isVisible()) {
                            info("Fixing spring.");
                            tool = getBot().getWidgets().get(409, 3, -1);

                            if (tool != null && helper.getInteractEvent(tool, "Select").executed()) {
                                sleep(2000);
                                if (helper.getInteractEvent(spring, "Spring").executed())
                                    sleep(3000);
                            }
                        }
                    } else if (helper.useOnObject("Broken multicannon", "Toolkit"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 9:
                    if (helper.inArea(NOTES_AREA)) {
                        if (helper.talkTo("Nulodion"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Grabbing nulodions notes");
                        helper.getWeb(NOTES_AREA).execute();
                    }
                    break;

                case 11:
                    // End
                    info("Finished: " + Quest.DWARF_CANNON.name());
                    setComplete();
                    break;
            }
        }
        sleep(600);
    }

    @Override
    public void onFinish() {
        helper.setGrabbedItems(false);
    }
}

