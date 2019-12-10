package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.enums.Bank;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.events.containers.DepositEvent;
import org.quantumbot.events.containers.WithdrawEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;
import java.util.function.Predicate;

public class WaterfallEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "How can I help?", "Where else is worth visiting around here?", "Thanks then, goodbye."
    };
    private final Tile[] PILLAR_LOCATIONS = {
            new Tile(2562, 9910, 0), new Tile(2569, 9910, 0),
            new Tile(2569, 9912, 0), new Tile(2562, 9912, 0),
            new Tile(2562, 9914, 0), new Tile(2569, 9914, 0)
    };
    private final Area START_AREA = new Area(2518, 3503, 2523, 3492);
    private final Area RAFT_AREA = new Area(2507, 3497, 2512, 3491);
    private final Tile QUEST_START_TILE = new Tile(2520, 3495, 0);
    private final Area ROPE_ON_ROCK_AREA = new Area(2509, 3481, 2514, 3475);
    private final Area INTERACT_TREE_BEFORE_ENTRANCE_AREA = new Area(2510, 3469, 2514, 3465);
    private final Area WATERFALL_DUNGEON_ENTRANCE = new Area(2508, 3462, 2514, 3463);
    private final Area HOUSE_HADLEY_AREA = new Area(
            new int[][]{
                    {2516, 3435},
                    {2516, 3431},
                    {2511, 3431},
                    {2511, 3427},
                    {2516, 3427},
                    {2517, 3427},
                    {2517, 3424},
                    {2520, 3424},
                    {2520, 3423},
                    {2524, 3423},
                    {2524, 3424},
                    {2524, 3427},
                    {2521, 3427},
                    {2521, 3435}
            }
    );
    private final Area HOUSE_HADLEY_BOOK_ROOM = new Area(2516, 3431, 2520, 3423, 1);
    private final Area MAZE_DUNGEON_KEY_ROOM = new Area(2541, 9573, 2556, 9560);
    private final Area MAZE_DUNGEON_LOCKED_DOOR_ROOM = new Area(2512, 9575, 2518, 9573);
    private final Area MAZE_DUNGEON_PEBBLE_ROOM = new Area(2507, 9585, 2522, 9576);
    private final Area GLARIAL_TOMBSTONE_AREA = new Area(2554, 3448, 2560, 3441);
    private final Area GLARIAL_DUNGEON_AMULET_ROOM = new Area(2525, 9848, 2535, 9839);
    private final Area GLARIAL_DUNGEON_URN_ROOM = new Area(2537, 9820, 2549, 9808);
    private final Area WATERFALL_DUNGEON_KEY_ROOM = new Area(2582, 9888, 2595, 9877);
    private final Tile WATERFALL_DUNGEON_LOCKED_DOOR_TILE = new Tile(2568, 9893, 0);
    private final Area WATERFALL_DUNGEON_LOCKED_DOOR = new Area(2566, 9892, 2570, 9893);
    private final Area WATERFALL_DUNGEON_MIDDLE_ROOM = new Area(2564, 9901, 2570, 9894);
    private final Area WATERFALL_QUEST_END_ROOM = new Area(2559, 9918, 2572, 9902);
    private boolean talkedToHadley, gotAmulet, gotUrn;

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public WaterfallEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Ring of dueling(1~8)", 1);
        itemReq.put("Games necklace(1~8)", 1);
        itemReq.put("Earth rune", 6);
        itemReq.put("Water rune", 6);
        itemReq.put("Air rune", 6);
        itemReq.put("Varrock teleport", 3);
        itemReq.put("Rope", 1);
        itemReq.put("Lobster", 10);
        info("Started: " + Quest.WATERFALL_QUEST.name());
        setGrabbedItems(false);
        talkedToHadley = false;
        gotAmulet = gotUrn = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(65);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (getBankEvent(itemReq).executed()) {
                    sleep(1000);
                    if (openBank()) {
                        // Execute deposit equipment.
                        String[] deposits = {"Earth rune", "Water rune", "Air rune"};
                        for (String s : deposits) {
                            if (new DepositEvent(getBot(), s, Integer.MAX_VALUE).executed())
                                sleep(1200);
                        }

                        sleep(700);

                        // Make sure we deposit worn items.
                        new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible()
                                && w.hasAction("Deposit worn items")).executed();

                        // At this point we now have our items.
                        setGrabbedItems(true);
                        sleep(700);
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
                info("GE event execute");
                getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 65 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getCamera().isLocked()) {
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption()).executed())
                    sleep(1000);

                if (inArea(MAZE_DUNGEON_PEBBLE_ROOM) && !getBot().getInventory().contains("Glarial's pebble") && result == 3)
                    sleepUntil(6000, () -> getBot().getInventory().contains("Glarial's pebble"));
            } else if (getBot().getDialogues().isPendingOption()) {
                info("QUEST_DIALOGUE");
                new DialogueEvent(getBot(), QUEST_DIALOGUE).execute();
                sleep(1000);
            } else {
                info("No dialogue???");
            }
        } else if (getBot().getInventory().contains("Lobster") && getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 15) {
            new HealEvent(getBot()).executed();
        } else {
            switch (result) {
                case 0:
                    // Start
                    if (inArea(START_AREA)) {
                        info("In start area");
                        if (talkTo("Almera"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(QUEST_START_TILE).execute();
                    }
                    break;
                case 1:
                    // Talk to boy
                    GameObject closedGate = getBot().getGameObjects().closest(o -> o != null && o.hasName("Gate")
                            && o.hasAction("Open") && o.getTile().getX() == 2513 && o.getTile().getTile().getY() == 3494);
                    if (closedGate != null) {
                        info("Need to open gate near raft");
                        if (new InteractEvent(getBot(), closedGate, "Open").executed()) {
                            sleepUntil(3000, () -> !closedGate.exists());
                        }
                    } else if (interactObject("Log raft", "Board")) {
                        info("Boarding raft to lost boy");
                        sleepUntil(13000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 2:
                    // Interact barrel
                    if (inArea(ROPE_ON_ROCK_AREA)) {
                        info("Attempting to cross water");
                        if (useOnObject(o -> o != null
                                && o.hasName("Rock") && o.getTile().getY() == 3468, "Rope")) {
                            info("Going across the water: Use rope on rock");
                            sleepUntil(10000, () -> inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA));
                        }
                    } else if (inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA)) {
                        if (useOnObject("Dead Tree", "Rope")) {
                            info("Going across the water: Use rope on dead tree");
                            sleepUntil(10000, () -> inArea(WATERFALL_DUNGEON_ENTRANCE));
                        }
                    } else if (inArea(WATERFALL_DUNGEON_ENTRANCE)) {
                        if (interactObject("Barrel", "Get in")) {
                            info("Interact barrel: get in");
                            sleepUntil(15000, () -> myPosition().getY() == 3413);
                        }
                    } else if (!talkedToHadley && inArea(HOUSE_HADLEY_AREA)) {
                        if (talkTo("Hadley")) {
                            talkedToHadley = true;
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (inArea(HOUSE_HADLEY_AREA) || inArea(HOUSE_HADLEY_BOOK_ROOM)) {
                        if (inArea(HOUSE_HADLEY_BOOK_ROOM)) {
                            if (getBot().getInventory().contains("Book on baxtorian")) {
                                if (interactInventory("Book on baxtorian", "Read")) {
                                    sleep(2200);
                                    if (interactInventory("Lobster", "Eat"))
                                        sleep(1200);
                                }
                            } else if (interactObject(o -> o != null && o.hasName("Bookcase")
                                    && o.getTile().getY() == 3426, "Search")) {
                                sleepUntil(3000, () -> getBot().getInventory().contains("Book on baxtorian"));
                            }
                        } else {
                            getWeb(HOUSE_HADLEY_BOOK_ROOM).execute();
                        }
                    } else {
                        getWeb(HOUSE_HADLEY_AREA).execute();
                    }
                    break;
                case 3:
                    if (!getBot().getInventory().contains("Glarial's pebble")) {
                        if (!getBot().getInventory().contains("Key")) {
                            if (inArea(MAZE_DUNGEON_KEY_ROOM)) {
                                info("Grabbing key");
                                if (interactObject(o -> o != null && o.hasAction("Search")
                                        && o.hasName("Crate") && o.getTile().equals(new Tile(2548, 9565, 0)), "Search")) {
                                    sleepUntil(4000, () -> getBot().getInventory().contains("Key"));
                                }
                            } else {
                                info("Walking to key room");
                                getWeb(new Tile(2548, 9566, 0)).setDestinationAccuracy(0).execute();
                            }
                        } else {
                            if (inArea(MAZE_DUNGEON_PEBBLE_ROOM)) {
                                info("Grabbing pebble");
                                if (talkTo("Golrie")) {
                                    sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                                }
                            } else if (inArea(MAZE_DUNGEON_LOCKED_DOOR_ROOM)) {
                                info("Unlocking door");
                                if (useOnObject("Door", "Key")) {
                                    sleepUntil(3000, () -> inArea(MAZE_DUNGEON_PEBBLE_ROOM));
                                }
                            } else {
                                info("Walking to locked door with key.");
                                getWeb(new Tile(2515, 9575, 0)).execute();
                            }
                        }
                    } else if (inArea(GLARIAL_TOMBSTONE_AREA)) {
                        info("At the tomb");
                        if (useOnObject("Glarial's Tombstone", "Glarial's pebble"))
                            sleepUntil(10000, () -> !inArea(GLARIAL_TOMBSTONE_AREA));
                    } else {
                        info("Walking to glarials tomb");
                        getWeb(GLARIAL_TOMBSTONE_AREA).execute();
                    }
                    break;
                case 4:
                    if (!gotAmulet || !gotUrn) {
                        if (getBot().getInventory().contains("Glarial's urn")) {
                            gotUrn = true;
                            gotAmulet = true;
                            return;
                        }

                        if (myPosition().getY() < 9000 || inArea(MAZE_DUNGEON_PEBBLE_ROOM)) {
                            if (inArea(GLARIAL_TOMBSTONE_AREA)) {
                                info("At the tomb");
                                if (useOnObject("Glarial's Tombstone", "Glarial's pebble"))
                                    sleepUntil(10000, () -> !inArea(GLARIAL_TOMBSTONE_AREA));
                            } else {
                                info("Walking to glarials tomb");
                               getWeb(GLARIAL_TOMBSTONE_AREA).execute();
                            }
                        } else {
                            info("Inside dungeon");
                            if (!gotAmulet) {
                                if (inArea(GLARIAL_DUNGEON_AMULET_ROOM)) {
                                    info("Grabbing amulet");
                                    if (interactObject("Chest", "Open"))
                                        sleep(600);

                                    if (interactObject("Chest", "Search")) {
                                        gotAmulet = true;
                                        sleepUntil(3000, () -> getBot().getInventory().contains("Glarial's amulet") || getBot().getInventory().contains("Lobster") && getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 15);
                                    }
                                } else {
                                    info("Walking to glarials amulet room");
                                    new WebWalkEvent(getBot(), GLARIAL_DUNGEON_AMULET_ROOM)
                                            .setInterruptCondition(() -> getBot().getInventory().contains("Lobster") && getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 15)
                                            .executed();
                                }
                            } else if (!gotUrn) {
                                if (inArea(GLARIAL_DUNGEON_URN_ROOM)) {
                                    info("Grabbing urn");
                                    if (interactObject("Glarial's Tomb", "Search")) {
                                        gotUrn = true;
                                        sleepUntil(4000, () -> getBot().getInventory().contains("Glarial's urn") || getBot().getInventory().contains("Lobster") && getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 15);
                                    }
                                } else {
                                    info("Walking to glarials urn room");
                                    new WebWalkEvent(getBot(), GLARIAL_DUNGEON_URN_ROOM)
                                            .setInterruptCondition(() -> getBot().getInventory().contains("Lobster") && getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 15)
                                            .executed();
                                }
                            }
                        }
                    } else if (inArea(GLARIAL_DUNGEON_URN_ROOM) && gotUrn) {
                        info("Leaving dungeon.");
                        if (getBot().getInventory().contains("Varrock teleport")) {
                            if (interactInventory("Varrock teleport", "Break"))
                                sleep(7000);
                        }
//                        getWeb(Bank.BARBARIAN_ASSAULT_BANK.getArea()).execute();
                    } else if (!getBot().getInventory().containsAll("Earth rune", "Water rune", "Air rune")) {
                        info("Need to grab runes");
                        if (!getBot().getBank().isOpen())
                            new BankOpenEvent(getBot(), Bank.BARBARIAN_ASSAULT_BANK).executed();
                        else {
                            info("Withdraw: runes");
                            if (!getBot().getInventory().contains("Air rune"))
                                new WithdrawEvent(getBot(), "Air rune", 6).executed();

                            if (!getBot().getInventory().contains("Earth rune"))
                                new WithdrawEvent(getBot(), "Earth rune", 6).executed();

                            if (!getBot().getInventory().contains("Water rune"))
                                new WithdrawEvent(getBot(), "Water rune", 6).executed();
                        }
                    } else {
                        if (inArea(WATERFALL_DUNGEON_ENTRANCE)) {
                            if (interactObject("Door", "Open")) {
                                info("Entering dungeon.");
                                sleepUntil(10000, () -> !inArea(WATERFALL_DUNGEON_ENTRANCE));
                            }
                        } else if (inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA)) {
                            if (useOnObject("Dead Tree", "Rope")) {
                                info("Use rope on dead tree");
                                    sleepUntil(10000, () -> inArea(WATERFALL_DUNGEON_ENTRANCE));
                            }
                        } else if (inArea(ROPE_ON_ROCK_AREA)) {
                            if (useOnObject(o -> o != null && o.hasAction("Swim to")
                                    && o.hasName("Rock") && o.getTile().getY() == 3468, "Rope")) {
                                info("Going across the water: Use rope on rock");
                                sleepUntil(10000, () -> inArea(INTERACT_TREE_BEFORE_ENTRANCE_AREA));
                            }
                        } else if (inArea(RAFT_AREA)) {
                            closedGate = getBot().getGameObjects().closest(o -> o != null && o.hasName("Gate")
                                    && o.hasAction("Open") && o.getTile().getX() == 2513 && o.getTile().getTile().getY() == 3494);

                            if (closedGate != null) {
                                info("Need to open gate near raft");
                                if (new InteractEvent(getBot(), closedGate, "Open").executed()) {
                                    sleepUntil(3000, () -> !closedGate.exists());
                                }
                            } else if (interactObject("Log raft", "Board")) {
                                info("Boarding raft");
                                sleepUntil(13000, () -> inArea(ROPE_ON_ROCK_AREA));
                            }
                        } else {
                            info("Walking to raft.");
                            getWeb(RAFT_AREA).execute();
                        }
                    }
                    break;
                case 5:
                    // Crate, Search, 2589 9888, 0
                    if (!getBot().getInventory().contains("Key")) {
                        if (inArea(WATERFALL_DUNGEON_KEY_ROOM)) {
                            info("Grabbing key");
                            if (interactObject(c -> c != null && c.hasName("Crate")
                                    && c.getTile().equals(new Tile(2589, 9888, 0)), "Search")) {
                                sleepUntil(3000, () -> getBot().getInventory().contains("Key"));
                            }
                        } else {
                            getWeb(new Tile(2589, 9887, 0)).execute();
                        }
                    } else if (inArea(WATERFALL_DUNGEON_MIDDLE_ROOM)) {
                        info("Walking to end room.");
                        getWeb(new Tile(2566, 9901, 0)).execute();
                        sleep(1000);
                        if (interactObject("Door", "Open")) {
                            sleepUntil(3000, () -> !inArea(WATERFALL_DUNGEON_MIDDLE_ROOM));
                        }
                    } else if (inArea(WATERFALL_DUNGEON_LOCKED_DOOR)) {
                        info("Opening locked door.");
                        if (useOnObject("Door", "Key")) {
                            sleepUntil(3000, () -> inArea(WATERFALL_DUNGEON_MIDDLE_ROOM));
                        }
                    } else {
                        info("Walking to locked door.");
                        getWeb(WATERFALL_DUNGEON_LOCKED_DOOR_TILE).execute();
                    }
                    break;
                case 6:
                    if (getBot().getInventory().containsAll("Earth rune", "Water rune", "Air rune")) {
                        int index = 0;
                        while (index < PILLAR_LOCATIONS.length) {
                            int finalIndex = index;
                            Predicate<GameObject> gameObjectPredicate = o -> o != null && o.getTile().equals(PILLAR_LOCATIONS[finalIndex]);
                            if (PILLAR_LOCATIONS[finalIndex].distance(myPosition()) > 2)
                                getWeb(PILLAR_LOCATIONS[finalIndex].translate(0, -1)).execute();

                            info("Using piller at: " + PILLAR_LOCATIONS[index].toString());
                            if (useOnObject(gameObjectPredicate, "Air rune")) {
                                info("Used: Air rune x1");
                                sleep(1000);
                            }
                            if (useOnObject(gameObjectPredicate, "Earth rune")) {
                                info("Used: Earth rune x1");
                                sleep(1000);
                            }
                            if (useOnObject(gameObjectPredicate, "Water rune")) {
                                info("Used: Water rune x1");
                                sleep(1000);
                                index++;
                            }
                        }
                    } else if (getBot().getInventory().contains("Glarial's amulet")) {
                        if (useOnObject("Statue of Glarial", "Glarial's amulet")) {
                            sleepUntil(12000, () -> !getBot().getInventory().contains("Glarial's amulet"));
                        }
                    }
                    break;
                case 8:
                    if (useOnObject("Chalice", "Glarial's urn")) {
                        sleepUntil(7000, () -> getBot().getInventory().contains("Gold bar"));
                    }
                    break;
                case 10:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.WATERFALL_QUEST.name());
                    setComplete();
                    break;
            }
        }

        sleep(1000);
    }

    @Override
    public void onFinish() {
        setGrabbedItems(false);
    }
}
