package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.interactions.ObjectInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.QuestContext;

import java.util.HashMap;

public class NatureSpiritEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Is there anything else interesting to do around here?", "Well, what is it, I may be able to help?",
            "Yes, I'll go and look for him.", "Yes, I'm sure.", "I'm wearing an amulet of ghost speak!",
            "How can I help?"

    };
    private final Area START_AREA = new Area(3435, 9902, 3444, 9886);
    private final Area OUTSIDE_GROTTO_AREA = new Area(3436, 3340, 3446, 3331);
    private Area bloomArea;
    private final Tile SPELL_TILE = new Tile(3423, 3336, 0);
    private boolean shouldUseItem;

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public NatureSpiritEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Ghostspeak amulet", 1);
        itemReq.put("Silver sickle", 1);
        itemReq.put("Adamant scimitar", 1);
        itemReq.put("Lobster", 13);

        info("Started: " + Quest.NATURE_SPIRIT.name());
        setGrabbedItems(false);
        bloomArea = new Area(SPELL_TILE.getX() - 2, SPELL_TILE.getY() + 2, SPELL_TILE.getX() + 2, SPELL_TILE.getY() - 2, 0);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(307);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (getBankEvent(itemReq).executed()) {
                    if (closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Adamant scimitar", "Ghostspeak amulet"};
                        for (String s : equipables) {
                            if (interactInventory(s, "Wear", "Wield"))
                                sleep(1200);
                        }
                        // At this point we now have our items equipped.
                        setGrabbedItems(true);
                    }
                }
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

        info("Quest stage: 307 = " + result);
        if (getBot().getDialogues().inDialogue() && !shouldUseItem) {
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
                case 35:
                case 0:
                    // Small delay while he blesses us
                    if (result == 35)
                        sleep(3000);
                    // Start
                    if (inArea(START_AREA)) {
                        info("Talking to Drezel");
                        if (talkTo("Drezel"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Drezel");
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 5:
                    // Finished talking and he gave us pies walk to filiman grotto
                    info("Walking to filiman");
                    getWeb(OUTSIDE_GROTTO_AREA).execute();
                    break;
                case 50:
                case 10:
                    if (inArea(OUTSIDE_GROTTO_AREA)) {
                        info("Talking to filiman");
                        if (interactObject("Grotto", "Enter"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to filiman");
                        getWeb(OUTSIDE_GROTTO_AREA).execute();
                    }
                    break;
                case 20:
                    if (!getBot().getInventory().contains("Washing bowl")) {
                        if (interactGroundItem("Washing bowl", "Take"))
                            sleepUntil(5000, () -> getBot().getInventory().contains("Washing bowl"));
                    } else if (!getBot().getInventory().contains("Mirror")) {
                        shouldUseItem = true;
                        if (interactGroundItem("Mirror", "Take"))
                            sleepUntil(5000, () -> getBot().getInventory().contains("Mirror"));
                    } else if (useOnNPC("Filliman Tarlock", "Mirror")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        shouldUseItem = false;
                    } else if (interactObject("Grotto", "Enter")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 25:
                    if (!getBot().getInventory().contains("Journal")) {
                        shouldUseItem = true;
                        if (interactObject("Grotto tree", "Search"))
                            sleepUntil(5000, () -> getBot().getInventory().contains("Journal"));
                    } else if (useOnNPC("Filliman Tarlock", "Journal")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        shouldUseItem = false;
                    } else if (interactObject("Grotto", "Enter")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 30:
                    if (talkTo("Filliman Tarlock")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (interactObject("Grotto", "Enter")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 45:
                case 40:
                    if (SPELL_TILE.equals(myPosition())) {
                        int count = getBot().getInventory().getEmptySlots();
                        GameObject fungiLog = getBot().getGameObjects().closest(log -> log != null && log.hasName("Fungi on log") && bloomArea.contains(log));
                        if (fungiLog != null && new ObjectInteractEvent(getBot(), fungiLog.getName(), "Pick").executed()) {
                            info("PICK FUNGI");
                            sleepUntil(4000, () -> getBot().getInventory().getEmptySlots() < count);
                        } else if (getBot().getInventory().contains("Druidic spell")) {
                            info("Cast spell");
                            if (interactInventory("Druidic spell", "Cast")) {
                                sleepUntil(3000, () -> getBot().getGameObjects().closest(log -> log != null && log.hasName("Fungi on log") && bloomArea.contains(log)) != null);
                            }
                        }
                    } else {
                        info("Walking to bloom tile");
                        getWeb(SPELL_TILE).execute();
                    }
                    break;
                case 55:
                    break;
                case 60:
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
        setGrabbedItems(false);
    }
}

