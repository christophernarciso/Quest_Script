package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GroundItem;
import org.quantumbot.api.equipment.AttackStyle;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.AttackStyleEvent;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class WitchPotionEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "I am in search of a quest.", "Yes, help me become one with my darker side.",
    };
    private final Area START_AREA = new Area(2965, 3208, 2970, 3203);
    private final Area STOVE_AREA = new Area(2963, 3216, 2970, 3209);
    private final Area RAT_AREA = new Area(2953, 3205, 2960, 3202);
    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public WitchPotionEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Cooked meat", 1);
        itemReq.put("Eye of newt", 1);
        itemReq.put("Onion", 1);
        itemReq.put("Iron scimitar", 1);
        if (getBot().getClient().isMembers())
            itemReq.put("Falador teleport", 1);
        info("Started: " + Quest.WITCHS_POTION.name());
        helper.setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(67);

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

        info("Quest stage: 67 = " + result);
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
                        if (helper.talkTo("Hetty"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;

                case 1:
                    if (!getBot().getInventory().contains("Burnt meat")) {
                        if (helper.inArea(STOVE_AREA)) {
                            if (getBot().getInventory().isSelected(i -> i != null && i.hasName("Cooked meat"))) {
                                if (helper.interactObject("Range", "Use"))
                                    sleepUntil(3000, () -> getBot().getInventory().contains("Burnt meat"));
                            } else {
                                if (helper.interactInventory("Cooked meat", "Use"))
                                    sleepUntil(3000, () -> getBot().getInventory().isSelected(i -> i != null && i.hasName("Cooked meat")));
                            }
                        } else {
                            helper.getWeb(STOVE_AREA).execute();
                        }
                    } else if (!getBot().getInventory().contains("Rat's tail")) {
                        if (getBot().getInventory().contains("Iron scimitar")) {
                            if (helper.interactInventory("Iron scimitar", "Wield"))
                                sleepUntil(3000, () -> !getBot().getInventory().contains("Iron scimitar"));

                            new AttackStyleEvent(getBot(), AttackStyle.MELEE_AGGRESSIVE).executed();
                        } else if (helper.inArea(RAT_AREA)) {
                            GroundItem tail = getBot().getGroundItems().first("Rat's tail");
                            if (tail != null) {
                                if (helper.getInteractEvent(tail, "Take").executed())
                                    sleepUntil(3000, () -> getBot().getInventory().contains("Rat's tail"));
                            } else if (!helper.myPlayer().isInteracting() && helper.interactNPC(r -> r != null && r.isAttackable(), "Attack"))
                                sleepUntil(3000, () -> helper.myPlayer().isInteracting());
                        } else {
                            helper.getWeb(RAT_AREA).execute();
                        }
                    } else {
                        if (helper.inArea(START_AREA)) {
                            if (helper.talkTo("Hetty"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else {
                            helper.getWeb(START_AREA).execute();
                        }
                    }
                    break;
                case 2:
                    if (helper.interactObject("Cauldron", "Drink From"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 3:
                    // End
                    info("Finished: " + Quest.WITCHS_POTION.name());
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
