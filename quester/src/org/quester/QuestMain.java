package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.api.map.InstancedArea;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.f2p.*;
import org.quester.questevents.p2p.DeathPlateauEvent;
import org.quester.questevents.p2p.WaterfallEvent;
import org.quester.questevents.p2p.WitchHouseEvent;
import org.quester.questutil.HelperMethods;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger{

    private HelperMethods helperMethods;
    private boolean sevenQuestPointMode, allFreeToPlayMode;
    private boolean starterAccountMode = true, f2pMode = false; // mode2,......

    @Override
    public void onStart() {
        helperMethods = new HelperMethods(getBot());
    }

    @Override
    public void onLoop() throws InterruptedException {
        info("bank cached: " + getBot().getBank().isCached());
        // Cache the bank before executing events.
        if (!getBot().getBank().isCached())
            new BankOpenEvent(getBot()).execute();
        else if (f2pMode)
            new CookAssistantEvent(getBot(), helperMethods).then(
                    new DoricEvent(getBot(),helperMethods),
                    new ImpCatcherEvent(getBot(), helperMethods),
                    new WitchPotionEvent(getBot(), helperMethods),
                    new RuneMysteriesEvent(getBot(), helperMethods),
                    new SheepShearerEvent(getBot(), helperMethods),
                    new RestlessGhostEvent(getBot(), helperMethods),
                    new ErnestTheChickenEvent(getBot(), helperMethods)
            ).executed();
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
