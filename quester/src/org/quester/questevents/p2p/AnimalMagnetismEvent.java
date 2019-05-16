package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.containers.DepositEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;

public class AnimalMagnetismEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            ""
    };
    private final Area START_AREA = new Area(3091, 3363, 3096, 3354);

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
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(939);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (helper.getBankEvent(itemReq).executed()) {
                    sleep(1000);
                    if (helper.openBank()) {
                        // Execute deposit equipment.
                        String[] deposits = {"Earth rune", "Water rune", "Air rune"};
                        for (String s : deposits) {
                            if (new DepositEvent(getBot(), s, Integer.MAX_VALUE).executed())
                                sleep(1200);
                        }

                        sleep(700);

                        // Make sure we deposit worn items.
                        new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible()
                                && w.hasAction("Deposit worn items")).executed();

                        // At this point we now have our items.
                        helper.setGrabbedItems(true);
                        sleep(700);
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
                        info("Talking to Elena");
                        if (helper.talkTo("Elena"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Elena");
                        helper.getWeb(START_AREA).execute();
                    }
                    break;

                case 30:
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


