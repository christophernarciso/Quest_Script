package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Prayer;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.HealEvent;
import org.quantumbot.events.TogglePrayerEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class TreeGnomeVillageEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Can I help at all?", "I would be glad to help.",
            "Ok, I'll gather some wood.", "I'll try my best.",
            "Yes please.", "I will find the warlord and bring back the orbs."
    };

    private final Area START_AREA = new Area(2536, 3172, 2547, 3167);
    private final Area VILLAGE_AREA = new Area(2514, 3175, 2548, 3161);
    private final Area RAILING_OUTSIDE_AREA = new Area(2514, 3160, 2518, 3157);
    private final Area BATTLEFIELD_AREA = new Area(2513, 3214, 2536, 3202);
    private final Area TRACKER_ONE_AREA = new Area(2498, 3261, 2507, 3260);
    private final Area TRACKER_TWO_AREA = new Area(2523, 3256, 2525, 3255);
    private final Area TRACKER_THREE_AREA = new Area(2493, 3245, 2499, 3239);
    private final Area BALLISTA_AREA = new Area(2505, 3213, 2513, 3206);
    private final Area BATTLEFIELD_COMPOUND_ENTRANCE_AREA = new Area(2507, 3253, 2511, 3252);
    private final Area BATTLEFIELD_COMPOUND_GROUND_AREA = new Area(
            new int[][]{
                    {2500, 3259},
                    {2500, 3252},
                    {2501, 3251},
                    {2504, 3251},
                    {2507, 3254},
                    {2512, 3254},
                    {2513, 3255},
                    {2513, 3259},
                    {2512, 3260},
                    {2501, 3260}
            }, 0
    );
    private final Area BATTLEFIELD_COMPOUND_UPPER_AREA = new Area(
            new int[][]{
                    {2500, 3259},
                    {2500, 3252},
                    {2501, 3251},
                    {2504, 3251},
                    {2507, 3254},
                    {2512, 3254},
                    {2513, 3255},
                    {2513, 3259},
                    {2512, 3260},
                    {2501, 3260}
            }, 1
    );
    private final Area ELKOY_AREA = new Area(2500, 3193, 2505, 3190);
    private final Area WARLORD_AREA = new Area(2454, 3303, 2458, 3294);

    private boolean spokeToTrackerOne, spokeToTrackerTwo, spokeToTrackerThree, spokeToMontai;
    private int index = 1;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public TreeGnomeVillageEvent(QuantumBot context) {
        super(context);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Prayer potion(4)", 3);
        itemReq.put("Ring of dueling(1~8)", 1);
        itemReq.put("Mind rune", 400);
        itemReq.put("Air rune", 900);
        itemReq.put("Lobster", 5);
        itemReq.put("Logs", 6);
        if (getBot().getClient().getSkillReal(Skill.MAGIC) >= 13)
            itemReq.put("Staff of fire", 1);
        else
            itemReq.put("Staff of earth", 1);
        itemReq.put("Amulet of magic", 1);
        itemReq.put("Wizard hat", 1);
        info("Started: " + Quest.TREE_GNOME_VILLAGE.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(111);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Make sure we deposit worn items.
                new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible()
                        && w.hasAction("Deposit worn items")).executed();

                sleep(2000);

                // Load bank event and execute withdraw
                if (getBankEvent(itemReq).executed()) {
                    if (closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Amulet of magic", "Wizard hat", "Staff of earth", "Staff of fire"};
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

        info("Quest stage: 111 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getCamera().isLocked()) {
            info("Dialogue");
            if (getBot().getDialogues().isPendingContinuation()) {
                info("Handling continue");
                if (new DialogueEvent(getBot()).setInterruptCondition(() -> getBot().getDialogues().isPendingOption()).executed())
                    sleepUntil(2000, () -> !getBot().getDialogues().isPendingContinuation());
            } else if (getBot().getDialogues().isPendingOption()) {
                if (result == 4 && inArea(BALLISTA_AREA)) {
                    getDialogue("000" + index++).execute();
                    return;
                }

                info("Handling option");
                info("QUEST_DIALOGUE");
                new DialogueEvent(getBot(), QUEST_DIALOGUE).execute();
                sleep(1000);
            } else {
                info("No dialogue???");
            }
        } else if (getBot().getInventory().contains(item -> item.getName().contains("Prayer potion")) && getBot().getClient().getSkillBoosted(Skill.PRAYER) < 10) {
            if (interactInventory(item -> item.getName().contains("Prayer potion"), "Drink"))
                sleepUntil(3000, () -> getBot().getClient().getSkillBoosted(Skill.PRAYER) > 10);
        } else {
            switch (result) {
                case 0:
                    // Squeeze-through Loose Railing
                    // Start
                    if (inArea(RAILING_OUTSIDE_AREA)) {
                        if (interactObject("Loose Railing", "Squeeze-through"))
                            sleepUntil(4000, () -> inArea(VILLAGE_AREA));
                    } else if (inArea(VILLAGE_AREA)) {
                        if (!talkTo("King Bolren"))
                            getWeb(START_AREA).execute();
                    } else {
                        getWeb(RAILING_OUTSIDE_AREA).execute();
                    }
                    break;
                case 3:
                case 2:
                case 1:
                    if (!inArea(BATTLEFIELD_AREA)) {
                        getWeb(BATTLEFIELD_AREA).execute();
                        return;
                    }

                    if (talkTo("Commander Montai"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;

                case 4:
                    if (!spokeToTrackerOne) {
                        if (!inArea(TRACKER_ONE_AREA)) {
                            getWeb(TRACKER_ONE_AREA).execute();
                            return;
                        }

                        if (talkTo("Tracker gnome 1")) {
                            spokeToTrackerOne = true;
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (!spokeToTrackerTwo) {
                        if (!inArea(TRACKER_TWO_AREA)) {
                            getWeb(TRACKER_TWO_AREA).execute();
                            return;
                        }

                        if (talkTo("Tracker gnome 2", false)) {
                            spokeToTrackerTwo = true;
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (!spokeToTrackerThree) {
                        if (!inArea(TRACKER_THREE_AREA)) {
                            getWeb(TRACKER_THREE_AREA).execute();
                            return;
                        }

                        if (talkTo("Tracker gnome 3")) {
                            spokeToTrackerThree = true;
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (!inArea(BALLISTA_AREA)) {
                        getWeb(BALLISTA_AREA).execute();
                    } else if (interactObject("Ballista", "Fire")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;

                case 5:
                    if (!spokeToMontai) {
                        if (!inArea(BATTLEFIELD_AREA)) {
                            getWeb(BATTLEFIELD_AREA).execute();
                            return;
                        }

                        if (talkTo("Commander Montai")) {
                            spokeToMontai = true;
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (inArea(BATTLEFIELD_COMPOUND_UPPER_AREA)) {
                        if (interactObject("Closed chest", "Open"))
                            sleep(2000);
                        if (interactObject("Open chest", "Search"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (inArea(BATTLEFIELD_COMPOUND_GROUND_AREA)) {
                        GameObject door = getBot().getGameObjects().closest(d -> d.hasAction("Open") && d.hasName("Door")
                                && d.getId() == 1535 && BATTLEFIELD_COMPOUND_GROUND_AREA.contains(d));
                        if (door != null) {
                            info("Need to open the door.");
                            if (getInteractEvent(door, "Open").executed())
                                sleepUntil(1500, () -> !door.exists());
                        } else if (interactObject("Ladder", "Climb-up")) {
                            sleepUntil(3000, () -> inArea(BATTLEFIELD_COMPOUND_UPPER_AREA));
                        }
                    } else if (inArea(BATTLEFIELD_COMPOUND_ENTRANCE_AREA)) {
                        if (myPlayer().isAnimating())
                            return;

                        if (!getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                            new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, true).execute();
                            return;
                        }

                        if (interactObject("Crumbled wall", "Climb-over"))
                            sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(BATTLEFIELD_COMPOUND_ENTRANCE_AREA).execute();
                    }
                    break;
                case 8:
                case 6:
                    if (inArea(WARLORD_AREA) && !getBot().getInventory().contains("Orbs of protection")) {
                        if (interactGroundItem("Orbs of protection", "Take"))
                            sleepUntil(3000, () -> getBot().getInventory().contains("Orbs of protection"));
                    } else if (inArea(BATTLEFIELD_COMPOUND_UPPER_AREA)) {
                        if (interactObject("Ladder", "Climb-down"))
                            sleepUntil(3000, () -> !inArea(BATTLEFIELD_COMPOUND_UPPER_AREA));
                    } else if (inArea(RAILING_OUTSIDE_AREA)) {
                        if (interactObject("Loose Railing", "Squeeze-through"))
                            sleepUntil(4000, () -> inArea(VILLAGE_AREA));
                    } else if (inArea(VILLAGE_AREA)) {
                        if (result == 8)
                            sleep(10000);
                        if (!talkTo("King Bolren"))
                            getWeb(START_AREA).execute();
                    } else if (!inArea(ELKOY_AREA)) {
                        if (getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                            new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, false).execute();
                            return;
                        }
                        getWeb(ELKOY_AREA).execute();
                    } else if (talkTo("Elkoy")) {
                        sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;

                case 7:
                    if (ourHealthPercent() <= 50) {
                        new HealEvent(getBot()).execute();
                        return;
                    }

                    if (!isAutocasting()) {
                        info("Need to autocast spell");
                        if (autocastSpell(getBot().getClient().getSkillReal(Skill.MAGIC) >= 13 ? StandardSpellbook.FIRE_STRIKE : StandardSpellbook.EARTH_STRIKE, false)) {
                            sleepUntil(3000, this::isAutocasting);
                        }
                        return;
                    }

                    if (!inArea(WARLORD_AREA)) {
                        getWeb(WARLORD_AREA).execute();
                        return;
                    }

                    if (!getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                        new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, true).execute();
                        return;
                    }

                    NPC warlord = getBot().getNPCs().closest(w -> w.hasAction("Attack") && w.hasName("Khazard warlord"));
                    if (warlord != null) {
                        if (myPlayer().isInteracting(warlord))
                            return;
                        if (getInteractEvent(warlord, "Attack").executed())
                            sleepUntil(3000, () -> myPlayer().isInteracting(warlord));
                    }

                    if (talkTo("Khazard warlord"))
                        sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    break;

                case 9:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.TREE_GNOME_VILLAGE.name());
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
