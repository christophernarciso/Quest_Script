package org.quester.questutil;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.containers.Item;
import org.quantumbot.api.containers.ItemContainer;
import org.quantumbot.api.entities.*;
import org.quantumbot.api.interfaces.Interactable;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.Tab;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.*;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.ge.GEEvent;
import org.quantumbot.events.interactions.*;
import org.quantumbot.utils.StringUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class QuestContext extends BotEvent {

    private final Point DEFENSIVE_AUTOCAST_BUTTON_POSITION = new Point(650, 280);
    private final Point REGULAR_AUTOCAST_BUTTON_POSITION = new Point(649, 303);
    private boolean grabbedItems;

    public QuestContext(QuantumBot context) {
        super(context);
    }

    /**
     * HELPER METHODS
     */

    public Player myPlayer() {
        return getBot().getPlayers().getLocal();
    }

    public Tile myPosition() {
        return getBot().getPlayers().getLocal().getTile();
    }

    public WebWalkEvent getWeb(Locatable... locations) {
        return new WebWalkEvent(getBot(), locations);
    }

    public WebWalkEvent getWeb(Area area) {
        return new WebWalkEvent(getBot(), area);
    }

    public WebWalkEvent getWeb(Tile tile) {
        WebWalkEvent w = new WebWalkEvent(getBot(), tile);
        w.setInterruptCondition(() -> getBot().getInventory().contains(item -> item != null && item.hasAction("Eat"))
                && ourHealthPercent() <= 50);
        return w;
    }

    public InteractEvent getInteractEvent(Interactable interactable, String... actions) {
        return new InteractEvent(getBot(), interactable, actions);
    }

    public DialogueEvent getDialogue(String... options) {
        return options == null ? new DialogueEvent(getBot()) : new DialogueEvent(getBot(), options);
    }

    public BankEvent getBankEvent(HashMap<String, Integer> req) {
        BankEvent be = new BankEvent(getBot());
        be.stopIfUnmetReqs(true);

        for (String key : req.keySet())
            be.addReq(req.get(key), req.get(key), key);

        return be;
    }

    public boolean closeBank() throws InterruptedException {
        return !getBot().getBank().isOpen() || new CloseInterfacesEvent(getBot()).executed();
    }

    public boolean openBank() throws InterruptedException {
        return getBot().getBank().isOpen() || new BankOpenEvent(getBot()).executed();
    }

    public GEEvent getBuyableEvent(HashMap<String, Integer> req) {
        GEEvent ge = new GEEvent(getBot());
        ge.setAlwaysBuy(true);
        ge.setDepositAll(true);
        int totalCoins = (int) (getBot().getInventory().getAmount("Coins")
                + getBot().getBank().getAmount("Coins"));
        int expectedTotal = 0;
        int originalPrice, price;

        for (String key : req.keySet()) {
            int amt = req.get(key);
            if (key.contains("~")) {
                List<String> expanded = StringUtils.expandItemName(key);
                key = StringUtils.expandItemName(key).get(expanded.size() - 1);
                System.out.println("Expanded: " + key);
            }
            originalPrice = getBot().getPriceGrabber().getGEPrice(key);
            // Buy over 30% value for instant transactions > 500 : buy for 5,000 max
            price = originalPrice > 1500 || key.contains("rune") ? (int) (originalPrice + (originalPrice * .30)) : 5000;
            expectedTotal += price;

            System.out.println("Adding " + key + " to buy list");
            ge.buy(amt, price, key);
        }

        if (totalCoins < expectedTotal)
            return null;

        return ge;
    }

    public boolean talkTo(String npcName) throws InterruptedException {
        if (getBot().getDialogues().inDialogue())
            return true;

        return new NPCInteractEvent(getBot(), npcName, "Talk-to").executed();
    }

    public boolean interactNPC(String npcName, String... actions) throws InterruptedException {
        return new NPCInteractEvent(getBot(), npcName, actions).executed();
    }

    public boolean interactNPC(Predicate<NPC> npcPredicate, String... actions) throws InterruptedException {
        return new NPCInteractEvent(getBot(), npcPredicate, actions).executed();
    }

    public boolean useOnObject(String objectName, String useItemName) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), objectName).setWalk(false).setUse(useItemName).executed();
    }

    public boolean useOnObject(Predicate<GameObject> objectPredicate, String useItemName) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), objectPredicate).setWalk(false).setUse(useItemName).executed();
    }

    public boolean useOnObject(Predicate<GameObject> objectPredicate, int useItemID) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), objectPredicate).setWalk(false).setUse(useItemID).executed();
    }

    public boolean useOnNPC(String npcName, String useItemName) throws InterruptedException {
        return new NPCInteractEvent(getBot(), npcName).setWalk(false).setUse(useItemName).executed();
    }

    public boolean interactObject(String objectName, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), objectName, actions).executed();
    }

    public boolean interactObject(Predicate<GameObject> objectPredicate, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(getBot(), objectPredicate, actions).setWalk(false).executed();
    }

    public boolean interactGroundItem(String groundItemName, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(getBot(), groundItemName, actions).executed();
    }

    public boolean interactGroundItem(Predicate<GroundItem> groundItemPredicate, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(getBot(), groundItemPredicate, actions).executed();
    }

    public boolean interactInventory(String itemName, String... actions) throws InterruptedException {
        if (!getBot().getInventory().contains(itemName))
            return false;

        return new InventoryInteractEvent(getBot(), itemName, actions).executed();
    }

    public boolean interactInventory(Predicate<Item> itemPredicate, String... actions) throws InterruptedException {
        if (!getBot().getInventory().contains(itemPredicate))
            return false;

        return new InventoryInteractEvent(getBot(), itemPredicate, actions).executed();
    }

    public boolean inArea(Area area) {
        return area.contains(myPlayer());
    }

    public boolean atExactPosition(Tile tile) {
        return myPlayer().getTile().equals(tile);
    }

    public boolean hasQuestItemsBeforeStarting(HashMap<String, Integer> list, boolean bank) {
        if (list == null || list.isEmpty())
            return true;

        for (String key : list.keySet()) {
            if (getBot().getEquipment().contains(key))
                continue;

            if ((int) getBot().getBank().getAmount(key) < list.get(key) && bank) {
                //System.out.println("Missing " + key + " x" + list.get(key) + " from the bank");
                return false;
            } else if ((int) getBot().getInventory().getAmount(key) < list.get(key) && !bank) {
                //System.out.println("Missing  " + key + " x" + list.get(key) + " from the inventory");
                return false;
            }
        }
        return true;
    }

    public boolean isGrabbedItems() {
        return grabbedItems;
    }

    public void setGrabbedItems(boolean grabbedItems) {
        this.grabbedItems = grabbedItems;
    }

    private boolean openAutocastPanel(boolean defensive) throws InterruptedException {
        if (isAutocastPanelOpen()) return true;

        if (!getBot().getTabs().isOpen(Tab.COMBAT_OPTIONS))
            new TabEvent(getBot(), Tab.COMBAT_OPTIONS).execute();

        Optional<Widget> button = getBot().getWidgets().getAll().stream().filter(w -> w != null && w.isVisible()
                && w.hasAction("Choose spell") && w.getX() == (defensive ? DEFENSIVE_AUTOCAST_BUTTON_POSITION.getX() : REGULAR_AUTOCAST_BUTTON_POSITION.getX())
                && w.getY() == (defensive ? DEFENSIVE_AUTOCAST_BUTTON_POSITION.getY() : REGULAR_AUTOCAST_BUTTON_POSITION.getY())).findFirst();
        if (button.isPresent() && button.get().isVisible()) {
            return new InteractEvent(getBot(), button.get(), "Choose spell").executed();
        }
        return false;
    }

    private boolean isAutocastPanelOpen() {
        Widget panel = getBot().getWidgets().first(w -> w != null && w.getText() != null && w.getText().equals("Select a Combat Spell"));
        return panel != null && panel.isVisible();
    }

    public boolean isAutocasting() {
        return getBot().getVarps().getVarp(108) != 0;
    }

    public boolean autocastSpell(StandardSpellbook spellbook, boolean defensive) throws InterruptedException {
        if (isAutocastPanelOpen()) {
            System.out.println("Autocast panel open!");
            if (new WidgetInteractEvent(getBot(), w -> w != null && w.isVisible() && w.hasAction(spellbook.getSpellName()), spellbook.getSpellName()).executed()) {
                System.out.println("Autocast: " + spellbook.getSpellName());
                return true;
            }
        } else {
            System.out.println("Open autocast panel.");
            openAutocastPanel(defensive);
        }
        return false;
    }

    public int ourHealthPercent() {
        int currHealth = getBot().getClient().getSkillBoosted(Skill.HITPOINTS);
        int maxHealth = getBot().getClient().getSkillReal(Skill.HITPOINTS);
        return ((currHealth * 100) / maxHealth);
    }

    public void walkHere(Tile pos) throws InterruptedException {
        if (new InteractEvent(getBot(), pos, "Walk here").executed())
            System.out.println("Walk => " + pos.toString());
    }

    public int getQuantity(ItemContainer container, String name) {
        return (int) container.getAmount(name);
    }

    public int getQuantity(ItemContainer container, int id) {
        return (int) container.getAmount(id);
    }
}
