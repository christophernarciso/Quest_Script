package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.DropItemsEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.interactions.ObjectInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class NatureSpiritEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Is there anything else interesting to do around here?", "Well, what is it, I may be able to help?",
            "Yes, I'll go and look for him.", "Yes, I'm sure.", "I'm wearing an amulet of ghost speak!",
            "How can I help?", "I think I've solved the puzzle!"

    };
    private final Area START_AREA = new Area(3435, 9902, 3444, 9886);
    private final Area OUTSIDE_GROTTO_AREA = new Area(3436, 3340, 3446, 3331);
    private final Area INSIDE_GROTTO_AREA = new Area(3435, 9744, 3448, 9732);
    private Area bloomArea;
    private final Tile SPELL_TILE = new Tile(3423, 3336, 0);
    private final Tile TAN_TILE = new Tile(3439, 3336, 0);
    private final Tile ORANGE_TILE = new Tile(3440, 3335, 0);
    private final Tile GREY_TILE = new Tile(3441, 3336, 0);
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
        itemReq.put("Varrock teleport", 1);
        itemReq.put("Lobster", 13);

        info("Started: " + Quest.NATURE_SPIRIT.name());
        setGrabbedItems(false);
        bloomArea = new Area(SPELL_TILE.getX() - 2, SPELL_TILE.getY() + 2, SPELL_TILE.getX() + 2, SPELL_TILE.getY() - 2, 0);
        shouldUseItem = false;
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
            if (result == 20 && !getBot().getInventory().contains("Mirror")) {
                if (getInteractEvent(myPosition(), "Walk here").executed())
                    sleep(1000);
            } else if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption() || result == 20 && !getBot().getInventory().contains("Mirror")).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                info("Handling option");
                info("QUEST_DIALOGUE");
                new DialogueEvent(getBot(), QUEST_DIALOGUE).setInterruptCondition(() -> result == 20 && !getBot().getInventory().contains("Mirror")).execute();
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
                case 85:
                case 80:
                case 75:
                case 45:
                case 40:
                    if (SPELL_TILE.equals(myPosition())) {
                        int count = getBot().getInventory().getEmptySlots();
                        GameObject fungiLog = getBot().getGameObjects().closest(log -> log != null && log.hasName("Fungi on log") && bloomArea.contains(log));
                        if (getBot().getInventory().contains("Druid pouch") && getQuantity(getBot().getInventory(), "Mort myre fungus") >= 3) {
                            if (interactInventory("Druid pouch", "Fill"))
                                sleepUntil(3000, () -> !getBot().getInventory().contains("Mort myre fungus"));
                        } else if (fungiLog != null && new ObjectInteractEvent(getBot(), fungiLog.getName(), "Pick").executed()) {
                            info("PICK FUNGI");
                            sleepUntil(4000, () -> getBot().getInventory().getEmptySlots() < count);
                        } else if (getBot().getInventory().contains("Druidic spell")) {
                            info("Cast spell");
                            if (interactInventory("Druidic spell", "Cast")) {
                                sleepUntil(3000, () -> getBot().getGameObjects().closest(log -> log != null && log.hasName("Fungi on log") && bloomArea.contains(log)) != null);
                            }
                        } else if (getBot().getInventory().contains("Silver sickle (b)")) {
                            info("Cast bloom");
                            if (interactInventory("Silver sickle (b)", "Cast Bloom")) {
                                sleepUntil(3000, () -> getBot().getGameObjects().closest(log -> log != null && log.hasName("Fungi on log") && bloomArea.contains(log)) != null);
                            }
                        }
                    } else {
                        if (INSIDE_GROTTO_AREA.contains(myPosition())) {
                            if (interactObject("Grotto", "Exit")) {
                                sleepUntil(5000, () -> !INSIDE_GROTTO_AREA.contains(myPosition()));
                            }
                        } else if (getBot().getInventory().contains("Rotten food")) {
                            new DropItemsEvent(getBot(), "Pie dish", "Rotten food").executed();
                        } else {
                            info("Walking to bloom tile");
                            getWeb(SPELL_TILE).setInterruptCondition(() -> INSIDE_GROTTO_AREA.contains(myPosition())).execute();
                        }
                    }
                    break;
                case 55:
                    if (inArea(OUTSIDE_GROTTO_AREA)) {
                        if (ORANGE_TILE.equals(myPosition())) {
                            shouldUseItem = false;
                            if (talkTo("Filliman Tarlock")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                            return;
                        }

                        if (getBot().getInventory().contains("Mort myre fungus")) {
                            shouldUseItem = true;
                            if (useOnObject(o -> o != null && "Stone".equals(o.getName()) && o.getTile().equals(TAN_TILE), "Mort myre fungus"))
                                sleepUntil(3000, () -> !getBot().getInventory().contains("Mort myre fungus"));
                        }

                        if (getBot().getInventory().contains("A used spell")) {
                            if (useOnObject(o -> o != null && "Stone".equals(o.getName()) && o.getTile().equals(GREY_TILE), "A used spell"))
                                sleepUntil(3000, () -> !getBot().getInventory().contains("A used spell"));
                        }

                        if (interactObject("Grotto", "Enter")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            getWeb(ORANGE_TILE).executed();
                        }
                    } else {
                        info("Walking to filiman");
                        getWeb(OUTSIDE_GROTTO_AREA).execute();
                    }
                    break;
                case 105:
                case 60:
                    if (INSIDE_GROTTO_AREA.contains(myPosition())) {
                        if (interactObject("Grotto", "Search")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (OUTSIDE_GROTTO_AREA.contains(myPosition())) {
                        if (interactObject("Grotto", "Enter")) {
                            sleepUntil(3000, () -> !OUTSIDE_GROTTO_AREA.contains(myPosition()));
                        }
                    } else {
                        info("Walking to filiman");
                        getWeb(OUTSIDE_GROTTO_AREA).execute();
                    }
                    break;
                case 65:
                    if (interactObject("Grotto", "Search")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 70:
                    if (myPlayer().isAnimating())
                        return;
                    if (talkTo("Nature Spirit")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (interactObject("Grotto", "Search")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 100:
                case 95:
                case 90:
                    if (ourHealthPercent() <= 50)
                        new HealEvent(getBot(),12).executed();

                    NPC ghast = getBot().getNPCs().closest(g -> g != null && "Ghast".equals(g.getName()) && g.hasAction("Attack") && g.isAttackable());
                    if (ghast != null && myPlayer().getInteracting() == null) {
                        if (getInteractEvent(ghast, "Attack").executed()) {
                            sleepUntil(5000, () -> myPlayer().getInteracting() != null);
                        }
                    }
                    break;
                case 110:
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

