package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.EnterAmountEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class DeathPlateauEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Do you have any quests for me?", "No but perhaps I could try and find one?", "I'm looking for the guard that was on last night.",
            "You're the guard that was on duty last night?", "Where were you when you last had the combination?", "OK, I'll get those for you."
    };
    private final int[] COMBINATION_ID = {3113, 3112, 3111, 3110, 3109};
    private final Tile[] COMBINATION_TILES = {new Tile(2895, 3564, 0), new Tile(2895, 3563, 0), new Tile(2895, 3562, 0),
            new Tile(2894, 3562, 0), new Tile(2894, 3563, 0)};
    private final Area START_AREA = new Area(2891, 3532, 2900, 3526);
    private final Area SECOND_STAGE_AREA = new Area(
            new int[][]{
                    {2896, 3569},
                    {2896, 3567},
                    {2892, 3567},
                    {2892, 3561},
                    {2896, 3561},
                    {2896, 3556},
                    {2901, 3556},
                    {2902, 3556},
                    {2902, 3561},
                    {2906, 3561},
                    {2906, 3566},
                    {2906, 3567},
                    {2902, 3567},
                    {2902, 3571},
                    {2896, 3571}
            }
            , 1);
    private final Area THIRD_STAGE_AREA = new Area(2905, 3544, 2915, 3536, 1);
    private final Area THIRD_STAGE_PART_TWO_AREA = new Area(2905, 3542, 2906, 3536, 1);
    private final Area FOURTH_STAGE_AREA = new Area(
            new int[][]{
                    {2893, 3566},
                    {2893, 3561},
                    {2896, 3561},
                    {2896, 3558},
                    {2898, 3558},
                    {2898, 3559},
                    {2900, 3559},
                    {2900, 3558},
                    {2902, 3558},
                    {2902, 3561},
                    {2905, 3561},
                    {2905, 3567},
                    {2901, 3567},
                    {2901, 3570},
                    {2896, 3570},
                    {2896, 3567},
                    {2893, 3567}
            }
    );
    private final Area FIFTH_STAGE_AREA = new Area(2891, 3571, 2895, 3567, 1);
    private final Area SIXTH_STAGE_AREA = new Area(2855, 3581, 2871, 3569);
    private final Area SIXTH_STAGE_PART_TWO_AREA = new Area(2264, 4763, 2275, 4750);
    private final Area SEVENTH_STAGE_AREA = new Area(2818, 3557, 2822, 3553); //Tenzing
    private final Area EIGHTH_STAGE_AREA = new Area(2917, 3577, 2923, 3572);
    private final Area NINTH_STAGE_AREA = new Area(2814, 3562, 2821, 3558);
    private int grabStart = 3561, index = 0;
    private boolean shouldGrabBalls, shouldTalkToGuard, shouldTalkToSaba, shouldTalkToDunstan, shouldFindPath;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public DeathPlateauEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Coins", 5000);
        itemReq.put("Bread", 10);
        itemReq.put("Trout", 10);
        itemReq.put("Iron bar", 1);
        itemReq.put("Asgarnian ale", 1);
        itemReq.put("Premade blurb' sp.", 1);
        itemReq.put("Games necklace(1~8)", 1);
        info("Started: " + Quest.DEATH_PLATEAU.name());
        setGrabbedItems(false);
        shouldGrabBalls = true;
        shouldTalkToGuard = true;
        shouldTalkToSaba = true;
        shouldTalkToDunstan = true;
        shouldFindPath = true;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(314);

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

        info("Quest stage: 314 = " + result);
        if (getBot().getDialogues().inDialogue()) {
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                Widget continueGambling = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Continue..."));
                if (continueGambling != null && continueGambling.isVisible()) {
                    info("Selecting: Ok");
                    if (new InteractEvent(getBot(), continueGambling, "Ok").executed())
                        sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                } else if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption()).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                info("Handling option");
                if (result == 50) {
                    info("Selecting result 50 dialogue");
                    if (getBot().getInventory().contains("Premade blurb' sp."))
                        new DialogueEvent(getBot(), "Can I buy you a drink?").execute();
                    else
                        new DialogueEvent(getBot(), "Would you like to gamble?").execute();
                } else if (result == 70 && inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                    info("Selecting result 70 dialogue");
                    new DialogueEvent(getBot(), "Do you know of another way up Death Plateau?").execute();
                } else {
                    info("QUEST_DIALOGUE");
                    new DialogueEvent(getBot(), QUEST_DIALOGUE).execute();
                }
                sleep(1000);
            } else {
                info("No dialogue???");
            }
        } else {
            switch (result) {
                case 0:
                    // Start
                    if (inArea(START_AREA)) {
                        if (talkTo("Denulth"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walk to quest start");
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 10:
                    // After accepting quest & now find eohric to talk about the guard
                    if (inArea(SECOND_STAGE_AREA)) {
                        if (talkTo("Eohric"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(SECOND_STAGE_AREA).execute();
                    }
                    break;
                case 20:
                    // Head to the guard and ask for combination
                    sleep(1000);
                    if (inArea(THIRD_STAGE_PART_TWO_AREA)) {
                        if (talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (inArea(THIRD_STAGE_AREA)) {
                        if (interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(THIRD_STAGE_AREA).execute();
                    }
                    break;
                case 30:
                    // Head back to eohric after guard refuses to talk
                    if (inArea(SECOND_STAGE_AREA)) {
                        if (talkTo("Eohric"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(SECOND_STAGE_AREA).execute();
                    }
                    break;
                case 40:
                    // Head back to the guard after talking to eohric
                    sleep(1000);
                    if (inArea(THIRD_STAGE_PART_TWO_AREA)) {
                        if (talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (inArea(THIRD_STAGE_AREA)) {
                        if (interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(THIRD_STAGE_AREA).execute();
                    }
                    break;
                case 50:
                    // Offer him a drink & gamble
                    Widget gamblingScreen = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null
                            && (w.getText().equals("Harold rolls...") || w.getText().equals("Your roll...") || w.getText().equals("You win!") || w.getText().equals("You lose!")));

                    Widget enterAmountScreen = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Enter amount:"));
                    if (enterAmountScreen != null && enterAmountScreen.isVisible()) {
                        if (new EnterAmountEvent(getBot(), 100).executed()) {
                            info("Successfully entered amount.");
                        }
                    } else if (gamblingScreen != null && gamblingScreen.isVisible()) {
                        Widget rollDice = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Roll Dice!"));
                        Widget continueGambling = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Continue..."));
                        if (rollDice != null && rollDice.isVisible()) {
                            if (getInteractEvent(rollDice, "Ok").executed())
                                sleepUntil(10000, () -> getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("You win!")) != null);
                        } else if (continueGambling != null && continueGambling.isVisible()) {
                            if (getInteractEvent(continueGambling, "Ok").executed())
                                sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                        }
                    } else {
                        if (talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 55:
                    // For more room for the combination unlock
                    if (getBot().getInventory().contains("Coins"))
                        interactInventory("Coins", "Drop");

                    // Won the gambling & have IOU note
                    if (getBot().getInventory().contains("Iou") && interactInventory("Iou", "Read")) {
                        sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                    }
                    break;
                case 60:
                    // Have combination -> grab stone balls -> do combination
                    if (inArea(FOURTH_STAGE_AREA)) {
                        if (shouldGrabBalls) {
                            if (grabStart == 3566 || getBot().getInventory().isFull())
                                shouldGrabBalls = false;
                            else {
                                info("Grabbing stone balls");
                                int last = getBot().getInventory().getEmptySlots();
                                if (interactGroundItem(o -> o != null &&
                                        o.getName().equals("Stone ball") && o.getTile().getY() == grabStart, "Take")) {
                                    sleepUntil(10000, () -> getBot().getInventory().getEmptySlots() < last);
                                    grabStart++;
                                }
                            }
                        } else {
                            info("handling combination");
                            int last = getBot().getInventory().getEmptySlots();

                            if (useOnObject(o -> o != null && o.getName().equals("Stone Mechanism") && o.getTile().equals(COMBINATION_TILES[index]), COMBINATION_ID[index])) {
                                sleepUntil(10000, () -> getBot().getInventory().getEmptySlots() > last);
                                index++;
                                sleep(600);
                            }
                        }
                    } else {
                        getWeb(new Tile(2896, 3563, 0)).execute();
                    }
                    break;
                case 70:
                    // Talk to guard archer, saba, tenzing, dunstan, denulth, tenzing, denulth. in order.. | code is in reverse order depending on flags or items obtained.
                    if (getBot().getInventory().contains("Secret way map")) {
                        if (inArea(SEVENTH_STAGE_AREA)) {
                            if (interactObject(o -> o != null && o.getName().equals("Door") && o.getTile().getX() == 2820, "Open"))
                                sleepUntil(7000, () -> inArea(NINTH_STAGE_AREA));
                        } else if (inArea(NINTH_STAGE_AREA)) {
                            if (interactObject("Stile", "Climb-over"))
                                sleepUntil(7000, () -> !inArea(NINTH_STAGE_AREA));
                        } else {
                            if (shouldFindPath) {
                                if (getWeb(new Tile(2865, 3609, 0)).executed())
                                    shouldFindPath = false;
                            } else {
                                if (inArea(START_AREA)) {
                                    if (talkTo("Denulth"))
                                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                } else {
                                    getWeb(START_AREA).execute();
                                }
                            }
                        }
                    } else if (getBot().getInventory().contains("Certificate")) {
                        if (inArea(EIGHTH_STAGE_AREA)) {
                            if (talkTo("Dunstan")) {
                                sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                            }
                        } else {
                            getWeb(EIGHTH_STAGE_AREA).execute();
                        }
                    } else if (getBot().getInventory().contains("Climbing boots")) {
                        if (shouldTalkToDunstan) {
                            if (inArea(EIGHTH_STAGE_AREA)) {
                                if (talkTo("Dunstan")) {
                                    sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                                    shouldTalkToDunstan = false;
                                }
                            } else {
                                getWeb(EIGHTH_STAGE_AREA).execute();
                            }
                        } else {
                            if (inArea(START_AREA)) {
                                if (talkTo("Denulth"))
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            } else {
                                getWeb(START_AREA).execute();
                            }
                        }
                    } else if (shouldTalkToGuard) {
                        if (inArea(FIFTH_STAGE_AREA)) {
                            info("Talk to guard: " + shouldTalkToGuard);
                            if (talkTo("Archer")) {
                                shouldTalkToGuard = false;
                                info("Talk to guard: " + shouldTalkToGuard);
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else {
                            getWeb(FIFTH_STAGE_AREA).execute();
                        }
                    } else if (shouldTalkToSaba) {
                        if (inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                            if (talkTo("Saba")) {
                                shouldTalkToSaba = false;
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else if (inArea(SIXTH_STAGE_AREA)) {
                            if (interactObject("Cave Entrance", "Enter"))
                                sleepUntil(7000, () -> inArea(SIXTH_STAGE_PART_TWO_AREA));
                        } else {
                            if (inArea(FIFTH_STAGE_AREA)) {
                                if (interactObject("Ladder", "Climb-down"))
                                    sleepUntil(7000, () -> !inArea(FIFTH_STAGE_AREA));
                                // Updates too quickly so we need a small delay for the local pathfinder
                                sleep(2000);
                                return;
                            }
                            getWeb(SIXTH_STAGE_AREA).execute();
                        }
                    } else {
                        if (inArea(SEVENTH_STAGE_AREA)) {
                            if (talkTo("Tenzing")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else { // If we have spiked boots our logic should return back here
                            if (inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                                if (interactObject("Cave Exit", "Exit"))
                                    sleepUntil(7000, () -> inArea(SIXTH_STAGE_AREA));
                            } else {
                                getWeb(SEVENTH_STAGE_AREA).setInterruptCondition(() -> getBot().getDialogues().isPendingContinuation()).execute();
                            }
                        }
                    }
                    break;
                case 80:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.DEATH_PLATEAU.name());
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
