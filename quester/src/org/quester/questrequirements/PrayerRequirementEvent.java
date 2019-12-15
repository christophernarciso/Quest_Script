package org.quester.questrequirements;

import com.sun.org.apache.regexp.internal.RE;
import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.requisitions.Requisition;
import org.quantumbot.enums.Skill;
import org.quantumbot.events.WorldHopEvent;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.events.containers.WithdrawEvent;
import org.quantumbot.events.ge.GEEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;

public class PrayerRequirementEvent extends QuestContext implements Logger {

    private boolean needToSetLastHouse;
    private final Area RIMMINGTON = new Area(2944, 3227, 2958, 3209, 0);

    public PrayerRequirementEvent(QuantumBot context) {
        super(context);
    }

    @Override
    public void onStart() {
        needToSetLastHouse = true;
    }

    @Override
    public void step() throws InterruptedException {
        int level = getBot().getClient().getSkillReal(Skill.PRAYER);

        if (level >= 45 && !isInHouse()) {
            info("Already have prayer level requirement.");
            setComplete();
            return;
        }

        if (getBot().getWorlds().getCurrent() != 330) {
            info("World hop to the house party world.");
            if (new WorldHopEvent(getBot(), 330).executed())
                sleepUntil(10000, () -> getBot().getWorlds().getCurrent() == 330);
            return;
        }

        if (level < 45 && !getBot().getInventory().contains(537) && !isInHouse()) {
            if (!getBot().getInventory().isEmpty()) {
                if (!getBot().getBank().isOpen())
                    new BankOpenEvent(getBot()).execute();
                else
                    new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible()
                            && w.hasAction("Deposit inventory")).executed();
                return;
            }

            info("Buying dragon bones");
            new GEEvent(getBot())
                    .setCollectUnnoted(false)
                    .setAlwaysBuy(true)
                    .setDepositAll(false)
                    .buy(300, getBot().getPriceGrabber().getBuyPrice(536), "Dragon bones", "Dragon bones")
            .execute();
            return;
        }

        if (getQuantity(getBot().getInventory(), "Coins") < 80000) {
            if (getBot().getBank().isOpen())
                new WithdrawEvent(getBot(), "Coins", 80000, false).execute();
            else
                new BankOpenEvent(getBot()).execute();
            return;
        }

        if (isInHouse()) {
            if (getBot().getInventory().contains(536)) {
                int lastXP = getBot().getClient().getSkillExp(Skill.PRAYER);
                if (useOnObject("Altar", 536))
                    sleepUntil(3000, 50,
                            () -> getBot().getClient().getSkillExp(Skill.PRAYER) > lastXP);
            } else if (interactObject("Portal", "Enter")) {
                sleepUntil(10000, () -> !isInHouse());
            }
        } else if (RIMMINGTON.contains(getBot().getPlayers().getLocal())) {
            if (getBot().getInventory().contains(536)) {
                if (needToSetLastHouse) {
                    if (getBot().getWidgets().hasOpenInterface()) {
                        info("Open interface available select a house");
                        if (new WidgetInteractEvent(getBot(), widget -> widget.hasAction("Enter House"), "Enter House").executed()) {
                            sleepUntil(10000, this::isInHouse);
                            needToSetLastHouse = false;
                            sleep(3500);
                        }
                    } else if (interactObject("House Advertisement", "View")) {
                        sleepUntil(5000, () -> getBot().getWidgets().hasOpenInterface());
                    }
                } else if (interactObject("House Advertisement", "Visit-Last")) {
                    sleepUntil(10000, this::isInHouse);
                    sleep(3500);
                    if (!isInHouse())
                        needToSetLastHouse = true;
                }
            } else if (getBot().getDialogues().inDialogue()) {
                if (getDialogue("Exchange All:").executed()) {
                    sleepUntil(3000, () -> getBot().getInventory().contains(536));
                }
            } else if (useOnNPC("Phials", 537)) {
                sleepUntil(6000, () -> getBot().getDialogues().inDialogue());
            } else if (!getBot().getInventory().contains(537)) {
                info("No more bones. Setting complete..");
                setComplete();
            }
        } else if (getWeb(RIMMINGTON).executed()) {
            info("Walked to rimmington");
        }
        sleep(100);
    }
}
