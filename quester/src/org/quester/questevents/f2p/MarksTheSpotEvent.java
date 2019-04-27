package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

// Work in progress
public class MarksTheSpotEvent extends BotEvent implements Logger {

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private final String[] QUEST_DIALOGUE = {
            "I'm looking for a quest.", "Sounds good, what should I do?", "Okay, thanks Veos.",
    };

    private Tile MARK_TILE = new Tile(3230, 3209, 0);

    private Area START_AREA = new Area(
            new int[][]{
                    {3226, 3242},
                    {3226, 3239},
                    {3228, 3239},
                    {3228, 3236},
                    {3234, 3236},
                    {3234, 3242},
                    {3234, 3243},
                    {3226, 3243}
            }
    );

    public MarksTheSpotEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Spade", 1);
        if (getBot().getClient().isMembers()) {
            itemReq.put("Lumbridge teleport", 1);
            itemReq.put("Necklace of passage(1~5)", 1);
        }
        info("Started: X Marks the Spot");
        helper.setGrabbedItems(true);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(173);

        if (result == 21 && !getBot().getDialogues().inDialogue()) {
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

        info("Quest stage: 173 = " + result);
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
                    if (getBot().getInventory().contains("Ancient casket"))
                        START_AREA = new Area(3047, 3249, 3055, 3245);

                    if (helper.inArea(START_AREA)) {
                        if (helper.talkTo("Veos"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (getBot().getInventory().contains(23068))
                        MARK_TILE = new Tile(3203, 3212, 0);
                    else if (getBot().getInventory().contains("Mysterious Orb"))
                        MARK_TILE = new Tile(3109, 3264, 0);
                    else if (getBot().getInventory().contains(23070))
                        MARK_TILE = new Tile(3077, 3260, 0);

                    if (helper.myPosition().equals(MARK_TILE)) {
                        if (helper.interactInventory("Spade", "Dig"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(MARK_TILE).setDestinationAccuracy(0).execute();
                    }
                    break;

                case 21:
                    // End
                    info("Finished: X Marks the Spot");
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
