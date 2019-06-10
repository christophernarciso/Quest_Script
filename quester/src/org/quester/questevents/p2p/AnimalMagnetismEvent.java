package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.ItemCombineEvent;
import org.quantumbot.events.containers.EquipmentInteractEvent;
import org.quantumbot.events.containers.StoreInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AnimalMagnetismEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "I would be happy to make your home a better place.",
            "I'm here about a quest.", "Okay, you need it more than I do, I suppose.",
            "Could I buy those chickens now, then?", "Could I buy 2 chickens?",
            "Hello, I'm here about those trees again.", "I'd love one, thanks"

    };
    private final Area START_AREA = new Area(3091, 3363, 3096, 3354);
    private final Area UNDEAD_FARM_AREA = new Area(3626, 3527, 3630, 3524);
    private final Area HUSBAND_AREA = new Area(3613, 3529, 3621, 3524);
    private final Area CRONE_AREA = new Area(3460, 3560, 3463, 3556);
    private final Area BONE_GRINDER_AREA = new Area(3651, 3528, 3668, 3511, 1);
    private final Area SLIME_SHOP_AREA = new Area(3699, 3506, 3711, 3495);
    private final Area ALTAR_AREA = new Area(3654, 3524, 3664, 3515);
    private final Area WITCH_AREA = new Area(3097, 3373, 3101, 3367);
    private final Area RIMMINGTON_MINE_AREA = new Area(2975, 3242, 2979, 3237);
    private final Area UNDEAD_TREE_AREA = new Area(3108, 3347, 3110, 3345);
    private final Area SLAYER_MASTER_AREA = new Area(2930, 3538, 2933, 3535);
    private boolean talkToHusband, haveIngred, talkToDisciple, talkToAva;

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public AnimalMagnetismEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Coins", 10000);
        itemReq.put("Mithril axe", 1);
        itemReq.put("Iron bar", 5);
        itemReq.put("Ghostspeak amulet", 1);
        itemReq.put("Bones", 4);
        itemReq.put("Pot", 4);
        itemReq.put("Hammer", 1);
        itemReq.put("Hard leather", 1);
        itemReq.put("Holy symbol", 1);
        itemReq.put("Polished buttons", 1);
        itemReq.put("Games necklace(1~8)", 1);
        itemReq.put("Draynor manor teleport", 4);

        info("Started: " + Quest.ANIMAL_MAGNETISM.name());
        helper.setGrabbedItems(false);
        talkToHusband = false;
        haveIngred = false;
        talkToDisciple = false;
        talkToAva = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(939);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw
                if (helper.getBankEvent(itemReq).executed()) {
                    if (helper.closeBank()) {
                        // Execute wear equipment.
                        sleepUntil(5000, () -> !getBot().getBank().isOpen());
                        String[] equipables = {"Ghostspeak amulet"};
                        for (String s : equipables) {
                            if (helper.interactInventory(s, "Wear", "Wield"))
                                sleep(1200);
                        }
                        // At this point we now have our items equipped.
                        helper.setGrabbedItems(true);
                    }
                }
            } else {
                // Load buy event and execute buy orders
                if (helper.getBuyableEvent(itemReq) == null) {
                    info("Failed: Not enough coins. Setting complete and stopping.");
                    setComplete();
                    getBot().stop();
                    return;
                }
                //info("GE event execute");
                helper.getBuyableEvent(itemReq).executed();
            }
            return;
        }

        info("Quest stage: 939 = " + result);
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
                case 220:
                    if (getBot().getInventory().contains("A pattern")){
                        if (new ItemCombineEvent(getBot(), "A pattern", "Hard leather").executed())
                            sleepGameCycle();
                    }
                case 210:
                case 190:
                case 0:
                    // Start
                    if (helper.inArea(START_AREA)) {
                        info("Talking to Ava");
                        if (helper.talkTo("Ava"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Ava");
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 76:
                    // Got crone amulet.
                    if (getBot().getInventory().contains("Ghostspeak amulet")) {
                        info("Wearing our ghostspeak amulet again.");
                        if (helper.interactInventory("Ghostspeak amulet", "Wear")) {
                            sleepUntil(4000, () -> getBot().getEquipment().contains("Ghostspeak amulet"));
                        }
                    }
                case 60:
                    // Husband and wife exchanges
                case 50:
                    // Husband and wife exchanges
                case 40:
                    // Husband and wife exchanges
                case 30:
                    // Husband and wife exchanges
                case 20:
                    // Husband and wife exchanges
                case 10:
                    if (talkToHusband) {
                        info("Talk to husband.");
                        helper.getWeb(UNDEAD_FARM_AREA).execute();
                        if (helper.talkTo("Alice's husband")) {
                            sleepUntil(10000, () -> getBot().getDialogues().inDialogue());
                            talkToHusband = false;
                        }
                    } else if (helper.inArea(UNDEAD_FARM_AREA)) {
                        info("Talk to Alice");
                        if (helper.talkTo("Alice")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            talkToHusband = true;
                        }
                    } else {
                        info("Walking to Alice");
                        helper.getWeb(UNDEAD_FARM_AREA).execute();
                    }
                    break;
                case 73:
                    if (getBot().getEquipment().contains("Ghostspeak amulet")) {
                        if (new EquipmentInteractEvent(getBot(), "Ghostspeak amulet", "Remove").executed()) {
                            info("Removed ghostspeak amulet");
                            sleepUntil(4000, () -> !getBot().getEquipment().contains("Ghostspeak amulet"));
                        }
                    }
                case 70:
                    // Get crone amulet
                    if (helper.inArea(CRONE_AREA)) {
                        info("Talking to crone");
                        if (helper.talkTo("Old crone")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to crone");
                        helper.getWeb(CRONE_AREA).execute();
                    }
                    break;

                case 80:
                    sleepGameCycle();
                    if (helper.talkTo("Alice's husband")) {
                        sleepUntil(10000, () -> getBot().getDialogues().inDialogue());
                        talkToHusband = false;
                    }
                    break;
                case 100:
                    if (getBot().getInventory().contains("Undead chicken")) {
                        if (helper.inArea(START_AREA)) {
                            info("Talking to Ava");
                            if (helper.talkTo("Ava"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else {
                            info("Walking to Ava");
                            helper.getWeb(START_AREA).execute();
                        }
                    } else if (getBot().getInventory().contains("Ecto-token")) {
                        info("Buying chickens");
                        if (helper.inArea(HUSBAND_AREA)) {
                            info("Talk to husband.");
                            if (helper.talkTo("Alice's husband")) {
                                sleepUntil(10000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else {
                            info("Walking to husband");
                            helper.getWeb(HUSBAND_AREA).execute();
                        }
                    } else if (talkToDisciple) {
                        if (helper.talkTo("Ghost disciple")) {
                            sleepUntil(10000, () -> getBot().getDialogues().inDialogue());
                            talkToDisciple = false;
                        }
                    } else if (!haveIngred && helper.getQuantity(getBot().getInventory(), "Bonemeal") < 4) {
                        if (helper.inArea(BONE_GRINDER_AREA)) {
                            info("making grinded bones");
                            if (helper.useOnObject("Loader", "Bones")) {
                                sleepUntil(120000, () -> !getBot().getInventory().contains("Bones"));
                                sleep(5000);
                            }
                        } else {
                            info("Walking to bone grinder");
                            helper.getWeb(BONE_GRINDER_AREA).execute();
                        }
                    } else if (!haveIngred && helper.getQuantity(getBot().getInventory(), "Bucket of slime") < 4) {
                        if (helper.inArea(SLIME_SHOP_AREA)) {
                            info("Buying slime");
                            if (getBot().getStoreInventory().isOpen()) {
                                if (getBot().getStoreInventory().contains("Bucket of slime")) {
                                    if (new StoreInteractEvent(getBot(), "Bucket of slime", "Buy 5").executed()) {
                                        sleepUntil(3000, () -> helper.getQuantity(getBot().getInventory(), "Buckets of slime") > 3);
                                        haveIngred = true;
                                    }
                                }
                            } else if (helper.interactNPC("Trader Crewmember", "Trade")) {
                                sleepUntil(5000, () -> getBot().getStoreInventory().isOpen());
                            }
                        } else {
                            info("Walking to buy slime");
                            helper.getWeb(SLIME_SHOP_AREA).execute();
                        }
                    } else if (helper.inArea(ALTAR_AREA)) {
                        info("Offering bones...");
                        if (helper.interactObject("Ectofuntus", "Worship")) {
                            sleepGameCycle();
                            if (!getBot().getInventory().contains("Bonemeal"))
                                talkToDisciple = true;
                        }
                    } else {
                        info("Walking to altar");
                        helper.getWeb(ALTAR_AREA).execute();
                    }
                    break;
                case 130:
                case 120:
                    if (helper.inArea(WITCH_AREA)) {
                        info("Talk to witch");
                        if (helper.talkTo("Witch")) {
                            sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to witch");
                        helper.getWeb(WITCH_AREA).execute();
                    }
                    break;
                case 140:
                    if (getBot().getInventory().contains("Bar magnet")) {
                        if (helper.inArea(START_AREA)) {
                            info("Talking to Ava");
                            if (helper.talkTo("Ava"))
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        } else {
                            info("Walking to Ava");
                            helper.getWeb(START_AREA).execute();
                        }
                    } else if (helper.inArea(RIMMINGTON_MINE_AREA)) {
                        info("shaping magnet");
                        if (helper.myPlayer().getOrientation() == 1024) {
                            info("Facing north");
                            if (new ItemCombineEvent(getBot(), "Hammer", "Selected iron").executed()) {
                                sleepUntil(6000, () -> getBot().getInventory().contains("Bar magnet"));
                            }
                        } else {
                            info("Attempting to face north.");
                            helper.getWeb(helper.myPosition().translate(0, 2)).execute();
                        }
                    } else {
                        info("Walking to mind");
                        helper.getWeb(RIMMINGTON_MINE_AREA).execute();
                    }
                    break;
                case 150:
                    if (helper.inArea(UNDEAD_TREE_AREA)) {
                        info("Attempting to cut tree.");
                        if (helper.interactNPC("Undead tree", "Chop")) {
                            talkToAva = true;
                            sleepGameCycle();
                        }
                    } else {
                        info("Walking to undead trees");
                        helper.getWeb(new Tile(3109, 3345, 0)).execute();
                    }
                    break;
                case 170:
                case 160:
                    if (talkToAva) {
                        info("Need to talk to ava");
                        if (helper.inArea(START_AREA)) {
                            info("Talking to Ava");
                            if (helper.talkTo("Ava")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                talkToAva = false;
                            }
                        } else {
                            info("Walking to Ava");
                            helper.getWeb(START_AREA).execute();
                        }
                    } else if (helper.inArea(SLAYER_MASTER_AREA)) {
                        info("at slayer master talking to him");
                        if (helper.talkTo("Turael"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to slayer master");
                        helper.getWeb(SLAYER_MASTER_AREA).execute();
                    }
                    break;
                case 180:
                    if (talkToAva) {
                        info("Need to talk to ava");
                        if (helper.inArea(START_AREA)) {
                            info("Talking to Ava");
                            if (helper.talkTo("Ava")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                                talkToAva = false;
                            }
                        } else {
                            info("Walking to Ava");
                            helper.getWeb(START_AREA).execute();
                        }
                    } else if (helper.inArea(UNDEAD_TREE_AREA)) {
                        if (getBot().getInventory().contains("Undead twigs"))
                            talkToAva = true;
                        else {
                            info("Attempting to cut tree.");
                            if (helper.interactNPC("Undead tree", "Chop")) {
                                sleepUntil(2000, () -> getBot().getInventory().contains("Undead twigs"));
                            }
                        }
                    } else {
                        info("Walking to undead trees");
                        helper.getWeb(new Tile(3109, 3345, 0)).execute();
                    }
                    break;
                case 200:
                    Widget notesInterface = getBot().getWidgets().get(480, 18);
                    if (notesInterface != null && notesInterface.isVisible()){
                        List<Widget> listOfButtons = getBot().getWidgets().getAll()
                                .stream()
                                .filter(w -> w != null && w.getRootId() == 480 && w.getTooltip() != null
                                        && w.getTooltip().equals("Off"))
                                .collect(Collectors.toList());
                        int i = 0;
                        for (Widget widget: listOfButtons) {
                            if (widget != null){
                                if (i == 1 || i == 4 || i == 8) {
                                    i++;
                                    continue;
                                }
                                if (helper.getInteractEvent(widget, "Off").executed()) {
                                    sleepGameCycle();
                                    i++;
                                }
                            }
                        }
                    } else if (helper.interactInventory("Research notes", "Translate")){
                        sleepUntil(4000, () -> getBot().getWidgets().getRoot(480).isVisible());
                        sleepGameCycle();
                    }
                    break;
                case 240:
                    // End
                    info("Finished: " + Quest.ANIMAL_MAGNETISM.name());
                    setComplete();
                    break;
            }
        }
        sleep(600);
    }

    @Override
    public void onFinish() {
        helper.setGrabbedItems(false);
    }
}


