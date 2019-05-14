package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.f2p.*;
import org.quester.questevents.p2p.*;
import org.quester.questutil.HelperMethods;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger {

    private HelperMethods helperMethods;
    private boolean starterAccountMode = true, allFreeToPlayMode = true, avasReady = true;

    @Override
    public void onStart() {
        helperMethods = new HelperMethods(getBot());
    }

    @Override
    public void onLoop() throws InterruptedException {
        info("bank cached: " + getBot().getBank().isCached());
        // Cache the bank before executing events.
        if (!getBot().getBank().isCached() && helperMethods.myPosition().getY() < 9000)
            new BankOpenEvent(getBot()).execute();
        else if (allFreeToPlayMode) {
            new CookAssistantEvent(getBot(), helperMethods).then(
                    new DoricEvent(getBot(), helperMethods),
                    new ImpCatcherEvent(getBot(), helperMethods),
                    new WitchPotionEvent(getBot(), helperMethods),
                    new RuneMysteriesEvent(getBot(), helperMethods),
                    new SheepShearerEvent(getBot(), helperMethods),
                    new RestlessGhostEvent(getBot(), helperMethods),
                    new ErnestTheChickenEvent(getBot(), helperMethods)
            ).executed();
            allFreeToPlayMode = false;
        }else if (starterAccountMode) {
            new DeathPlateauEvent(getBot(), helperMethods).then(
                    new ImpCatcherEvent(getBot(), helperMethods),
                    new WitchPotionEvent(getBot(), helperMethods),
                    new WitchHouseEvent(getBot(), helperMethods),
                    new WaterfallEvent(getBot(), helperMethods),
                    new VampireSlayerEvent(getBot(), helperMethods)
            ).executed();
            starterAccountMode = false;
        } else if (avasReady) {
            new RestlessGhostEvent(getBot(), helperMethods).then(
                    new ErnestTheChickenEvent(getBot(), helperMethods),
                    new WaterfallEvent(getBot(), helperMethods),
                    new PriestInPerilEvent(getBot(), helperMethods),
                    new DwarfCannonEvent(getBot(), helperMethods),
                    new PlagueCityEvent(getBot(), helperMethods)
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
