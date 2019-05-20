package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.containers.EquipmentInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class AnimalMagnetismEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "I'm here about a quest.", "Okay, you need it more that I do, I suppose.",

    };
    private final Area START_AREA = new Area(3091, 3363, 3096, 3354);
    private final Area UNDEAD_FARM_AREA = new Area(3626, 3527, 3630, 3524);
    private final Area CRONE_AREA = new Area(3460, 3560, 3463, 3556);
    private boolean talkToHusband;

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public AnimalMagnetismEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Mithril axe", 1);
        itemReq.put("Iron bar", 5);
        itemReq.put("Ghostspeak amulet", 1);
        itemReq.put("Bones", 4);
        itemReq.put("Bucket", 4);
        itemReq.put("Pot", 4);
        itemReq.put("Hammer", 1);
        itemReq.put("Hard leather", 1);
        itemReq.put("Holy symbol", 1);
        itemReq.put("Polished buttons", 1);
        itemReq.put("Games necklace(1~8)", 1);
        itemReq.put("Draynor manor teleport", 4);

        info("Started: " + Quest.ANIMAL_MAGNETISM.name());
        helper.setGrabbedItems(false);
        talkToHusband = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(939);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (helper.getBankEvent(itemReq).executed()) {
                    if (helper.closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Ghostspeak amulet"};
                        for (String s : equipables) {
                            if (helper.interactInventory(s, "Wear", "Wield"))
                                sleep(1200);
                        }
                        // At this point we now have our items equipped.
                        helper.setGrabbedItems(true);
                    }
                }
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

        info("Quest stage: 939 = " + result);
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
                        info("Talking to Ava");
                        if (helper.talkTo("Ava"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Ava");
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 76:
                    // Got crone amulet.
                    if (getBot().getInventory().contains("Ghostspeak amulet")){
                        info("Wearing our ghostspeak amulet again.");
                        if (helper.interactInventory("Ghostspeak amulet", "Wear")){
                            sleepUntil(4000, () -> getBot().getEquipment().contains("Ghostspeak amulet"));
                        }
                    }
                case 60:
                    // Husband and wife exchanges
                case 50:
                    // Husband and wife exchanges
                case 40:
                    // Husband and wife exchanges
                case 30:
                    // Husband and wife exchanges
                case 20:
                    // Husband and wife exchanges
                case 10:
                    if (talkToHusband){
                        info("Talk to husband.");
                        if (helper.talkTo("Alice's husband")) {
                            sleepUntil(10000, () -> getBot().getDialogues().inDialogue());
                            talkToHusband = false;
                        }
                    } else if (helper.inArea(UNDEAD_FARM_AREA)){
                        info("Talk to Alice");
                        if (helper.talkTo("Alice")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            talkToHusband = true;
                        }
                    } else {
                        info("Walking to Alice");
                        helper.getWeb(UNDEAD_FARM_AREA).execute();
                    }
                    break;
                case 73:
                    if (getBot().getEquipment().contains("Ghostspeak amulet")){
                        if (new EquipmentInteractEvent(getBot(), "Ghostspeak amulet", "Remove").executed()) {
                            info("Removed ghostspeak amulet");
                            sleepUntil(4000, () -> !getBot().getEquipment().contains("Ghostspeak amulet"));
                        }
                    }
                case 70:
                    // Get crone amulet
                    if (helper.inArea(CRONE_AREA)){
                        info("Talking to crone");
                        if (helper.talkTo("Old crone")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to crone");
                        helper.getWeb(CRONE_AREA).execute();
                    }
                    break;

                case 300:
                    // End
                    info("Finished: " + Quest.ANIMAL_MAGNETISM.name());
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


