import org.quantumbot.api.Script;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.GroundItem;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.CameraEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.EnterAmountEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.interactions.*;
import org.quantumbot.interfaces.Logger;

import java.util.function.Predicate;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Questing")
public class QuestMain extends Script implements Logger{

    private final int QUEST_CONFIG = 314;
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
    private final Area CASTLE_WARS_AREA = new Area(2435, 3098, 2449, 3079);
    
    @Override
    public void onStart() {

    }

    @Override
    public void onLoop() throws InterruptedException {
        int result = getBot().getClient().getVarp(QUEST_CONFIG);
        info("Quest stage: " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getDialogues().isPendingContinuation() || getBot().getDialogues().isPendingOption()){
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()){
                info("Continue");
                Widget continueGambling = getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("Continue..."));
                if (continueGambling != null && continueGambling.isVisible()) {
                    new InteractEvent(getBot(), continueGambling, "Ok").execute();
                    sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                } else if (new DialogueEvent(getBot()).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()){
                info("Option");
                if (result == 50){
                    info("Selecting result 50 dialogue");
                    if (getBot().getInventory().contains("Premade blurb' sp."))
                        new DialogueEvent(getBot(), "Can I buy you a drink?").execute();
                    else
                        new DialogueEvent(getBot(), "Would you like to gamble?").execute();
                } else if (result == 70 && SIXTH_STAGE_PART_TWO_AREA.contains(myTile())) {
                    info("Selecting result 70 dialogue");
                    new DialogueEvent(getBot(), "Do you know of another way up Death Plateau?").execute();
                } else {
                    info("QUEST_DIALOGUE");
                    new DialogueEvent(getBot(), QUEST_DIALOGUE_DEATH_PLAT).execute();
                }
                sleep(1000);
            } else if (new EnterAmountEvent(getBot(), 100).executed())
                info("Successfully entered amount.");
            else info("No dialogue???");
        } else {
            switch (result) {
                case 0:
                    //start
                    if (START_AREA.contains(myTile())) {
                        if (interactNPC("Denulth", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        walk(START_AREA);
                    }
                    break;
                case 10:
                    //after accepting quest & now find eohric to talk about the guard
                    if (SECOND_STAGE_AREA.contains(myTile())) {
                        if (interactNPC("Eohric", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        walk(SECOND_STAGE_AREA);
                    }
                    break;
                case 20:
                    //head to the guard and ask for combination
                    sleep(1000);
                    if (THIRD_STAGE_PART_TWO_AREA.contains(myTile())) {
                        if (interactNPC("Harold", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (THIRD_STAGE_AREA.contains(myTile())) {
                        if (interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        walk(THIRD_STAGE_AREA);
                    }
                    break;
                case 30:
                    //head back to eohric after guard refuses to talk
                    if (SECOND_STAGE_AREA.contains(myTile())) {
                        if (interactNPC("Eohric", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        walk(SECOND_STAGE_AREA);
                    }
                    break;
                case 40:
                    //head back to the guard after talking to eohric
                    sleep(1000);
                    if (THIRD_STAGE_PART_TWO_AREA.contains(myTile())) {
                        if (interactNPC("Harold", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (THIRD_STAGE_AREA.contains(myTile())) {
                        if (interactObject(o -> o != null && o.hasAction("Open") && o.getTile().getX() == 2906, "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        walk(THIRD_STAGE_AREA);
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
                            new InteractEvent(getBot(), rollDice, "Ok").execute();
                            sleepUntil(10000, () -> getBot().getWidgets().first(w -> w != null && w.isVisible() && w.getText() != null && w.getText().equals("You win!")) != null);
                        } else if (continueGambling != null && continueGambling.isVisible()) {
                            new InteractEvent(getBot(), continueGambling, "Ok").execute();
                            sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                        }
                    } else {
                        if (interactNPC("Harold", "Talk-to"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 55:
                    //won the gambling & have IOU note
                    if (getBot().getInventory().contains("Iou") && new InventoryInteractEvent(getBot(), "Iou", "Read").executed()) {
                        sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                    }
                    break;
                case 60:
                    //have combination -> grab stone balls -> do combination
                    if (FOURTH_STAGE_AREA.contains(myTile())) {
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
                            if (getBot().getInventory().isSelected(i -> i.getId() == COMBINATION_ID[index])) {
                                info("ball selected.");
                                int last = getBot().getInventory().getEmptySlots();
                                if (interactObject(o -> o != null && o.getName().equals("Stone Mechanism") && o.getTile().equals(COMBINATION_TILES[index]), "Use")) {
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
                        walk(new Tile(2896, 3563, 0));
                    }
                    break;
                case 70:
                    //talk to guard archer, saba, tenzing, dunstan, denulth, tenzing, denulth. in order.. | code is in reverse order depending on flags or items obtained.
                    if (getBot().getInventory().contains("Secret way map")) {
                        if (SEVENTH_STAGE_AREA.contains(myTile())) {
                            if (interactObject(o -> o != null && o.getName().equals("Door") && o.getTile().getX() == 2820, "Open"))
                                sleepUntil(7000, () -> NINTH_STAGE_AREA.contains(myTile()));
                        } else if (NINTH_STAGE_AREA.contains(myTile())) {
                            if (interactObject("Stile", "Climb-over"))
                                sleepUntil( 7000, () -> !NINTH_STAGE_AREA.contains(myTile()));
                        } else {
                            if (shouldFindPath) {
                                if (walk(new Tile(2865, 3609, 0)))
                                    shouldFindPath = false;
                            } else {
                                if (START_AREA.contains(myTile())) {
                                    if (interactNPC("Denulth", "Talk-to"))
                                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                } else {
                                    walk(START_AREA);
                                }
                            }
                        }
                    } else if (getBot().getInventory().contains("Certificate")) {
                        if (EIGHTH_STAGE_AREA.contains(myTile())) {
                            if (interactNPC("Dunstan", "Talk-to")) {
                                sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                            }
                        } else {
                            walk(EIGHTH_STAGE_AREA);
                        }
                    } else if (getBot().getInventory().contains("Climbing boots")) {
                        if (shouldTalkToDunstan) {
                            if (EIGHTH_STAGE_AREA.contains(myTile())) {
                                if (interactNPC("Dunstan", "Talk-to")) {
                                    sleepUntil(5000, () -> getBot().getDialogues().isPendingContinuation());
                                    shouldTalkToDunstan = false;
                                }
                            } else {
                                walk(EIGHTH_STAGE_AREA);
                            }
                        } else {
                            if (START_AREA.contains(myTile())) {
                                if (interactNPC("Denulth", "Talk-to"))
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            } else {
                                walk(START_AREA);
                            }
                        }
                    } else if (shouldTalkToGuard) {
                        if (FIFTH_STAGE_AREA.contains(myTile())) {
                            if (interactNPC("Archer", "Talk-to")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                shouldTalkToGuard = false;
                            }
                        } else {
                            walk(FIFTH_STAGE_AREA);
                        }
                    } else if (shouldTalkToSaba) {
                        if (SIXTH_STAGE_PART_TWO_AREA.contains(myTile())) {
                            if (interactNPC("Saba", "Talk-to")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                shouldTalkToSaba = false;
                            }
                        } else if (SIXTH_STAGE_AREA.contains(myTile())) {
                            if (interactObject("Cave Entrance", "Enter"))
                                sleepUntil(7000, () -> SIXTH_STAGE_PART_TWO_AREA.contains(myTile()));
                        } else {
                            walk(SIXTH_STAGE_AREA);
                        }
                    } else {
                        if (SEVENTH_STAGE_AREA.contains(myTile())) {
                            if (interactNPC("Tenzing", "Talk-to")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else { //if we have spiked boots our logic should return back here
                            if (SIXTH_STAGE_PART_TWO_AREA.contains(myTile())) {
                                if (interactObject("Cave Exit", "Exit"))
                                    sleepUntil( 7000, () -> SIXTH_STAGE_AREA.contains(myTile()));
                            } else {
                                walk(SEVENTH_STAGE_AREA);
                            }
                        }
                    }
                    break;
                case 80:
                    //remove quest node
                    break;
            }
        }
        sleep(400);
    }

    @Override
    public void onExit() {

    }
    
    private boolean walk(Area area) throws InterruptedException {
        return new WebWalkEvent(getBot(), area).setInterruptCondition(() -> getBot().getDialogues().inDialogue()).executed();
    }

    private boolean walk(Tile area) throws InterruptedException {
        return new WebWalkEvent(getBot(), area).executed();
    }

    private Tile myTile(){
        return getBot().getPlayers().getLocal().getTile();
    }

    private boolean interactNPC(String name, String action) throws InterruptedException {
        return new NPCInteractEvent(getBot(), name, action).executed();
    }

    private boolean interactObject(String name, String action) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), name, action).executed();
    }
    
    private boolean interactObject(Predicate<GameObject> filter, String action) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), filter, action).executed();
    }

    private boolean interactGroundItem(Predicate<GroundItem> filter, String action) throws InterruptedException {
        return new GroundItemInteractEvent(getBot(), filter, action).executed();
    }

}
