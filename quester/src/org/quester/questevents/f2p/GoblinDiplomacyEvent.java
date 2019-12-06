package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.ItemCombineEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class GoblinDiplomacyEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Do you want me to pick an armour colour for you?", "No, he doesn't look fat", "What about a different colour?",
            "I have some orange armour here", "I have some blue armour here", "I have some brown armour here"
    };
    private final Area START_AREA = new Area(
            new int[][]{
                    { 2954, 3510 },
                    { 2954, 3513 },
                    { 2956, 3513 },
                    { 2956, 3515 },
                    { 2959, 3515 },
                    { 2959, 3514 },
                    { 2962, 3514 },
                    { 2962, 3510 }
            }
    );

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public GoblinDiplomacyEvent(QuantumBot context) {
        super(context);
    }


    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Goblin mail", 3);
        itemReq.put("Blue dye", 1);
        itemReq.put("Orange dye", 1);
        if (getBot().getClient().isMembers()) {
            itemReq.put("Falador teleport", 1);
            itemReq.put("Varrock teleport", 1);
        }
        info("Started: " + Quest.GOBLIN_DIPLOMACY.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(62);

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

        info("Quest stage: 62 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getCamera().isLocked()) {
            info("Dialogue");
            if (result == 6 && !new CloseInterfacesEvent(getBot()).executed())
                return;
            else if (getBot().getDialogues().isPendingContinuation()) {
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
                case 5:
                case 0:
                    // Start
                    if (!inArea(START_AREA)) {
                        getWeb(START_AREA).execute();
                        return;
                    }

                    if (talkTo("General Bentnoze"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;

                case 3:
                    if (getBot().getInventory().contains("Orange dye")) {
                        if (new ItemCombineEvent(getBot(), "Goblin mail", "Orange dye").executed())
                            sleepUntil(3000, () -> !getBot().getInventory().contains("Orange dye"));
                    } else if (talkTo("General Bentnoze"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 4:
                    if (getBot().getInventory().contains("Blue dye")) {
                        if (new ItemCombineEvent(getBot(), "Goblin mail", "Blue dye").executed())
                            sleepUntil(3000, () -> !getBot().getInventory().contains("Blue dye"));
                    } else if (talkTo("General Bentnoze"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 6:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.GOBLIN_DIPLOMACY.name());
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
