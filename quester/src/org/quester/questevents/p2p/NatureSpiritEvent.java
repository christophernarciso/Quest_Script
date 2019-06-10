package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class NatureSpiritEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Is there anything else interesting to do around here?", "Well, what is it, I may be able to help?",

    };
    private final Area START_AREA = new Area(3435, 9902, 3444, 9886);

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public NatureSpiritEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Ghostspeak amulet", 1);
        itemReq.put("Silver sickle", 1);
        itemReq.put("Adamant scimitar", 1);
        itemReq.put("Lobster", 13);

        info("Started: " + Quest.NATURE_SPIRIT.name());
        helper.setGrabbedItems(true);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(307);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (helper.getBankEvent(itemReq).executed()) {
                    if (helper.closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Adamant scimitar", "Ghostspeak amulet"};
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

        info("Quest stage: 307 = " + result);
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
                        info("Talking to Drezel");
                        if (helper.talkTo("Drezel"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Drezel");
                        helper.getWeb(START_AREA).execute();
                    }
                    break;

                case 30:
                    // End
                    info("Finished: " + Quest.NATURE_SPIRIT.name());
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

