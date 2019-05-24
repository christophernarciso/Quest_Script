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
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.TabEvent;
import org.quantumbot.events.WebWalkEvent;
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

public class HelperMethods {

    private final Point DEFENSIVE_AUTOCAST_BUTTON_POSITION = new Point(650, 280);
    private final Point REGULAR_AUTOCAST_BUTTON_POSITION = new Point(649, 303);
    private QuantumBot context;
    private boolean grabbedItems;

    public HelperMethods(QuantumBot context) {
        this.context = context;
    }

    public QuantumBot getContext() {
        return this.context;
    }

    public Player myPlayer() {
        return context.getPlayers().getLocal();
    }

    public Tile myPosition() {
        return context.getPlayers().getLocal().getTile();
    }

    public WebWalkEvent getWeb(Locatable... locations) {
        return new WebWalkEvent(context, locations);
    }

    public WebWalkEvent getWeb(Area area) {
        return new WebWalkEvent(context, area);
    }

    public WebWalkEvent getWeb(Tile tile) {
        WebWalkEvent w = new WebWalkEvent(context, tile);
        w.setInterruptCondition(() -> context.getInventory().contains(item -> item != null && item.hasAction("Eat"))
                && ourHealthPercent() <= 50);
        return w;
    }

    public InteractEvent getInteractEvent(Interactable interactable, String... actions) {
        return new InteractEvent(context, interactable, actions);
    }

    public DialogueEvent getDialogue(String... options) {
        return options == null ? new DialogueEvent(context) : new DialogueEvent(context, options);
    }

    public BankEvent getBankEvent(HashMap<String, Integer> req) {
        BankEvent be = new BankEvent(context);
        be.stopIfUnmetReqs(true);

        for (String key : req.keySet())
            be.addReq(req.get(key), req.get(key), key);

        return be;
    }

    public boolean closeBank() throws InterruptedException {
        return !context.getBank().isOpen() || new CloseInterfacesEvent(context).executed();
    }

    public boolean openBank() throws InterruptedException {
        return context.getBank().isOpen() || new BankOpenEvent(context).executed();
    }

    public GEEvent getBuyableEvent(HashMap<String, Integer> req) {
        GEEvent ge = new GEEvent(context);
        ge.setAlwaysBuy(true);
        ge.setDepositAll(true);
        int totalCoins = (int) (context.getInventory().getAmount("Coins")
                + context.getBank().getAmount("Coins"));
        int expectedTotal = 0;
        int originalPrice, price;

        for (String key : req.keySet()) {
            int amt = req.get(key);
            if (key.contains("~")) {
                List<String> expanded = StringUtils.expandItemName(key);
                key = StringUtils.expandItemName(key).get(expanded.size() - 1);
                System.out.println("Expanded: " + key);
            }
            originalPrice = context.getPriceGrabber().getGEPrice(key);
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
        if (context.getDialogues().inDialogue())
            return true;

        return new NPCInteractEvent(context, npcName, "Talk-to").executed();
    }

    public boolean interactNPC(String npcName, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcName, actions).executed();
    }

    public boolean interactNPC(Predicate<NPC> npcPredicate, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcPredicate, actions).executed();
    }

    public boolean useOnObject(String objectName, String useItemName) throws InterruptedException {
        return new ObjectInteractEvent(context, objectName, "Use").setUse(useItemName).executed();
    }

    public boolean useOnObject(Predicate<GameObject> objectPredicate, String useItemName) throws InterruptedException {
        return new ObjectInteractEvent(context, objectPredicate, "Use").setUse(useItemName).executed();
    }

    public boolean interactObject(String objectName, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectName, actions).executed();
    }

    public boolean interactObject(Predicate<GameObject> objectPredicate, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectPredicate, actions).setWalk(false).executed();
    }

    public boolean interactGroundItem(String groundItemName, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemName, actions).executed();
    }

    public boolean interactGroundItem(Predicate<GroundItem> groundItemPredicate, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemPredicate, actions).executed();
    }

    public boolean interactInventory(String itemName, String... actions) throws InterruptedException {
        if (!context.getInventory().contains(itemName))
            return false;

        return new InventoryInteractEvent(context, itemName, actions).executed();
    }

    public boolean interactInventory(Predicate<Item> itemPredicate, String... actions) throws InterruptedException {
        if (!context.getInventory().contains(itemPredicate))
            return false;

        return new InventoryInteractEvent(context, itemPredicate, actions).executed();
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
            if (context.getEquipment().contains(key))
                continue;

            if ((int) context.getBank().getAmount(key) < list.get(key) && bank) {
                //System.out.println("Missing " + key + " x" + list.get(key) + " from the bank");
                return false;
            } else if ((int) context.getInventory().getAmount(key) < list.get(key) && !bank) {
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

        if (!context.getTabs().isOpen(Tab.COMBAT_OPTIONS))
            new TabEvent(context, Tab.COMBAT_OPTIONS).execute();

        Optional<Widget> button = context.getWidgets().getAll().stream().filter(w -> w != null && w.isVisible()
                && w.hasAction("Choose spell") && w.getX() == (defensive ? DEFENSIVE_AUTOCAST_BUTTON_POSITION.getX() : REGULAR_AUTOCAST_BUTTON_POSITION.getX())
                && w.getY() == (defensive ? DEFENSIVE_AUTOCAST_BUTTON_POSITION.getY() : REGULAR_AUTOCAST_BUTTON_POSITION.getY())).findFirst();
        if (button.isPresent() && button.get().isVisible()) {
            return new InteractEvent(context, button.get(), "Choose spell").executed();
        }
        return false;
    }

    private boolean isAutocastPanelOpen() {
        Widget panel = context.getWidgets().first(w -> w != null && w.getText() != null && w.getText().equals("Select a Combat Spell"));
        return panel != null && panel.isVisible();
    }

    public boolean isAutocasting() {
        return context.getVarps().getVarp(108) != 0;
    }

    public boolean autocastSpell(StandardSpellbook spellbook, boolean defensive) throws InterruptedException {
        if (isAutocastPanelOpen()) {
            System.out.println("Autocast panel open!");
            if (new WidgetInteractEvent(context, w -> w != null && w.isVisible() && w.hasAction(spellbook.getSpellName()), spellbook.getSpellName()).executed()) {
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
        int currHealth = context.getClient().getSkillBoosted(Skill.HITPOINTS);
        int maxHealth = context.getClient().getSkillReal(Skill.HITPOINTS);
        return ((currHealth * 100) / maxHealth);
    }

    public void walkHere(Tile pos) throws InterruptedException {
        if (new InteractEvent(context, pos, "Walk here").executed())
            System.out.println("Walk => " + pos.toString());
    }

    public int getQuantity(ItemContainer container, String name) {
        return (int) container.getAmount(name);
    }

    public int getQuantity(ItemContainer container, int id) {
        return (int) container.getAmount(id);
    }
}
