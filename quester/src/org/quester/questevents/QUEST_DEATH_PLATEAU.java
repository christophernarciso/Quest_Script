package org.quester.questevents;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.EnterAmountEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

public class QUEST_DEATH_PLATEAU extends BotEvent implements Logger {

    private HelperMethods helper;
    private int grabStart = 3561, index = 0;
    private boolean shouldGrabBalls = true, shouldTalkToGuard = true, shouldTalkToSaba = true, shouldTalkToDunstan = true, shouldFindPath = true;
    private final String[] QUEST_DIALOGUE_DEATH_PLAT = {"Do you have any quests for me?", "No but perhaps I could try and find one?", "I'm looking for the guard that was on last night.",
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

    public QUEST_DEATH_PLATEAU(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getClient().getVarp(314);

        info("Quest stage: 314 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getDialogues().isPendingContinuation() || getBot().getDialogues().isPendingOption()) {
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                Widget continueGambling = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Continue..."));
                if (continueGambling != null && continueGambling.isVisible()) {
                    info("Selecting: Ok");
                    if (new InteractEvent(getBot(), continueGambling, "Ok").executed())
                        sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                } else if (new DialogueEvent(getBot()).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                info("Handling option");
                if (result == 50) {
                    info("Selecting result 50 dialogue");
                    if (getBot().getInventory().contains("Premade blurb' sp."))
                        new DialogueEvent(getBot(), "Can I buy you a drink?").execute();
                    else
                        new DialogueEvent(getBot(), "Would you like to gamble?").execute();
                } else if (result == 70 && helper.inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                    info("Selecting result 70 dialogue");
                    new DialogueEvent(getBot(), "Do you know of another way up Death Plateau?").execute();
                } else {
                    info("QUEST_DIALOGUE");
                    new DialogueEvent(getBot(), QUEST_DIALOGUE_DEATH_PLAT).execute();
                }
                sleep(1000);
            } else if (new EnterAmountEvent(getBot(), 100).executed()) {
                info("Successfully entered amount.");
            } else {
                info("No dialogue???");
            }
        } else {
            switch (result) {
                case 0:
                    //start
                    if (helper.inArea(START_AREA)) {
                        if (helper.talkTo("Denulth"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 10:
                    //after accepting quest & now find eohric to talk about the guard
                    if (helper.inArea(SECOND_STAGE_AREA)) {
                        if (helper.talkTo("Eohric"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(SECOND_STAGE_AREA).execute();
                    }
                    break;
                case 20:
                    //head to the guard and ask for combination
                    sleep(1000);
                    if (helper.inArea(THIRD_STAGE_PART_TWO_AREA)) {
                        if (helper.talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (helper.inArea(THIRD_STAGE_AREA)) {
                        if (helper.interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(THIRD_STAGE_AREA).execute();
                    }
                    break;
                case 30:
                    //head back to eohric after guard refuses to talk
                    if (helper.inArea(SECOND_STAGE_AREA)) {
                        if (helper.talkTo("Eohric"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(SECOND_STAGE_AREA).execute();
                    }
                    break;
                case 40:
                    //head back to the guard after talking to eohric
                    sleep(1000);
                    if (helper.inArea(THIRD_STAGE_PART_TWO_AREA)) {
                        if (helper.talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (helper.inArea(THIRD_STAGE_AREA)) {
                        if (helper.interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(THIRD_STAGE_AREA).execute();
                    }
                    break;
                case 50:
                    //offer him a drink & gamble
                    //Widget gamblingScreen = getWidgets().getWidgetContainingText(99, "Harold rolls...", "Your roll...", "You win!", "You lose!");
                    Widget gamblingScreen = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null
                            && (w.getText().equals("Harold rolls...") || w.getText().equals("Your roll...") || w.getText().equals("You win!") || w.getText().equals("You lose!")));

                    if (gamblingScreen != null && gamblingScreen.isVisible()) {
                        //RS2Widget rollDice = getWidgets().getWidgetContainingText(99, "Roll Dice!");
                        //RS2Widget continueGambling = getWidgets().getWidgetContainingText(99, "Continue...");
                        Widget rollDice = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Roll Dice!"));
                        Widget continueGambling = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Continue..."));
                        if (rollDice != null && rollDice.isVisible()) {
                            if (helper.getInteractEvent(rollDice, "Ok").executed())
                                sleepUntil(10000, () -> getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("You win!")) != null);
                        } else if (continueGambling != null && continueGambling.isVisible()) {
                            if (helper.getInteractEvent(continueGambling, "Ok").executed())
                                sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                        }
                    } else {
                        if (helper.talkTo("Harold"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 55:
                    //won the gambling & have IOU note
                    if (getBot().getInventory().contains("Iou") && helper.interactInventory("Iou", "Read")) {
                        sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                    }
                    break;
                case 60:
                    //have combination -> grab stone balls -> do combination
                    if (helper.inArea(FOURTH_STAGE_AREA)) {
                        if (shouldGrabBalls) {
                            if (grabStart == 3566 || getBot().getInventory().isFull())
                                shouldGrabBalls = false;
                            else {
                                info("Grabbing stone balls");
                                int last = getBot().getInventory().getEmptySlots();
                                if (helper.interactGroundItem(o -> o != null &&
                                        o.getName().equals("Stone ball") && o.getTile().getY() == grabStart, "Take")) {
                                    sleepUntil(10000, () -> getBot().getInventory().getEmptySlots() < last);
                                    grabStart++;
                                }
                            }
                        } else {
                            info("handling combination");
                            if (getBot().getInventory().isSelected(i -> i.getId() == COMBINATION_ID[index])) {
                                info("ball selected.");
                                int last = getBot().getInventory().getEmptySlots();
                                if (helper.interactObject(o -> o != null && o.getName().equals("Stone Mechanism") && o.getTile().equals(COMBINATION_TILES[index]), "Use")) {
                                    sleepUntil(10000, () -> getBot().getInventory().getEmptySlots() > last);
                                    index++;
                                    sleep(600);
                                }
                            } else {
                                info("select: ball");
                                if (new InventoryInteractEvent(getBot(), COMBINATION_ID[index], "Use").executed())
                                    sleepUntil(5000, () -> getBot().getInventory().isSelected(i -> i.getId() == COMBINATION_ID[index]));
                            }
                        }
                    } else {
                        helper.getWeb(new Tile(2896, 3563, 0)).execute();
                    }
                    break;
                case 70:
                    //talk to guard archer, saba, tenzing, dunstan, denulth, tenzing, denulth. in order.. | code is in reverse order depending on flags or items obtained.
                    if (getBot().getInventory().contains("Secret way map")) {
                        if (helper.inArea(SEVENTH_STAGE_AREA)) {
                            if (helper.interactObject(o -> o != null && o.getName().equals("Door") && o.getTile().getX() == 2820, "Open"))
                                sleepUntil(7000, () -> helper.inArea(NINTH_STAGE_AREA));
                        } else if (helper.inArea(NINTH_STAGE_AREA)) {
                            if (helper.interactObject("Stile", "Climb-over"))
                                sleepUntil(7000, () -> !helper.inArea(NINTH_STAGE_AREA));
                        } else {
                            if (shouldFindPath) {
                                if (helper.getWeb(new Tile(2865, 3609, 0)).executed())
                                    shouldFindPath = false;
                            } else {
                                if (helper.inArea(START_AREA)) {
                                    if (helper.talkTo("Denulth"))
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                } else {
                                    helper.getWeb(START_AREA).execute();
                                }
                            }
                        }
                    } else if (getBot().getInventory().contains("Certificate")) {
                        if (helper.inArea(EIGHTH_STAGE_AREA)) {
                            if (helper.talkTo("Dunstan")) {
                                sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                            }
                        } else {
                            helper.getWeb(EIGHTH_STAGE_AREA).execute();
                        }
                    } else if (getBot().getInventory().contains("Climbing boots")) {
                        if (shouldTalkToDunstan) {
                            if (helper.inArea(EIGHTH_STAGE_AREA)) {
                                if (helper.talkTo("Dunstan")) {
                                    sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                                    shouldTalkToDunstan = false;
                                }
                            } else {
                                helper.getWeb(EIGHTH_STAGE_AREA).execute();
                            }
                        } else {
                            if (helper.inArea(START_AREA)) {
                                if (helper.talkTo("Denulth"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            } else {
                                helper.getWeb(START_AREA).execute();
                            }
                        }
                    } else if (shouldTalkToGuard) {
                        if (helper.inArea(FIFTH_STAGE_AREA)) {
                            if (helper.talkTo("Archer")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                shouldTalkToGuard = false;
                            }
                        } else {
                            helper.getWeb(FIFTH_STAGE_AREA).execute();
                        }
                    } else if (shouldTalkToSaba) {
                        if (helper.inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                            if (helper.talkTo("Saba")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                shouldTalkToSaba = false;
                            }
                        } else if (helper.inArea(SIXTH_STAGE_AREA)) {
                            if (helper.interactObject("Cave Entrance", "Enter"))
                                sleepUntil(7000, () -> helper.inArea(SIXTH_STAGE_PART_TWO_AREA));
                        } else {
                            helper.getWeb(SIXTH_STAGE_AREA).execute();
                        }
                    } else {
                        if (helper.inArea(SEVENTH_STAGE_AREA)) {
                            if (helper.talkTo("Tenzing")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else { //if we have spiked boots our logic should return back here
                            if (helper.inArea(SIXTH_STAGE_PART_TWO_AREA)) {
                                if (helper.interactObject("Cave Exit", "Exit"))
                                    sleepUntil(7000, () -> helper.inArea(SIXTH_STAGE_AREA));
                            } else {
                                helper.getWeb(SEVENTH_STAGE_AREA).execute();
                            }
                        }
                    }
                    break;
                case 80:
                    //remove quest node
                    this.setComplete();
                    break;
            }
        }
        sleep(600);
    }
}
