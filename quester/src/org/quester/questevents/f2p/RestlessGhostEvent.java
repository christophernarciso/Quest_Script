package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.QuestContext;

import java.util.HashMap;

public class RestlessGhostEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "I'm looking for a quest!", "Ok, let me help then.", "Father Aereck sent me to talk to you.",
            "He's got a ghost haunting his graveyard.", "Yep, now tell me what the problem is.",

    };
    private final Area START_AREA = new Area(3240, 3215, 3247, 3204);
    private final Area URHNEY_AREA = new Area(3144, 3177, 3151, 3173);
    private final Area GHOST_AREA = new Area(3247, 3195, 3252, 3190);
    private final Area SKULL_AREA = new Area(3111, 9569, 3121, 9564);
    private boolean placeHead;

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public RestlessGhostEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        if (getBot().getClient().isMembers()) {
            itemReq.put("Lumbridge teleport", 3);
            itemReq.put("Necklace of passage(1~5)", 1);
        }
        info("Started: " + Quest.THE_RESTLESS_GHOST.name());
        setGrabbedItems(false);
        placeHead = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(107);

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

        info("Quest stage: 107 = " + result);
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
                        if (talkTo("Father Aereck"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (inArea(URHNEY_AREA)) {
                        if (talkTo("Father Urhney"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(URHNEY_AREA).execute();
                    }
                    break;
                case 2:
                    if (getBot().getInventory().contains("Ghostspeak amulet")) {
                        if (interactInventory("Ghostspeak amulet", "Wear"))
                            sleepUntil(3000, () -> !getBot().getInventory().contains("Ghostspeak amulet"));
                    } else if (inArea(GHOST_AREA)) {
                        NPC ghost = getBot().getNPCs().first("Restless ghost");
                        if (ghost != null) {
                            if (getInteractEvent(ghost, "Talk-to").executed())
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else if (interactObject("Coffin", "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(GHOST_AREA).execute();
                    }
                    break;
                case 3:
                    if (inArea(SKULL_AREA)) {
                        if (interactObject("Altar", "Search"))
                            sleepUntil(3000, () -> getBot().getInventory().contains("Ghost's skull"));
                    } else {
                        getWeb(SKULL_AREA).execute();
                    }
                    break;
                case 4:
                    if (inArea(GHOST_AREA)) {
                        NPC ghost = getBot().getNPCs().first("Restless ghost");
                        if (placeHead) {
                            if (useOnObject("Coffin", "Ghost's skull")) {
                                sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else if (ghost != null) {
                            if (getInteractEvent(ghost, "Talk-to").executed()) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                placeHead = true;
                            }
                        } else if (interactObject("Coffin", "Open")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else if (interactObject("Coffin", "Search")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        getWeb(GHOST_AREA).execute();
                    }
                    break;
                case 5:
                    // End
                    info("Finished: " + Quest.THE_RESTLESS_GHOST.name());
                    setComplete();
                    break;
            }
        }
        sleep(600);
    }

    @Override
    public void onFinish() {
        setGrabbedItems(false);
        placeHead = false;
    }
}
