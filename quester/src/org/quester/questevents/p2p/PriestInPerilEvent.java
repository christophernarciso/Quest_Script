package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.GroundItem;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Food;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;
import java.util.function.Predicate;

public class PriestInPerilEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Sure.", "Roald sent me to check on Drezel.", "Yes", "Tell me anyway."
    };
    private final Tile[] MONUMENT_POSITIONS = {
            new Tile(3423, 9884, 0), new Tile(3427, 9885, 0),
            new Tile(3428, 9890, 0), new Tile(3427, 9894, 0),
            new Tile(3423, 9895, 0), new Tile(3418, 9894, 0),
            new Tile(3416, 9890, 0)
    };
    private final Area START_AREA = new Area(
            new int[][]{
                    {3219, 3473},
                    {3220, 3474},
                    {3220, 3479},
                    {3225, 3479},
                    {3225, 3474},
                    {3226, 3473},
                    {3226, 3471},
                    {3224, 3469},
                    {3221, 3469},
                    {3219, 3470}
            }
    );
    private final Area TEMPLE_FRONT_DOOR_AREA = new Area(3407, 3490, 3408, 3487);
    private final Area TEMPLE_DUNGEON_ENTRANCE_AREA = new Area(3403, 3507, 3407, 3504);
    private final Area TEMPLE_GROUND_FLOOR_AREA = new Area(3409, 3494, 3418, 3483);
    private final Area TEMPLE_MIDDLE_FLOOR_AREA = new Area(3409, 3494, 3418, 3483, 1);
    private final Area TEMPLE_TOP_FLOOR_AREA = new Area(3409, 3494, 3418, 3483, 2);
    private final Area TEMPLE_DUNGEON_MONUMENT_AREA = new Area(3417, 9896, 3429, 9884);
    private final Area TEMPLE_TOP_CELL_AREA = new Area(3416, 3494, 3419, 3482, 2);
    private final Area TEMPLE_DREZEL_AREA = new Area(3436, 9903, 3444, 9886);
    private boolean returnToDrezel;

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public PriestInPerilEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Pure essence", 50);
        itemReq.put("Bucket", 1);
        itemReq.put("Lobster", 7);
        itemReq.put("Adamant scimitar", 1);
        itemReq.put("Amulet of strength", 1);
        itemReq.put("Varrock teleport", 2);

        info("Started: " + Quest.PRIEST_IN_PERIL.name());
        setGrabbedItems(false);
        returnToDrezel = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(302);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                // We can't withdraw > 28
                itemReq.remove("Pure essence");

                info("Bank event execute");
                // Load bank event and execute withdraw
                if (getBankEvent(itemReq).executed()) {
                    if (closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Adamant scimitar", "Amulet of strength"};
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

        info("Quest stage: 302 = " + result);
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
        } else if (getBot().getInventory().contains("Lobster") && ourHealthPercent() <= 50) {
            new HealEvent(getBot(), getBot().getClient().getSkillBoosted(Skill.HITPOINTS), Food.LOBSTER).executed();
        } else {
            switch (result) {
                case 3:
                    // Just return to King Roald.
                case 0:
                    // Start
                    if (inArea(START_AREA)) {
                        info("Talk to King Roald");
                        if (talkTo("King Roald"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walk to King Roald");
                        getWeb(START_AREA).execute();
                    }
                    break;

                case 1:
                    // Knock at temple
                    if (inArea(TEMPLE_FRONT_DOOR_AREA)) {
                        info("Knock on temple door");
                        if (interactObject("Large door", "Knock-at"))
                            sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walk to temple");
                        getWeb(TEMPLE_FRONT_DOOR_AREA).execute();
                    }
                    break;
                case 2:
                    // Told to kill a guardian dog in dungeon
                    NPC templeGuardian = getBot().getNPCs().first("Temple guardian");
                    if (templeGuardian != null) {
                        if (!myPlayer().isInteracting()) {
                            info("Fighting temple guardian");
                            if (getInteractEvent(templeGuardian, "Attack").executed())
                                sleepUntil(3000, () -> myPlayer().isInteracting());
                        }
                    } else if (inArea(TEMPLE_DUNGEON_ENTRANCE_AREA)) {
                        GameObject trapdoor = getBot().getGameObjects().closest("Trapdoor");
                        if (trapdoor != null) {
                            if (trapdoor.hasAction("Open") && interactObject("Trapdoor", "Open")) {
                                info("Open trapdoor");
                                sleepGameCycle();
                            } else if (trapdoor.hasAction("Climb-down") && interactObject("Trapdoor", "Climb-down")) {
                                info("Entering dungeon");
                                sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                            }
                        }
                    } else {
                        info("Walking to entrance");
                        getWeb(new Tile(3405, 3506, 0)).execute();
                    }
                    break;
                case 4:
                    if (inArea(TEMPLE_GROUND_FLOOR_AREA) || inArea(TEMPLE_TOP_FLOOR_AREA)) {
                        if (!getBot().getInventory().contains("Golden key")) {
                            sleep(3000);
                            GroundItem key = getBot().getGroundItems().first("Golden key");
                            NPC monk = getBot().getNPCs().first(m -> m != null && m.getCombatLevel() == 30
                                    && m.hasAction("Attack") && m.hasName("Monk of Zamorak"));
                            if (key != null) {
                                info("Grabbing golden key");
                                if (getInteractEvent(key, "Take").executed())
                                    sleepUntil(3000, () -> getBot().getInventory().contains("Golden key"));
                            } else if (monk != null) {
                                if (!myPlayer().isInteracting()) {
                                    info("Fighting monk for golden key");
                                    if (getInteractEvent(monk, "Attack").executed())
                                        sleepUntil(3000, () -> myPlayer().isInteracting());
                                }
                            }
                        } else if (inArea(TEMPLE_TOP_FLOOR_AREA)) {
                            info("Talk to drezel through cell door");
                            if (interactObject("Cell door", "Talk-through"))
                                sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                        } else if (inArea(TEMPLE_MIDDLE_FLOOR_AREA)) {
                            info("Climbing ladder");
                            if (interactObject("Ladder", "Climb-up"))
                                sleepUntil(4000, () -> inArea(TEMPLE_TOP_FLOOR_AREA));
                        } else {
                            info("Going to top floor.");
                            getWeb(TEMPLE_TOP_FLOOR_AREA).execute();
                        }
                    } else if (inArea(TEMPLE_FRONT_DOOR_AREA)) {
                        info("Knock on temple door");
                        if (interactObject("Large door", "Knock-at"))
                            sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walk to temple");
                        getWeb(TEMPLE_FRONT_DOOR_AREA).execute();
                    }
                    break;
                case 5:
                    if (returnToDrezel) {
                        if (inArea(TEMPLE_TOP_FLOOR_AREA)) {
                            info("Unlocking door.");
                            if (useOnObject("Cell door", "Iron key"))
                                sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                        } else if (inArea(TEMPLE_MIDDLE_FLOOR_AREA)) {
                            info("Climbing ladder");
                            if (interactObject("Ladder", "Climb-up"))
                                sleepUntil(4000, () -> inArea(TEMPLE_TOP_FLOOR_AREA));
                        } else {
                            info("Going to top floor.");
                            getWeb(TEMPLE_TOP_FLOOR_AREA).execute();
                        }
                    } else if (myPosition().getY() < 9000) {
                        if (inArea(TEMPLE_DUNGEON_ENTRANCE_AREA)) {
                            GameObject trapdoor = getBot().getGameObjects().closest("Trapdoor");
                            if (trapdoor != null) {
                                if (trapdoor.hasAction("Open") && interactObject("Trapdoor", "Open")) {
                                    info("Open trapdoor");
                                    sleepGameCycle();
                                } else if (trapdoor.hasAction("Climb-down") && interactObject("Trapdoor", "Climb-down")) {
                                    info("Entering dungeon");
                                    sleepUntil(4000, () -> !inArea(TEMPLE_DUNGEON_ENTRANCE_AREA));
                                }
                            }
                        } else {
                            info("Walking to entrance");
                            getWeb(new Tile(3405, 3506, 0)).execute();
                        }
                    } else if (inArea(TEMPLE_DUNGEON_MONUMENT_AREA)) {
                        if (!getBot().getInventory().contains("Iron key")) {
                            info("Finding iron key primary task");
                            int index = 0;
                            while (index < MONUMENT_POSITIONS.length) {
                                if (getBot().getInventory().contains("Iron key")) break;

                                int finalIndex = index;
                                Predicate<GameObject> gameObjectPredicate = o -> o != null && o.getTile().equals(MONUMENT_POSITIONS[finalIndex]);
                                if (MONUMENT_POSITIONS[finalIndex].distance(myPosition()) > 2)
                                    getWeb(MONUMENT_POSITIONS[finalIndex].translate(0, -1)).execute();

                                if (useOnObject(gameObjectPredicate, "Golden key")) {
                                    info("Used: key on monument at " + MONUMENT_POSITIONS[index].toString());
                                    sleep(1000);
                                    index++;
                                }
                            }
                        } else if (!getBot().getInventory().contains("Murky water")) {
                            if (useOnObject("Well", "Bucket"))
                                sleepUntil(3000, () -> getBot().getInventory().contains("Murky water"));
                        } else {
                            info("Have required items.");
                            returnToDrezel = true;
                        }
                    } else {
                        info("Walking to monuments");
                        getWeb(TEMPLE_DUNGEON_MONUMENT_AREA).execute();
                    }
                    break;
                case 7:
                    // Just return to Drezel
                case 6:
                    if (inArea(TEMPLE_TOP_FLOOR_AREA)) {
                        if (getBot().getInventory().contains("Blessed water")) {
                            if (inArea(TEMPLE_TOP_CELL_AREA)) {
                                info("Leaving cell area");
                                if (interactObject("Cell door", "Open"))
                                    sleepUntil(4000, () -> !inArea(TEMPLE_TOP_CELL_AREA));
                            } else if (useOnObject("Coffin", "Blessed water")) {
                                info("Using bucket on coffin");
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else if (inArea(TEMPLE_TOP_CELL_AREA)) {
                            info("Talk to Drezel");
                            if (talkTo("Drezel"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else if (interactObject("Cell door", "Open")) {
                            info("Opening cell door");
                            sleepUntil(4000, () -> inArea(TEMPLE_TOP_CELL_AREA));
                        }
                    }
                    break;
                case 8:
                    if (inArea(TEMPLE_TOP_CELL_AREA)) {
                        if (interactObject("Cell door", "Open"))
                            sleepUntil(4000, () -> !inArea(TEMPLE_TOP_CELL_AREA));
                    } else if (inArea(TEMPLE_DREZEL_AREA)) {
                        info("Talk to Drezel");
                        if (talkTo("Drezel"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Drezel");
                        getWeb(TEMPLE_DREZEL_AREA).execute();
                    }
                    break;
                case 38:
                    // Keep getting essence
                case 10:
                    if (getBot().getInventory().contains("Pure essence")) {
                        if (inArea(TEMPLE_DREZEL_AREA)) {
                            info("Talk to Drezel");
                            if (talkTo("Drezel"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else {
                            info("Walking to Drezel");
                            getWeb(TEMPLE_DREZEL_AREA).execute();
                        }
                    } else {
                        info("Grabbing more essence.");
                        new BankEvent(getBot()).addReq(1, 28, "Pure essence").execute();
                    }
                    break;
                case 61:
                case 60:
                    sleepGameCycle();
                    info("Talk to Drezel");
                    if (talkTo("Drezel"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());

                    // End
                    info("Finished: " + Quest.PRIEST_IN_PERIL.name());
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
