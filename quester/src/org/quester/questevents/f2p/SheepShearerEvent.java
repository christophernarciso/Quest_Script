package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class SheepShearerEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "I'm looking for a quest.", "Yes okay. I can do that."
    };
    private final Area START_AREA = new Area(3188, 3275, 3192, 3270);

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public SheepShearerEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Ball of wool", 20);
        if (getBot().getClient().isMembers()) {
            itemReq.put("Lumbridge teleport", 1);
            itemReq.put("Varrock teleport", 1);
        }
        info("Started: " + Quest.SHEEP_SHEARER.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(179);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw;
                setGrabbedItems(getBankEvent(itemReq).executed());
            } else {
                // Load buy event and execute buy orders
                if (getBuyableEvent(itemReq) == null) {
                    info("Failed: Not enough coins. Setting complete and stopping.");
                    setComplete();
                    getBot().stop();
                    return;
                }
                //info("GE event execute");
                getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 179 = " + result);
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
                    if (inArea(START_AREA)) {
                        if (talkTo("Fred the Farmer"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(START_AREA).execute();
                    }
                    break;

                case 21:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.SHEEP_SHEARER.name());
                    setComplete();
                    break;
            }
        }
        sleep(600);
    }

    @Override
    public void onFinish() {
        setGrabbedItems(false);
    }
}
