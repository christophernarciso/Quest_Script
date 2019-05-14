package org.quester.questevents.f2p;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.ItemCombineEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questutil.HelperMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ErnestTheChickenEvent extends BotEvent implements Logger {

    private final String[] QUEST_DIALOGUE = {
            "Aha, sounds like a quest. I'll help.", "I'm looking for a guy called Ernest.",
            "Change him back this instant!"
    };
    private final Area START_AREA = new Area(3106, 3333, 3113, 3327);
    private final Area PROFESSOR_AREA = new Area(3108, 3370, 3112, 3362, 2);
    private final Area FOUNTAIN_AREA = new Area(3084, 3338, 3092, 3331);
    private final Area COMPOST_AREA = new Area(3085, 3356, 3089, 3353);
    private final Area TUBE_AREA = new Area(3108, 3368, 3112, 3366);
    private final Area BASEMENT_ENTRANCE_AREA = new Area(3091, 3363, 3096, 3354);
    // https://vignette.wikia.nocookie.net/2007scape/images/f/fe/Ernest_the_Chicken_-_Oil_can_map.png
    private final Area BASEMENT_A_B_AREA = new Area(3100, 9757, 3118, 9745);
    private final Area BASEMENT_C_D_AREA = new Area(3105, 9767, 3112, 9758);
    private final Area BASEMENT_E_F_AREA = new Area(3096, 9767, 3099, 9763);
    private final Area BASEMENT_2_3_AREA = new Area(3100, 9762, 3104, 9758);
    private final Area BASEMENT_4_5_AREA = new Area(3096, 9762, 3099, 9758);
    private final Area BASEMENT_7_8_AREA = new Area(3100, 9767, 3104, 9763);
    private final Area BASEMENT_OIL_CAN_AREA = new Area(3090, 9757, 3099, 9753);
    private List<String> levers = new ArrayList<>();
    private boolean done;

    private HelperMethods helper;
    private HashMap<String, Integer> itemReq = new HashMap<>();

    public ErnestTheChickenEvent(QuantumBot bot, HelperMethods helper) {
        super(bot);
        this.helper = helper;
    }

    @Override
    public void onStart() {
        // Required items needed
        itemReq.put("Spade", 1);
        itemReq.put("Fish food", 1);
        itemReq.put("Poison", 1);
        if (getBot().getClient().isMembers())
            itemReq.put("Draynor manor teleport", 1);
        info("Started: " + Quest.ERNEST_THE_CHICKEN.name());
        helper.setGrabbedItems(false);
        // Setup the levers
        levers.add("Lever B");
        levers.add("Lever A");
        levers.add("Lever D");
        levers.add("Lever B");
        levers.add("Lever A");
        levers.add("Lever F");
        levers.add("Lever E");
        levers.add("Lever C");
        levers.add("Lever E");
        done = false;
    }

    @Override
    public void step() throws InterruptedException {
        int result = getBot().getVarps().getVarp(32);
        int leverRes = getBot().getVarps().getVarp(33);

        if (result == 0 && !helper.hasQuestItemsBeforeStarting(itemReq, false) && !helper.isGrabbedItems()) {
            if (helper.hasQuestItemsBeforeStarting(itemReq, true)) {
                info("Bank event execute");
                // Load bank event and execute withdraw;
                helper.setGrabbedItems(helper.getBankEvent(itemReq).executed());
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

        info("Quest stage: 32 = " + result);
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
                    if (helper.inArea(START_AREA)) {
                        if (helper.talkTo("Veronica"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(START_AREA).execute();
                    }
                    break;
                case 1:
                    if (helper.inArea(PROFESSOR_AREA)) {
                        if (helper.talkTo("Professor Oddenstein"))
                            sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                    } else {
                        helper.getWeb(PROFESSOR_AREA).execute();
                    }
                    break;
                case 2:
                    if (!getBot().getInventory().contains("Pressure gauge")) {
                        if (helper.inArea(FOUNTAIN_AREA)) {
                            if (getBot().getInventory().contains("Poisoned fish food")) {
                                if (helper.useOnObject("Fountain", "Poisoned fish food")) {
                                    sleepUntil(4000, () -> !getBot().getInventory().contains("Poisoned fish food"));
                                }
                            } else if (getBot().getInventory().contains("Poison")) {
                                if (new ItemCombineEvent(getBot(), "Poison", "Fish food").executed()) {
                                    sleepUntil(3000, () -> getBot().getInventory().contains("Poisoned fish food"));
                                }
                            } else if (helper.interactObject("Fountain", "Search")) {
                                sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            }
                        } else {
                            helper.getWeb(FOUNTAIN_AREA).execute();
                        }
                    } else if (!getBot().getInventory().contains("Rubber tube")) {
                        if (getBot().getInventory().contains("Key")) {
                            if (helper.inArea(TUBE_AREA)) {
                                if (helper.interactGroundItem("Rubber tube", "Take")) {
                                    sleepUntil(3000, () -> getBot().getInventory().contains("Rubber tube"));
                                }
                            } else {
                                helper.getWeb(TUBE_AREA).execute();
                            }
                        } else {
                            if (helper.inArea(COMPOST_AREA)) {
                                if (helper.useOnObject("Compost heap", "Spade")) {
                                    sleepUntil(4000, () -> getBot().getInventory().contains("Key"));
                                }
                            } else {
                                helper.getWeb(COMPOST_AREA).execute();
                            }
                        }
                    } else if (!getBot().getInventory().contains("Oil can") || helper.inArea(BASEMENT_OIL_CAN_AREA)) {
                        if (helper.myPosition().getY() < 9000) {
                            if (helper.inArea(TUBE_AREA)) {
                                info("Leaving tube room");
                                if (helper.interactObject("Door", "Open")) {
                                    sleepUntil(3000, () -> !helper.inArea(TUBE_AREA));
                                }
                            } else if (helper.inArea(BASEMENT_ENTRANCE_AREA)){
                                info("going down the ladder");
                                if (helper.interactObject("Ladder", "Climb-down")){
                                    sleepUntil(4000, () -> helper.inArea(BASEMENT_OIL_CAN_AREA));
                                }
                            } else {
                                info("Walking to basement entrance");
                                helper.getWeb(BASEMENT_ENTRANCE_AREA).execute();
                            }
                        } else {
                            handleDoorMaze(leverRes);
                        }
                    } else if (done) {
                        if ((int) getBot().getInventory().getAmount("Coins") < 300) {
                            if (helper.inArea(PROFESSOR_AREA)) {

                                if (helper.talkTo("Professor Oddenstein"))
                                    sleepUntil(3000, () -> getBot().getDialogues().inDialogue());
                            } else {
                                sleep(4000);
                                helper.getWeb(PROFESSOR_AREA).execute();
                            }
                        }
                    }
                    break;

                case 3:
                    // End
                    info("Finished: " + Quest.ERNEST_THE_CHICKEN.name());
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

    private void handleDoorMaze(int leverRes) throws InterruptedException {
        info("33=" + leverRes);

        switch (leverRes) {
            case 88:
                if (helper.inArea(BASEMENT_E_F_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3100, 9765, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_E_F_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_7_8_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3102, 9763, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_7_8_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_2_3_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3102, 9758, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_2_3_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_A_B_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3100, 9755, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_A_B_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_OIL_CAN_AREA)) {
                    if (getBot().getInventory().contains("Oil can")) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3100, 9755, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(5000, () -> helper.inArea(BASEMENT_A_B_AREA));
                            break;
                        }
                    } else if (helper.interactGroundItem("Oil can", "Take")) {
                        sleepUntil(7000, () -> getBot().getInventory().contains("Oil can"));
                        done = true;
                    }
                }
                break;
            case 120:
                if (helper.inArea(BASEMENT_C_D_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3105, 9765, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_C_D_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_7_8_AREA)) {
                    if (helper.interactObject(d -> d != null && d.hasName("Door")
                                    && d.hasAction("Open")
                                    && d.getTile().equals(new Tile(3100, 9765, 0))
                            , "Open")) {
                        info("Opening next door");
                        sleepUntil(3000, () -> !helper.inArea(BASEMENT_7_8_AREA));
                        break;
                    }
                } else if (helper.inArea(BASEMENT_E_F_AREA)) {
                    info("Pull lever E");
                    sleep(1500);
                }
            case 112:
                if (leverRes == 112) {
                    if (helper.inArea(BASEMENT_E_F_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3100, 9765, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_E_F_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_7_8_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3105, 9765, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_7_8_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_C_D_AREA)) {
                        info("Pull lever C");
                        sleep(1000);
                    }
                }
            case 16:
                if (leverRes == 16) {
                    if (helper.inArea(BASEMENT_A_B_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3102, 9758, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_A_B_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_2_3_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3100, 9760, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_2_3_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_4_5_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3097, 9763, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_4_5_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_E_F_AREA)) {
                        info("Pull F & E");
                        sleep(1500);
                    }
                }
            case 22:
                if (leverRes == 22) {
                    if (helper.inArea(BASEMENT_A_B_AREA)) {
                        info("Pull lever B & A");
                        sleep(1500);
                    } else if (helper.inArea(BASEMENT_2_3_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3102, 9758, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_2_3_AREA));
                            break;
                        }
                    } else if (helper.inArea(BASEMENT_C_D_AREA)) {
                        if (helper.interactObject(d -> d != null && d.hasName("Door")
                                        && d.hasAction("Open")
                                        && d.getTile().equals(new Tile(3105, 9760, 0))
                                , "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_C_D_AREA));
                            break;
                        }
                    }
                }
            case 6:
                if (leverRes == 6) {
                    if (helper.inArea(BASEMENT_C_D_AREA)) {
                        info("Pull lever D");
                        sleep(1500);
                    } else {
                        if (helper.interactObject("Door", "Open")) {
                            info("Opening next door");
                            sleepUntil(3000, () -> !helper.inArea(BASEMENT_A_B_AREA));
                            break;
                        }
                    }
                }
            default:
                if (!levers.isEmpty()) {
                    if (helper.interactObject(levers.get(0), "Pull")) {
                        levers.remove(levers.get(0));
                        sleepUntil(7000, () -> helper.myPlayer().isAnimating());
                    }
                }
                break;
        }
    }
}
