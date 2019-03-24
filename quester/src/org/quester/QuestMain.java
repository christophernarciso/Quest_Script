package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.DeathPlateauEvent;
import org.quester.questevents.WitchHouseEvent;
import org.quester.questutil.HelperMethods;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger{

    private HelperMethods helperMethods;
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

        /*if (starterAccountMode)
            new DeathPlateauEvent(getBot(), helperMethods)
                    .then(
                            new WitchHouseEvent(getBot(), helperMethods)
                    ).execute();
                    */
        else if (starterAccountMode)
            new DeathPlateauEvent(getBot(), helperMethods).execute();

        sleep(1000);
    }

    @Override
    public void onExit() {

    }

}
