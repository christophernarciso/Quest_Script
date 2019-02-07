package org.quester;

import org.quantumbot.api.Script;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.enums.Quest;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.QUEST_DEATH_PLATEAU;
import org.quester.questutil.HelperMethods;

@ScriptManifest(description = "", author = "N I X", image = "", version = 1, name = "Questing")
public class QuestMain extends Script implements Logger{

    private HelperMethods helperMethods;
    private boolean mode1; // mode2,......

    @Override
    public void onStart() {
        helperMethods = new HelperMethods(getBot());
        info("Setting rsbuddy price cache");
        helperMethods.setPriceCache(true);
        info("Cache size: " + helperMethods.getPriceCache().size());
    }

    @Override
    public void onLoop() throws InterruptedException {
        // Cache the bank before executing events.
        if (!getBot().getBank().isCached())
            new BankOpenEvent(getBot()).execute();

        if (!getBot().getQuests().isComplete(Quest.DEATH_PLATEAU))
            new QUEST_DEATH_PLATEAU(getBot(), helperMethods).execute();

    }

    @Override
    public void onExit() {

    }

}
