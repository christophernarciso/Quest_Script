package org.quester.questutil;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.containers.Item;
import org.quantumbot.api.entities.*;
import org.quantumbot.api.interfaces.Interactable;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.api.widgets.Widget;
import org.quantumbot.enums.Skill;
import org.quantumbot.enums.Tab;
import org.quantumbot.enums.spells.MagicSpell;
import org.quantumbot.enums.spells.StandardSpellbook;
import org.quantumbot.events.DialogueEvent;
import org.quantumbot.events.TabEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.ge.GEEvent;
import org.quantumbot.events.interactions.*;
import org.quantumbot.utils.StringUtils;
import org.quester.otherutil.ExchangeItem;
import org.quester.otherutil.json.JsonObject;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class HelperMethods {

    private QuantumBot context;
    private Map<String, ExchangeItem> priceCache = new HashMap<>();
    private boolean grabbedItems;
    private final Point DEFENSIVE_AUTOCAST_BUTTON_POSITION = new Point(650, 280);
    private final Point REGULAR_AUTOCAST_BUTTON_POSITION = new Point(649, 303);

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
        return new WebWalkEvent(context, tile);
    }

    public InteractEvent getInteractEvent(Interactable interactable, String... actions) {
        return new InteractEvent(context, interactable, actions);
    }

    public DialogueEvent getDialogue(String... options) {
        return options == null ? new DialogueEvent(context) : new DialogueEvent(context, options);
    }

    public BankEvent getBankEvent(HashMap<String, Integer> req){
        BankEvent be = new BankEvent(context);
        be.stopIfUnmetReqs(true);

        for (String key: req.keySet())
            be.addReq(req.get(key), req.get(key), key);

       return be;
    }
    
    public GEEvent getBuyableEvent(HashMap<String, Integer> req){
        GEEvent ge = new GEEvent(context);
        int totalCoins = (int) (context.getInventory().getAmount("Coins")
                        + context.getBank().getAmount("Coins"));
        int expectedTotal = 0;
        int originalPrice, price;

        for (String key: req.keySet()) {
            if (!getPriceCache().containsKey(key)) continue;
            if (key.contains("~")) {
                List<String> expanded = StringUtils.expandItemName(key);
                key = StringUtils.expandItemName(key).get(expanded.size() - 1);
                System.out.println("Expanded: " + key);
            }
            originalPrice = getPriceCache().get(key).getBuyAverage();
            // Buy over 30% value for instant transactions
            price = originalPrice > 0 ? (int) (originalPrice + (originalPrice * .30)) : 5000;
            expectedTotal += price;

            System.out.println("Adding " + key + " x" + req.get(key) + " to buy list");
            ge.buy(req.get(key), price, key);
        }

        if (totalCoins < expectedTotal)
            return null;
        
        return ge;
    }

    public boolean talkTo(String npcName) throws InterruptedException {
        return new NPCInteractEvent(context, npcName, "Talk-to").executed();
    }

    public boolean interactNPC(String npcName, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcName, actions).executed();
    }

    public boolean interactNPC(Predicate<NPC> npcPredicate, String... actions) throws InterruptedException {
        return new NPCInteractEvent(context, npcPredicate, actions).executed();
    }

    public boolean interactObject(String objectName, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectName, actions).executed();
    }

    public boolean interactObject(Predicate<GameObject> objectPredicate, String... actions) throws InterruptedException {
        return new ObjectInteractEvent(context, objectPredicate, actions).executed();
    }

    public boolean interactGroundItem(String groundItemName, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemName, actions).executed();
    }

    public boolean interactGroundItem(Predicate<GroundItem> groundItemPredicate, String... actions) throws InterruptedException {
        return new GroundItemInteractEvent(context, groundItemPredicate, actions).executed();
    }

    public boolean interactInventory(String itemName, String... actions) throws InterruptedException {
        return new InventoryInteractEvent(context, itemName, actions).executed();
    }

    public boolean interactInventory(Predicate<Item> itemPredicate, String... actions) throws InterruptedException {
        return new InventoryInteractEvent(context, itemPredicate, actions).executed();
    }

    public boolean inArea(Area area) {
        return area.contains(myPlayer());
    }

    public boolean atExactPosition(Tile tile) {
        return myPlayer().getTile().equals(tile);
    }

    public boolean hasQuestItemsBeforeStarting(HashMap<String, Integer> list, boolean bank) {
        for (String key : list.keySet()) {
            if (context.getBank().getAmount(key) < list.get(key) && bank)
                return false;
            if (context.getInventory().getAmount(key) < list.get(key))
                return false;
        }
        return true;
    }

    public Map<String, ExchangeItem> getPriceCache() {
        return this.priceCache;
    }

    public void setPriceCache(boolean forceNewCache) {
        this.priceCache = getJsonPriceCache(forceNewCache);
    }

    private Map<String, ExchangeItem> getJsonPriceCache(boolean forceNewCache) {
        Map<String, ExchangeItem> cache = new HashMap<>();

        try {
            java.io.File cacheFile = new java.io.File(System.getProperty("user.home") + File.separator + "QuantumBot"
                    + File.separator + "data" + File.separator + "quester" + File.separator + "summary.json");
            BufferedReader jsonFile;
            JsonObject priceJSON = null;

            if (cacheFile.exists() && !forceNewCache) {
                System.out.println("Loading from cached file");
                jsonFile = new BufferedReader(new InputStreamReader(cacheFile.toURI().toURL().openStream()));
                priceJSON = JsonObject.readFrom(jsonFile.readLine());
            } else if (cacheFile.exists() && forceNewCache) {
                System.out.println("File exists and now deleting.");
                if (cacheFile.delete()) {
                    System.out.println("Deleted file");
                    URL url = new URL("https://rsbuddy.com/exchange/summary.json");
                    System.out.println("Created new file in directory");

                    BufferedInputStream in = null;
                    FileOutputStream fout = null;
                    try {
                        in = new BufferedInputStream(url.openStream());
                        fout = new FileOutputStream(cacheFile);

                        final byte data[] = new byte[1024];
                        int count;
                        while ((count = in.read(data, 0, 1024)) != -1) {
                            fout.write(data, 0, count);
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (fout != null) {
                            fout.close();
                        }
                    }

                    System.out.println("Loading from cached file");
                    jsonFile = new BufferedReader(new InputStreamReader(cacheFile.toURI().toURL().openStream()));
                    priceJSON = JsonObject.readFrom(jsonFile.readLine());
                }
            } else {
                java.io.File directory = new java.io.File(System.getProperty("user.home") + File.separator + "QuantumBot"
                        + File.separator + "data" + File.separator + "dragons" + File.separator);
                if (directory.mkdirs() || directory.exists()) {
                    URL url = new URL("https://rsbuddy.com/exchange/summary.json");
                    System.out.println("Created new file in directory");

                    BufferedInputStream in = null;
                    FileOutputStream fout = null;
                    try {
                        in = new BufferedInputStream(url.openStream());
                        fout = new FileOutputStream(cacheFile);

                        final byte data[] = new byte[1024];
                        int count;
                        while ((count = in.read(data, 0, 1024)) != -1) {
                            fout.write(data, 0, count);
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (fout != null) {
                            fout.close();
                        }
                    }

                    System.out.println("Loading from cached file");
                    jsonFile = new BufferedReader(new InputStreamReader(cacheFile.toURI().toURL().openStream()));
                    priceJSON = JsonObject.readFrom(jsonFile.readLine());
                }
            }

            if (priceJSON != null) {
                for (JsonObject.Member aPriceJSON : priceJSON) {
                    JsonObject itemJSON = priceJSON.get(aPriceJSON.getName()).asObject();
                    cache.put(itemJSON.get("name").asString(), new ExchangeItem(itemJSON.get("name").asString(), itemJSON.get("id").asInt(), itemJSON.get("sell_average").asInt(),
                            itemJSON.get("overall_average").asInt(), itemJSON.get("buy_average").asInt()));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to grab item price cache!");
        }
        return cache;
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
        return context.getClient().getVarp(108) != 0;
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
}
