package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class RomeoJulietEvent extends QuestContext implements Logger {
    private final String[] QUEST_DIALOGUE = {
            "Perhaps I could help to find her for you?", "Yes, ok, I'll let her know.",
            "Where can I find Juliet?", "Talk about something else.",
            "Talk about Romeo & Juliet"
    };
    private final Area START_AREA = new Area(3205, 3429, 3219, 3421);
    private final Area JULIET_HOUSE = new Area(3155, 3426, 3161, 3425, 1);
    private final Area FATHER_HOUSE = new Area(3252, 3488, 3255, 3477);
    private final Area APPOTH_HOUSE = new Area(3192, 3406, 3198, 3402);

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public RomeoJulietEvent(QuantumBot context) {
        super(context);
    }


    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Cadava berries", 1);
        if (getBot().getClient().isMembers()) {
            itemReq.put("Varrock teleport", 5);
        }
        info("Started: " + Quest.ROMEO_JULIET.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(144);

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

        info("Quest stage: 144 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getCamera().isLocked()) {
            info("Dialogue");
            if (result == 10 && START_AREA.contains(myPlayer())) {
                if (new InteractEvent(getBot(), myPosition(), "Walk here").executed())
                    sleepUntil(3000, () -> !getBot().getDialogues().inDialogue());
            } else if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption()
                        || result == 10 && START_AREA.contains(myPlayer())).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                info("Handling option");
                info("QUEST_DIALOGUE");
                new DialogueEvent(getBot(), QUEST_DIALOGUE).setInterruptCondition(() -> result == 10 && START_AREA.contains(myPlayer())).execute();
                sleep(1000);
            } else {
                info("No dialogue???");
            }
        } else {
            switch (result) {
                case 60:
                case 20:
                case 0:
                    // Start
                    if (!inArea(START_AREA)) {
                        getWeb(START_AREA).execute();
                        return;
                    }

                    if (talkTo("Romeo"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 50:
                case 10:
                    if (!inArea(JULIET_HOUSE)) {
                        getWeb(JULIET_HOUSE).execute();
                        return;
                    }

                    if (talkTo("Juliet"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 30:
                    if (!inArea(FATHER_HOUSE)) {
                        getWeb(FATHER_HOUSE).execute();
                        return;
                    }

                    if (talkTo("Father Lawrence"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 40:
                    if (!inArea(APPOTH_HOUSE)) {
                        getWeb(APPOTH_HOUSE).execute();
                        return;
                    }

                    if (talkTo("Apothecary"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;

                case 100:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.ROMEO_JULIET.name());
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
