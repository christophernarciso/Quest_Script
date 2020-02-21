package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.NPC;
import org.quantumbot.api.map.Area;
import org.quantumbot.enums.Prayer;
import org.quantumbot.enums.Quest;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.TogglePrayerEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class FightArenaEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Can I help you?", "I'd like a Khali brew please."
    };

    private final Area START_AREA = new Area(2563, 3201, 2571, 3194);
    private final Area ARMOUR_AREA = new Area(
            new int[][]{
                    {2612, 3191},
                    {2612, 3190},
                    {2613, 3189},
                    {2614, 3189},
                    {2615, 3190},
                    {2615, 3191}
            }
    );
    private final Area JAIL_ENTRANCE_NORTHERN_AREA = new Area(
            new int[][]{
                    {2616, 3173},
                    {2617, 3172},
                    {2619, 3172},
                    {2619, 3173},
                    {2619, 3174},
                    {2618, 3174}
            }
    );
    private final Area JAIL_PRISON_CELLS_NORTHERN_AREA = new Area(
            new int[][]{
                    {2615, 3172},
                    {2614, 3170},
                    {2614, 3156},
                    {2615, 3155},
                    {2616, 3155},
                    {2616, 3152},
                    {2612, 3152},
                    {2612, 3139},
                    {2620, 3139},
                    {2620, 3151},
                    {2618, 3152},
                    {2618, 3155},
                    {2619, 3155},
                    {2620, 3156},
                    {2620, 3171},
                    {2619, 3172}
            }
    );
    private final Area JAIL_PRISON_CELLS_WESTERN_AREA = new Area(2585, 3144, 2603, 3139);
    private final Area GUARD_AREA = new Area(2613, 3145, 2619, 3139);
    private final Area BAR_AREA = new Area(2563, 3150, 2570, 3139);
    private final Area SAMMY_PRISON_AREA = new Area(2617, 3171, 2618, 3166);
    private final Area FIGHT_ARENA = new Area(2595, 3167, 2606, 3154);
    private HashMap<String, Integer> itemReq = new HashMap<>();
    private boolean talkedToNPC;

    public FightArenaEvent(QuantumBot context) {
        super(context);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Prayer potion(4)", 3);
        itemReq.put("Ardougne teleport", 1);
        itemReq.put("Mind rune", 400);
        itemReq.put("Air rune", 900);
        itemReq.put("Lobster", 5);
        itemReq.put("Coins", 100);
        if (getBot().getClient().getSkillReal(Skill.MAGIC) >= 13)
            itemReq.put("Staff of fire", 1);
        else
            itemReq.put("Staff of earth", 1);
        itemReq.put("Amulet of magic", 1);
        info("Started: " + Quest.FIGHT_ARENA.name());
        setGrabbedItems(false);
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(17);

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

        info("Quest stage: 17 = " + result);
        if (getBot().getDialogues().inDialogue() || getBot().getCamera().isLocked()) {
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
        } else if (getBot().getInventory().contains(item -> item.getName().contains("Prayer potion")) && getBot().getClient().getSkillBoosted(Skill.PRAYER) < 10) {
            if (interactInventory(item -> item.getName().contains("Prayer potion"), "Drink"))
                sleepUntil(3000, () -> getBot().getClient().getSkillBoosted(Skill.PRAYER) > 10);
        } else {
            switch (result) {
                case 13:
                    if (myPlayer().isAnimating())
                        return;

                    if (getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                        new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, false).execute();
                        talkedToNPC = false;
                        return;
                    }
                case 0:
                    // Start
                    if (!inArea(START_AREA)) {
                        getWeb(START_AREA).execute();
                        return;
                    }

                    if (talkTo("Lady Servil"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 1:
                    if (!inArea(ARMOUR_AREA)) {
                        getWeb(ARMOUR_AREA).execute();
                        return;
                    }

                    if (interactObject("Chest", "Open"))
                        sleep(2000);

                    if (interactObject("Chest", "Search"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 2:
                    if (getBot().getInventory().contains("Khazard helmet", "Khazard armour")) {
                        String[] equip = {"Khazard helmet", "Khazard armour"};
                        for (String x : equip) {
                            if (interactInventory(x, "Wear"))
                                sleep(1000);
                        }
                    } else if (inArea(JAIL_PRISON_CELLS_NORTHERN_AREA)) {
                        if (!talkedToNPC) {
                            if (talkTo("Sammy Servil", false)) {
                                talkedToNPC = true;
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else if (!inArea(GUARD_AREA)) {
                            getWeb(GUARD_AREA).execute();
                        } else if (talkTo("Khazard Guard")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else if (inArea(JAIL_ENTRANCE_NORTHERN_AREA)) {
                        if (interactObject("Door", "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        getWeb(JAIL_ENTRANCE_NORTHERN_AREA).execute();
                    }
                    break;
                case 3:
                    if (!getBot().getInventory().contains("Khali brew")) {
                        if (!inArea(BAR_AREA)) {
                            getWeb(BAR_AREA).execute();
                            return;
                        }

                        if (talkTo("Khazard barman"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (!inArea(GUARD_AREA)) {
                        getWeb(GUARD_AREA).execute();
                    } else if (talkTo("Khazard Guard")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    talkedToNPC = false;
                    break;
                case 5:
                    if (talkedToNPC)
                        return;

                    if (!isAutocasting()) {
                        info("Need to autocast spell");
                        if (autocastSpell(getBot().getClient().getSkillReal(Skill.MAGIC) >= 13 ? StandardSpellbook.FIRE_STRIKE : StandardSpellbook.EARTH_STRIKE, false)) {
                            sleepUntil(3000, this::isAutocasting);
                        }
                        return;
                    }

                    if (!inArea(SAMMY_PRISON_AREA)) {
                        getWeb(SAMMY_PRISON_AREA).execute();
                        return;
                    }

                    if (useOnObject(p -> p != null && SAMMY_PRISON_AREA.contains(p) && p.hasName("Prison Door") && p.getTile().getY() == 3167, "Khazard cell keys")) {
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        talkedToNPC = true;
                    }
                    break;
                case 6:
                    if (myPlayer().isMoving()) {
                        info("Moving....");
                        sleepUntil(25000, () -> !myPlayer().isMoving());
                        return;
                    }

                    // Fight bosses.
                    NPC boss_ogre = getBot().getNPCs().closest(n -> n != null && n.getTile().getX() <= 2606 && n.hasAction("Attack")
                            && n.isAttackable() && n.hasName("Khazard Ogre"));
                    if (boss_ogre == null) {
                        if (!getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                            new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, true).execute();
                            return;
                        }
                        if (talkTo("Justin Servil"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        return;
                    }

                    if (!myPlayer().isInteracting(boss_ogre)) {
                        info("Need to fight the boss.");
                        if (!getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                            new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, true).execute();
                            return;
                        }
                        if (getInteractEvent(boss_ogre, "Attack").executed())
                            sleepUntil(3000, () -> myPlayer().isInteracting(boss_ogre));
                    }
                    break;
                case 8:
                    // Dialogue
                    break;
                case 12:
                case 10:
                case 9:
                    if (myPlayer().isMoving()) {
                        info("Moving....");
                        sleepUntil(25000, () -> !myPlayer().isMoving());
                        return;
                    }

                    if (inArea(FIGHT_ARENA)) {
                        // Fight bosses.
                        String name = result == 12 ? "General Khazard" : result == 10 ? "Bouncer" : "Khazard Scorpion";
                        NPC bosses = getBot().getNPCs().closest(n -> n != null && n.hasAction("Attack") && n.getTile().getX() <= 2606
                                && n.isAttackable() && n.hasName(name));

                        if (!getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                            new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, true).execute();
                            return;
                        }

                        if (bosses == null) {
                            if (myPlayer().isAnimating() || myPlayer().isInteracting())
                                return;
                            if (talkTo("Sammy Servil"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            return;
                        }

                        if (!myPlayer().isInteracting(bosses)) {
                            info("Need to fight the boss.");
                            if (getInteractEvent(bosses, "Attack").executed())
                                sleepUntil(3000, () -> myPlayer().isInteracting(bosses));
                        }
                        return;
                    }

                    if (!inArea(JAIL_PRISON_CELLS_WESTERN_AREA))
                        return;

                    if (getBot().getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
                        new TogglePrayerEvent(getBot(), Prayer.PROTECT_FROM_MELEE, false).execute();
                        talkedToNPC = false;
                        return;
                    }

                    if (talkedToNPC)
                        return;

                    // Talk to Hengrad
                    if (talkTo("Hengrad")) {
                        talkedToNPC = true;
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 15:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;
                    // End
                    info("Finished: " + Quest.FIGHT_ARENA.name());
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
