package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.f2p.*;
import org.quester.questevents.p2p.*;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger {

    private boolean starterAccountMode = false, allFreeToPlayMode = false, avasReady = false, fungusReady = true;

    @Override
    public void onStart() {

    }

    @Override
    public void onLoop() throws InterruptedException {
        info("bank cached: " + getBot().getBank().isCached());
        // Cache the bank before executing events.
        if (!getBot().getBank().isCached())
            new BankOpenEvent(getBot()).execute();
        else if (allFreeToPlayMode) {
            new CookAssistantEvent(getBot()).then(
                    new DoricEvent(getBot()),
                    new ImpCatcherEvent(getBot()),
                    new WitchPotionEvent(getBot()),
                    new RuneMysteriesEvent(getBot()),
                    new SheepShearerEvent(getBot()),
                    new RestlessGhostEvent(getBot()),
                    new ErnestTheChickenEvent(getBot())
            ).executed();
            allFreeToPlayMode = false;
        } else if (fungusReady) {
            new ImpCatcherEvent(getBot()).then(
                    new WitchPotionEvent(getBot()),
                    new WitchHouseEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new RestlessGhostEvent(getBot()),
                    new PriestInPerilEvent(getBot()),
                    new NatureSpiritEvent(getBot())
            ).executed();
        } else if (starterAccountMode) {
            new DeathPlateauEvent(getBot()).then(
                    new ImpCatcherEvent(getBot()),
                    new WitchPotionEvent(getBot()),
                    new WitchHouseEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new VampireSlayerEvent(getBot())
            ).executed();
            starterAccountMode = false;
        } else if (avasReady) {
            new AnimalMagnetismEvent(getBot()).executed();
            new RestlessGhostEvent(getBot()).then(
                    new ErnestTheChickenEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new PriestInPerilEvent(getBot()),
                    new DwarfCannonEvent(getBot()),
                    new PlagueCityEvent(getBot()),
                    new AnimalMagnetismEvent(getBot())
            ).executed();
            avasReady = false;
        } else {
            getBot().stop();
        }
        sleep(1000);
    }

    @Override
    public void onExit() {

    }

}
