package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class ErnestTheChickenEvent extends BotEvent implements Logger {

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private final String[] QUEST_DIALOGUE = {
            "Aha, sounds like a quest. I'll help."
    };
    private final Area START_AREA = new Area(3106, 3333, 3113, 3327);
    private final Area PROFESSOR_AREA = new Area(3108, 3370, 3112, 3362, 2);

    public ErnestTheChickenEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Spade", 1);
        itemReq.put("Fish food", 1);
        itemReq.put("Poison", 1);
        if (getBot().getClient().isMembers())
            itemReq.put("Draynor manor teleport", 1);
        info("Started: " + Quest.ERNEST_THE_CHICKEN.name());
        helper.setGrabbedItems(true);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(32);

        if (result == 100 && !getBot().getDialogues().inDialogue()){
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

        info("Quest stage: 32 = " + result);
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
                        if (helper.talkTo("Veronica"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (helper.inArea(PROFESSOR_AREA)){
                        if (helper.talkTo("Professor Oddenstein"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(PROFESSOR_AREA).execute();
                    }
                    break;

                case 100:
                    // End
                    info("Finished: " + Quest.ERNEST_THE_CHICKEN.name());
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
