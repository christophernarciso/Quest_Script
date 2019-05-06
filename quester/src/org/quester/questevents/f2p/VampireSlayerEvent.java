package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Food;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class VampireSlayerEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Ok, I'm up for an adventure.", "Morgan needs your help!"
    };
    private final Area START_AREA = new Area(3096, 3270, 3102, 3266);
    private final Area BLUE_MOON_INN_AREA = new Area(3218, 3402, 3227, 3394);
    private final Area VAMPIRE_ROOM_ENTRANCE_AREA = new Area(3113, 3361, 3119, 3354);

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public VampireSlayerEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Hammer", 1);
        itemReq.put("Beer", 2);
        itemReq.put("Garlic", 1);
        itemReq.put("Adamant scimitar", 1);
        itemReq.put("Amulet of strength", 1);
        itemReq.put("Lobster", 12);
        if (getBot().getClient().isMembers()) {
            itemReq.put("Draynor manor teleport", 2);
            itemReq.put("Varrock teleport", 1);
        }

        info("Started: " + Quest.VAMPIRE_SLAYER.name());
        helper.setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(178);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (helper.getBankEvent(itemReq).executed()) {
                    if (helper.closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Adamant scimitar", "Amulet of strength"};
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

        info("Quest stage: 178 = " + result);
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
        } else if (getBot().getInventory().contains("Lobster") && helper.ourHealthPercent() <= 50) {
            new HealEvent(getBot(), getBot().getClient().getSkillBoosted(Skill.HITPOINTS), Food.LOBSTER).executed();
        } else {
            switch (result) {
                case 0:
                    // Start
                    if (helper.inArea(START_AREA)) {
                        info("Talking to Morgan");
                        if (helper.talkTo("Morgan"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Morgan");
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 2:
                    if (getBot().getInventory().contains("Stake")) {
                        if (helper.myPosition().getY() > 9000) {
                            NPC vamp = getBot().getNPCs().first("Count Draynor");
                            if (vamp != null) {
                                if (!helper.myPlayer().isInteracting()) {
                                    info("Attack Count Draynor");
                                    if (helper.getInteractEvent(vamp, "Attack").executed()) {
                                        sleepUntil(3000, () -> helper.myPlayer().isInteracting());
                                    }
                                }
                            } else if (helper.interactObject("Coffin", "Open")
                                    || helper.interactObject("Coffin", "Search")) {
                                info("Starting battle");
                                sleepGameCycle();
                            }
                        } else if (helper.inArea(VAMPIRE_ROOM_ENTRANCE_AREA)) {
                            if (helper.interactObject("Stairs", "Walk-Down")) {
                                info("Going downstairs");
                                sleepUntil(7000, () -> helper.myPosition().getY() > 9000);
                            }
                        } else {
                            info("Walking to battle entrance");
                            helper.getWeb(VAMPIRE_ROOM_ENTRANCE_AREA).execute();
                        }
                        break;
                    }

                    // Asks for a drink. Keep talking..
                case 1:
                    if (helper.inArea(BLUE_MOON_INN_AREA)) {
                        info("Talking to Dr Harlow");
                        if (helper.talkTo("Dr Harlow"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Dr Harlow");
                        helper.getWeb(BLUE_MOON_INN_AREA).execute();
                    }
                    break;

                case 3:
                    // End
                    info("Finished: " + Quest.VAMPIRE_SLAYER.name());
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

