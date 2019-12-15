package org.quester.questevents.p2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.ItemCombineEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

import java.util.HashMap;

public class PlagueCityEvent extends QuestContext implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "What's happened to her?", "Can I help find her?", "Yes.", "Hi, I'm looking for a woman from East Ardougne.",
            "Yes, I'll return it for you.", "But I think a kidnap victim is in here.", "I want to check anyway.",
            "I need permission to enter a plague house.", "This is urgent though!", "This is really important though!",
            "Do you know what's in the cure?", "They won't listen to me!"

    };
    private final Area START_AREA = new Area(2563, 3336, 2570, 3329);
    private final Area PAINTING_AREA = new Area(2574, 3335, 2577, 3331);
    private final Area REHNISON_HOUSE_TOP_FLOOR_AREA = new Area(2527, 3333, 2533, 3329, 1);
    private final Area REHNISON_HOUSE_ENTRANCE_AREA = new Area(2529, 3328, 2533, 3327);
    private final Area PLAGUE_HOUSE_ENTRANCE_AREA = new Area(2531, 3273, 2534, 3272);
    private final Area PLAGUE_HOUSE_GROUND_FLOOR_AREA = new Area(2532, 3271, 2541, 3268);
    private final Area BRAVEK_HOUSE_ENTRANCE_AREA = new Area(2527, 3315, 2529, 3312);
    private final Area BRAVEK_HOUSE_AREA = new Area(2530, 3316, 2539, 3312);
    private final Area PRISON_ROOM_AREA = new Area(2536, 9673, 2542, 9669);
    private final Area MAN_HOLE_AREA = new Area(2525, 3306, 2535, 3300);
    private boolean askedToPull;

    private HashMap<String, Integer> itemReq = new HashMap<>();

    public PlagueCityEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Dwellberries", 1);
        itemReq.put("Rope", 1);
        itemReq.put("Spade", 1);
        itemReq.put("Bucket of water", 4);
        itemReq.put("Bucket of milk", 1);
        itemReq.put("Chocolate dust", 1);
        itemReq.put("Snape grass", 1);
        itemReq.put("Necklace of passage(1~5)", 1);
        info("Started: " + Quest.PLAGUE_CITY.name());
        setGrabbedItems(false);
        askedToPull = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(165);

        if (result == 0 && !hasQuestItemsBeforeStarting(itemReq, false) && !isGrabbedItems()) {
            if (hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Make sure we deposit worn items.
                new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible()
                        && w.hasAction("Deposit worn items")).executed();
                sleep(2000);
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

        info("Quest stage: 165 = " + result);
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
        } else {
            switch (result) {
                case 28:
                case 2:
                    // Return to Edmond
                case 0:
                    // Start
                    if (inArea(START_AREA)) {
                        info("Talking to Edmond");
                        if (talkTo("Edmond"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to Edmond");
                        getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (!getBot().getInventory().contains("Picture")) {
                        if (inArea(PAINTING_AREA)) {
                            info("Grabbing picture");
                            if (interactGroundItem("Picture", "Take"))
                                sleepUntil(3000, () -> getBot().getInventory().contains("Picture"));
                        } else {
                            info("Walking to picture");
                            getWeb(PAINTING_AREA).execute();
                        }
                    } else if (talkTo("Alrena")) {
                        info("Talking to Alrena");
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 6:
                    // Keep putting buckets
                case 5:
                    // Keep putting buckets
                case 4:
                    // Keep putting buckets
                case 3:
                    info("Add water to mud patch");
                    if (useOnObject("Mud patch", "Bucket of water"))
                        sleepGameCycle();
                    break;
                case 7:
                    info("Dig into patch");
                    if (useOnObject("Mud patch", "Spade"))
                        sleepUntil(4000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 8:
                    info("Attempt to open grill");
                    if (interactObject("Grill", "Open")) {
                        sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                        if (useOnObject("Grill", "Rope"))
                            sleepUntil(6000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 9:
                    if (askedToPull) {
                        info("Waiting...");
                        sleepGameCycle();
                        return;
                    } else if (talkTo("Edmond")) {
                        sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                        askedToPull = true;
                    } else {
                        info("Walking closer to Edmond");
                        getWeb(new Tile(2517, 9748, 0)).execute();
                    }
                    break;
                case 10:
                    if (getBot().getInventory().contains("Gas mask")) {
                        info("Wearing gas mask");
                        if (interactInventory("Gas mask", "Wear")) {
                            sleepUntil(3000, () -> !getBot().getInventory().contains("Gas mask"));
                        }
                    } else if (myPosition().getY() > 9000) {
                        info("Leaving sewer");
                        if (interactObject("Pipe", "Climb-up")) {
                            sleepUntil(10000, () -> myPosition().getY() < 9000);
                        }
                    } else if (talkTo("Jethick")) {
                        info("Talking to Jethick");
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 20:
                    if (inArea(REHNISON_HOUSE_ENTRANCE_AREA)) {
                        info("At entrance");
                        if (interactObject("Door", "Open"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        info("Walking to rehnison house");
                        getWeb(REHNISON_HOUSE_ENTRANCE_AREA).execute();
                    }
                    break;
                case 21:
                    info("Talking to Ted");
                    if (talkTo("Ted Rehnison"))
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    break;
                case 22:
                    if (inArea(REHNISON_HOUSE_TOP_FLOOR_AREA)) {
                        info("Talking to mili");
                        if (talkTo("Milli Rehnison"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else if (interactObject("Stairs", "Walk-up")) {
                        info("Going upstairs");
                        sleepUntil(4000, () -> inArea(REHNISON_HOUSE_TOP_FLOOR_AREA));
                    }
                    break;
                case 23:
                    if (inArea(REHNISON_HOUSE_TOP_FLOOR_AREA)) {
                        if (interactObject("Stairs", "Walk-down")) {
                            info("Going downstairs");
                            sleepUntil(4000, () -> !inArea(REHNISON_HOUSE_TOP_FLOOR_AREA));
                        }
                    } else if (inArea(PLAGUE_HOUSE_ENTRANCE_AREA)) {
                        info("Attempt to enter plague house");
                        if (interactObject("Door", "Open")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to plague house");
                        getWeb(PLAGUE_HOUSE_ENTRANCE_AREA).execute();
                    }
                    break;
                case 24:
                    if (inArea(BRAVEK_HOUSE_ENTRANCE_AREA)) {
                        info("Attempt to enter bravek room");
                        if (talkTo("Clerk")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to bravek house");
                        getWeb(BRAVEK_HOUSE_ENTRANCE_AREA).execute();
                    }
                    break;
                case 25:
                    if (inArea(BRAVEK_HOUSE_AREA)) {
                        info("talking to bravek");
                        if (talkTo("Bravek")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to bravek");
                        getWeb(BRAVEK_HOUSE_AREA).execute();
                    }
                    break;
                case 26:
                    if (getBot().getInventory().contains("Chocolate dust")) {
                        info("Combine choco dust & milk");
                        if (new ItemCombineEvent(getBot(), "Chocolate dust", "Bucket of milk").executed())
                            sleepGameCycle();
                    } else if (getBot().getInventory().contains("Snape grass")) {
                        info("Add snape grass to the mix!");
                        if (new ItemCombineEvent(getBot(), "Snape grass", "Chocolatey milk").executed())
                            sleepGameCycle();
                    } else if (talkTo("Bravek")) {
                        info("talking to bravek");
                        sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 27:
                    if (inArea(PRISON_ROOM_AREA)) {
                        if (useOnObject("Door", "A small key")) {
                            info("Unlock door");
                            sleep(2000);
                            if (talkTo("Elena")) {
                                info("talking to Elena");
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        }
                    } else if (inArea(PLAGUE_HOUSE_GROUND_FLOOR_AREA)) {
                        if (!getBot().getInventory().contains("A small key")) {
                            info("Grabbing key");
                            if (interactObject("Barrel", "Search"))
                                sleepUntil(3000, () -> getBot().getInventory().contains("A small key"));
                        } else if (interactObject("Spooky stairs", "Walk-down")) {
                            info("Walking downstairs");
                            sleepUntil(4000, () -> !inArea(PLAGUE_HOUSE_GROUND_FLOOR_AREA));
                        }
                    } else if (inArea(PLAGUE_HOUSE_ENTRANCE_AREA)) {
                        info("Attempt to enter plague house");
                        if (interactObject("Door", "Open")) {
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                        }
                    } else {
                        info("Walking to plague house");
                        getWeb(PLAGUE_HOUSE_ENTRANCE_AREA).execute();
                    }
                    break;
                case 29:
                    if (!new CloseInterfacesEvent(getBot()).executed())
                        return;

                    if (interactInventory("Ardougne teleport scroll", "Read")) {
                        sleepUntil(5000, () -> getBot().getDialogues().inDialogue());
                    }
                    break;
                case 30:
                    // End
                    info("Finished: " + Quest.PLAGUE_CITY.name());
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

