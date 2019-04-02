package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.p2p.DeathPlateauEvent;
import org.quester.questevents.p2p.WaterfallEvent;
import org.quester.questevents.p2p.WitchHouseEvent;
import org.quester.questutil.HelperMethods;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger{

    private HelperMethods helperMethods;
    private boolean sevenQuestPointMode, allFreeToPlayMode;
    private boolean starterAccountMode = true; // mode2,......

    @Override
    public void onStart() {
        helperMethods = new HelperMethods(getBot());
        info("Setting rsbuddy price cache");
        helperMethods.setPriceCache(true);
        info("Cache size: " + helperMethods.getPriceCache().size());
    }

    @Override
    public void onLoop() throws InterruptedException {
        info("bank cached: " + getBot().getBank().isCached());
        // Cache the bank before executing events.
        if (!getBot().getBank().isCached())
            new BankOpenEvent(getBot()).execute();
        else if (starterAccountMode) {
            new DeathPlateauEvent(getBot(), helperMethods).then(
                    new WitchHouseEvent(getBot(), helperMethods),
                    new WaterfallEvent(getBot(), helperMethods)
            ).executed();
        }
        sleep(1000);
    }

    @Override
    public void onExit() {

    }

}
