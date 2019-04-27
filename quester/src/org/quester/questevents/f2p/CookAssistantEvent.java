package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class CookAssistantEvent extends BotEvent implements Logger {

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private final String[] QUEST_DIALOGUE = {
            "What's wrong?", "I'm always happy to help a cook in distress."
    };
    private final Area START_AREA = new Area(3205, 3217, 3212, 3211);

    public CookAssistantEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Bucket of milk", 1);
        itemReq.put("Pot of flour", 1);
        itemReq.put("Egg", 1);
        if (getBot().getClient().isMembers())
            itemReq.put("Lumbridge teleport", 1);
        info("Started: " + Quest.COOKS_ASSISTANT.name());
        helper.setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(29);

        if (result == 2 && !getBot().getDialogues().inDialogue()){
            setComplete();
            return;
        }

        if (!helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
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

        info("Quest stage: 29 = " + result);
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
                        if (helper.talkTo("Cook"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;

                case 2:
                    // End
                    info("Finished: " + Quest.COOKS_ASSISTANT.name());
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
