package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.interactions.InteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.QuestContext;

import java.util.Arrays;
import java.util.HashMap;

public class WitchHouseEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "What's the matter?", "Ok, I'll see what I can do."
    };
    private final String[] BOSSES = {
            "Witch's experiment",
            "Witch's experiment (second form)",
            "Witch's experiment (third form)",
            "Witch's experiment (fourth form)"
    };
    private final Area START_AREA = new Area(2923, 3458, 2935, 3449);
    private final Area HOUSE_FRONT_DOOR_AREA = new Area(2897, 3476, 2900, 3468);
    private final Area HOUSE_MAIN_ROOM_AREA = new Area(2901, 3474, 2907, 3468);
    private final Area HOUSE_BASEMENT_ROOM_1 = new Area(2903, 9877, 2908, 9869);
    private final Area HOUSE_BASEMENT_ROOM_2_AREA = new Area(2898, 9878, 2902, 9869);
    private final Area HOUSE_TRAP_UNLOCK_ROOM_AREA = new Area(2901, 3467, 2903, 3466);
    private final Area TRAIL_START_AREA = new Area(2900, 3465, 2902, 3459);
    private final Area FOUNTAIN_AREA = new Area(2908, 3476, 2913, 3465);
    private final Area BOSS_FRONT_DOOR_AREA = new Area(2933, 3467, 2933, 3459);
    private final Area BOSS_ROOM_AREA = new Area(2934, 3467, 2937, 3459);
    private final Area WITCH_TRAIL_AREA = new Area(2900, 3465, 2933, 3459);
    private boolean grabbedMagnet;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public WitchHouseEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Games necklace(1~8)", 1);
        itemReq.put("Mind rune", 500);
        itemReq.put("Air rune", 1000);
        itemReq.put("Cheese", 3);
        itemReq.put("Lobster", 13);
        if (getBot().getClient().getSkillReal(Skill.MAGIC) >= 13)
            itemReq.put("Staff of fire", 1);
        else
            itemReq.put("Staff of earth", 1);
        itemReq.put("Leather gloves", 1);
        itemReq.put("Amulet of magic", 1);
        info("Started: " + Quest.WITCHS_HOUSE.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(226);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (getBankEvent(itemReq).executed()) {
                    if (closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Amulet of magic", "Leather gloves", "Staff of earth", "Staff of fire"};
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
                info("GE event execute");
                getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 226 = " + result);
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
                        info("In start area");
                        if (talkTo("Boy"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (inArea(HOUSE_MAIN_ROOM_AREA) || inArea(HOUSE_BASEMENT_ROOM_2_AREA)) {
                        info("In house main room area");
                        if (!grabbedMagnet) {
                            if (inArea(HOUSE_BASEMENT_ROOM_2_AREA)) {
                                GameObject cupboard = getBot().getGameObjects().closest("Cupboard");
                                if (cupboard == null) return;
                                info("In house basement area");
                                if (getBot().getInventory().isFull()) {
                                    info("Making inventory space for item: Magnet");
                                    if (interactInventory("Trout", "Eat")) {
                                        sleepUntil(2000, () -> !getBot().getInventory().isFull());
                                    }
                                } else if (getBot().getInventory().contains("Magnet")) {
                                    info("Have magnet");
                                    grabbedMagnet = true;
                                } else if (cupboard.hasAction("Search") && interactObject("Cupboard", "Search")) {
                                    info("Searching cupboard");
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                } else if (cupboard.hasAction("Open") && interactObject("Cupboard", "Open")) {
                                    info("Opening cupboard");
                                    sleepGameCycle();
                                }
                            } else {
                                getWeb(HOUSE_BASEMENT_ROOM_2_AREA).execute();
                            }
                        }
                    } else if (inArea(HOUSE_FRONT_DOOR_AREA)) {
                        info("In front door of house area");
                        if (getBot().getInventory().isFull()) {
                            info("Making inventory space for item: Door key");
                            if (interactInventory("Trout", "Eat")) {
                                sleepUntil(2000, () -> !getBot().getInventory().isFull());
                            }
                        } else if (getBot().getInventory().contains("Door key")) {
                            if (interactObject("Door", "Open")) {
                                sleepUntil(5000, () -> inArea(HOUSE_MAIN_ROOM_AREA));
                            }
                        } else if (interactObject(o -> o.hasName("Potted plant") && o.hasAction("Look-under")
                                , "Look-under")) {
                            sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        getWeb(HOUSE_FRONT_DOOR_AREA).execute();
                    }
                    break;
                case 2:
                    if (inArea(HOUSE_TRAP_UNLOCK_ROOM_AREA)) {
                        info("In mouse trap room area");
                        NPC mouse = getBot().getNPCs().closest("Mouse");
                        if (mouse != null) {
                            if (getBot().getInventory().isSelected(i -> i != null && i.hasName("Magnet"))) {
                                if (new InteractEvent(getBot(), mouse, "Use").executed()) {
                                    info("Used magnet on mouse");
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                }
                            } else if (interactInventory("Magnet", "Use")) {
                                info("Selected magnet");
                                sleepUntil(3000, () -> getBot().getInventory().isSelected(i -> i != null && i.hasName("Magnet")));
                            }
                        } else if (interactInventory("Cheese", "Drop")) {
                            info("Dropped cheese to see mouse");
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        getWeb(HOUSE_TRAP_UNLOCK_ROOM_AREA).execute();
                    }
                    break;
                case 3:
                    if (inArea(BOSS_ROOM_AREA)) {
                        info("In boss area");
                        if (!isAutocasting()) {
                            info("Need to autocast spell");
                            if (autocastSpell(getBot().getClient().getSkillReal(Skill.MAGIC) >= 13 ? StandardSpellbook.FIRE_STRIKE : StandardSpellbook.EARTH_STRIKE, false)) {
                                sleepUntil(3000, () -> isAutocasting());
                            }
                        } else {
                            // Boss combat
                            if (getBot().getClient().getSkillBoosted(Skill.HITPOINTS) <= 7 || ourHealthPercent() <= 50) {
                                new HealEvent(getBot()).executed();
                            } else if (myPlayer().getInteracting() == null) {
                                NPC experiment = getBot().getNPCs().closest(n -> n != null && n.isAttackable() && Arrays.asList(BOSSES).contains(n.getName()));
                                if (experiment != null) {
                                    int level = experiment.getCombatLevel();
                                    if (level < 30) {
                                        if (atExactPosition(new Tile(2937, 3463, 0))) {
                                            if (new InteractEvent(getBot(), experiment, "Attack").executed()) {
                                                info("Attacking");
                                                sleepUntil(3000, () -> myPlayer().isInteracting(experiment));
                                            }
                                        } else {
                                            walkHere(new Tile(2937, 3463, 0));
                                        }
                                    } else {
                                        if (atExactPosition(new Tile(2936, 3459, 0))) {
                                            if (new InteractEvent(getBot(), experiment, "Attack").executed()) {
                                                info("Attacking");
                                                sleepUntil(3000, () -> myPlayer().isInteracting(experiment));
                                            }
                                        } else {
                                            walkHere(new Tile(2936, 3459, 0));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (inArea(FOUNTAIN_AREA)) {
                        info("In fountain area");
                        if (getBot().getInventory().contains("Key")) {
                            walkHere(new Tile(2914, 3466, 0));
                        } else if (interactObject("Fountain", "Check")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("In witch compound");
                        NPC witch = getBot().getNPCs().closest("Nora T. Hagg");
                        int witchX = 0;
                        boolean left = false, right = false;

                        if (witch != null) {
                            witchX = witch.getTile().getX();
                            left = witch.getOrientation() == 512;
                            right = witch.getOrientation() == 1536;
                        }

                        int myX = myPosition().getX();
                        int myY = myPosition().getY();

                        if (inArea(HOUSE_TRAP_UNLOCK_ROOM_AREA) || inArea(TRAIL_START_AREA)) {
                            info("Walking to start of trail");
                            getWeb(new Tile(2903, 3460, 0)).execute();
                        } else if (myY <= 3460) {
                            info("Traverse bottom trail");
                            if (witchX > 0 || witch == null) {
                                switch (myX) {
                                    case 2903:
                                        if (witch == null || witchX >= 2908 && right)
                                            walkHere(new Tile(2909, 3460, 0));
                                        break;
                                    case 2909:
                                        if (witch == null || witchX >= 2916 && right)
                                            walkHere(new Tile(2917, 3460, 0));
                                        break;
                                    case 2917:
                                        if (witch == null || witchX >= 2924 && right)
                                            walkHere(new Tile(2925, 3460, 0));
                                        break;
                                    case 2925:
                                        if (witch == null || witchX <= 2923 && left)
                                            walkHere(new Tile(2932, 3466, 0));
                                        break;
                                }
                            }
                        } else if (myY >= 3466) {
                            info("Traverse top trail");
                            if (witchX > 0 || witch == null) {
                                if (getBot().getInventory().contains("Key")) {
                                    if (inArea(BOSS_FRONT_DOOR_AREA)) {
                                        if (useOnObject("Door", "Key")) {
                                            info("Used key on boss door");
                                                sleepUntil(3000, () -> inArea(BOSS_ROOM_AREA));
                                        }
//                                        if (getBot().getInventory().isSelected(i -> i != null && i.hasName("Key"))) {
//                                            info("Entering boss room");
//                                            if (interactObject("Door", "Use")) {
//                                                info("Used key on boss door");
//                                                sleepUntil(3000, () -> inArea(BOSS_ROOM_AREA));
//                                            }
//                                        } else if (interactInventory("Key", "Use")) {
//                                            info("Selected key");
//                                            sleepUntil(3000, () -> getBot().getInventory().isSelected(i -> i != null && i.hasName("Key")));
//                                        }
                                    } else {
                                        info("Have key traverse to boss door");
                                        switch (myX) {
                                            case 2914:
                                                if (witch == null || witchX < myX && left || witchX >= 2920 && right)
                                                    walkHere(new Tile(2921, 3466, 0));
                                                break;
                                            case 2921:
                                                if (witch == null || witchX < myX && left)
                                                    walkHere(new Tile(2928, 3466, 0));
                                                break;
                                            case 2928:
                                                if (witch == null || witchX < myX && left)
                                                    walkHere(new Tile(2933, 3466, 0));
                                                break;
                                        }
                                    }
                                } else {
                                    info("No key traverse to fountain");
                                    switch (myX) {
                                        case 2932:
                                            if (witch == null || witchX <= 2927 && left)
                                                walkHere(new Tile(2926, 3466, 0));
                                            break;
                                        case 2926:
                                            if (witch == null || witchX <= 2920 && left)
                                                walkHere(new Tile(2919, 3466, 0));
                                            break;
                                        case 2919:
                                            if (witch == null || witchX <= 2913 && left || witchX > myX && right)
                                                walkHere(new Tile(2910, 3468, 0));
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 6:
                    if (inArea(START_AREA)) {
                        info("Talking to boy");
                        if (talkTo("Boy"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (inArea(TRAIL_START_AREA)) {
                        info("Leaving witch compound");
                        if (interactObject("Door", "Open")) {
                            sleepUntil(3000, () -> inArea(HOUSE_TRAP_UNLOCK_ROOM_AREA));
                        }
                    } else if (inArea(BOSS_ROOM_AREA)) {
                        if (getBot().getInventory().contains("Ball")) {
                            info("We have the ball!");
                            if (interactObject("Door", "Open")) {
                                sleepUntil(3000, () -> inArea(BOSS_FRONT_DOOR_AREA));
                            }
                        } else if (interactGroundItem("Ball", "Take")) {
                            info("Grabbed ball");
                            sleepUntil(3000, () -> getBot().getInventory().contains("Ball"));
                        }
                    } else if (inArea(BOSS_FRONT_DOOR_AREA)) {
                        info("Walking to bottom traversal start");
                        walkHere(new Tile(2929, 3460, 0));
                    } else if (inArea(WITCH_TRAIL_AREA)) {
                        NPC witch = getBot().getNPCs().closest("Nora T. Hagg");
                        int witchX = 0;
                        boolean left = false, right = false;

                        if (witch != null) {
                            witchX = witch.getTile().getX();
                            left = witch.getOrientation() == 512;
                            right = witch.getOrientation() == 1536;
                        }

                        int myX = myPosition().getX();
                        info("Traverse bottom trail with ball");

                        if (witchX > 0 || witch == null) {
                            switch (myX) {
                                case 2929:
                                    if (witch == null || witchX <= 2923 && left)
                                        walkHere(new Tile(2923, 3460, 0));
                                    break;
                                case 2923:
                                    if (witch == null || witchX <= 2916 && left || witchX > myX && right)
                                        walkHere(new Tile(2915, 3460, 0));
                                    break;
                                case 2915:
                                    if (witch == null || witchX > myX && right)
                                        walkHere(new Tile(2907, 3460, 0));
                                    break;
                                case 2907:
                                    if (witch == null || witchX > myX && right)
                                        walkHere(new Tile(2901, 3465, 0));
                                    break;
                            }
                        }
                    } else {
                        info("Got ball traverse to boy");
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 7:
                    // End
                    info("Finished: " + Quest.WITCHS_HOUSE.name());
                    setComplete();
                    break;
            }
        }

        // Help delay for walking : standard 800 ms
        sleep(myPlayer().isMoving() ? 3000 : 800);
    }

    @Override
    public void onFinish() {
        setGrabbedItems(false);
    }
}
