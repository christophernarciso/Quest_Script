package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;
import sun.audio.AudioData;

import java.util.HashMap;

public class RuneMysteriesEvent extends BotEvent implements Logger {

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private final String[] QUEST_DIALOGUE = {
            "Have you any quests for me?", "Sure, no problem.", "I'm looking for the head wizard.",
            "Ok, here you are.", "Yes, certainly.", "I have been sent here with a package for you."
    };
    private final Area START_AREA = new Area(3208, 3225, 3213, 3218, 1);
    private final Area SEDRI_AREA = new Area(3096, 9574, 3107, 9566);
    private final Area AUBRY_AREA = new Area(
            new int[][]{
                    {3250, 3402},
                    {3250, 3400},
                    {3252, 3399},
                    {3254, 3399},
                    {3255, 3400},
                    {3256, 3401},
                    {3256, 3403},
                    {3253, 3404},
                    {3252, 3402}
            }
    );

    public RuneMysteriesEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        if (getBot().getClient().isMembers()) {
            itemReq.put("Necklace of passage(1~5)", 1);
            itemReq.put("Varrock teleport", 1);
            itemReq.put("Lumbridge teleport", 1);
        }
        info("Started: " + Quest.RUNE_MYSTERIES.name());
        helper.setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(63);

        if (result == 6 && !getBot().getDialogues().inDialogue()) {
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

        info("Quest stage: 63 = " + result);
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
                        if (helper.talkTo("Duke Horacio"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 5:
                case 1:
                    if (helper.inArea(SEDRI_AREA)) {
                        if (helper.talkTo("Sedridor"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(SEDRI_AREA).execute();
                    }
                    break;
                case 4:
                case 3:
                    if (helper.inArea(AUBRY_AREA)){
                        if (helper.talkTo("Aubury"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(AUBRY_AREA).execute();
                    }
                    break;
                case 6:
                    // End
                    info("Finished: " + Quest.RUNE_MYSTERIES.name());
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
