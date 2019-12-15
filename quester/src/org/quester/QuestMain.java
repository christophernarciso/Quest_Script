package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.f2p.*;
import org.quester.questevents.p2p.*;
import org.quester.questrequirements.PrayerRequirementEvent;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Quest")
public class QuestMain extends Script implements Logger {

    private boolean starterAccountMode = true, allFreeToPlayMode = false, avasReady = false, fungusReady = false, ldkready = false;

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
                    new ErnestTheChickenEvent(getBot()),
                    new GoblinDiplomacyEvent(getBot()),
                    new RomeoJulietEvent(getBot())
            ).executed();
            allFreeToPlayMode = false;
        } else if (starterAccountMode) {
            new DeathPlateauEvent(getBot()).then(
                    new ImpCatcherEvent(getBot()),
                    new WitchPotionEvent(getBot()),
                    new WitchHouseEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new VampireSlayerEvent(getBot()),
                    new RestlessGhostEvent(getBot()),
                    new PriestInPerilEvent(getBot()),
                    new PrayerRequirementEvent(getBot()),
                    new PlagueCityEvent(getBot()),
                    new FightArenaEvent(getBot()),
                    new TreeGnomeVillageEvent(getBot())
            ).executed();
            starterAccountMode = false;
        } else if (fungusReady) {
            new ImpCatcherEvent(getBot()).then(
                    new WitchPotionEvent(getBot()),
                    new WitchHouseEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new RestlessGhostEvent(getBot()),
                    new PriestInPerilEvent(getBot()),
                    new NatureSpiritEvent(getBot()),
                    new PrayerRequirementEvent(getBot())
            ).executed();
            fungusReady = false;
        } else if (avasReady) {
            new RestlessGhostEvent(getBot()).then(
                    new ErnestTheChickenEvent(getBot()),
                    new WaterfallEvent(getBot()),
                    new PriestInPerilEvent(getBot()),
                    new DwarfCannonEvent(getBot()),
                    new PlagueCityEvent(getBot()),
                    new AnimalMagnetismEvent(getBot())
            ).executed();
            avasReady = false;
        } else if (ldkready) {
            new ImpCatcherEvent(getBot()).then(
                    new WitchPotionEvent(getBot()),
                    new WitchHouseEvent(getBot())
            ).executed();
            ldkready = false;
        } else {
            getBot().stop();
        }
        sleep(1000);
    }

    @Override
    public void onExit() {

    }

}
