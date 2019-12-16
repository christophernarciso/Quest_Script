package org.quester.questrequirements;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.entities.GameObject;
import org.quantumbot.api.entities.GroundItem;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;
import org.quantumbot.enums.Skill;
import org.quantumbot.events.WalkEvent;
import org.quantumbot.interfaces.Logger;
import org.quester.questevents.questutil.QuestContext;
import org.quester.questrequirements.imp.agility.AgilityObstacle;
import org.quester.questrequirements.imp.agility.Course;
import org.quester.questrequirements.imp.agility.CourseType;

import java.util.LinkedList;

public class AgilityRequirementEvent extends QuestContext implements Logger {

    private LinkedList<AgilityObstacle> obstacles;
    private Area WILDERNESS_COURSE_FAILSAFE_ROOM, SEERS_FIRST_FLOOR_FAILSAFE_ROOM, BARBARIAN_TRAP_FAILSAFE_ROOM, BARBARIAN_TRAP2_FAILSAFE_ROOM, LAST_OBSTACLE_AREA;
    private Tile startingTile, wildernessFailsafeTile;
    private Course course;
    private boolean loot, wildernessFailsafe, seersFailsafe, barbarianFailsafe;
    private int eatAtPercent, isStamina = 1048576, isNotStamina = 0, staminaConfig = 638, endLevel = 0;

    public AgilityRequirementEvent(QuantumBot context, int levelRequirement) {
        super(context);
        this.endLevel = levelRequirement;
    }

    @Override
    public void onStart() {
        this.course = getBestCourse(getBot().getClient().getSkillReal(Skill.AGILITY));

        if (this.course.getCourse() == CourseType.WILDERNESS_COURSE) {
            this.WILDERNESS_COURSE_FAILSAFE_ROOM = new Area(2987, 10339, 3009, 10369, 0);
            this.wildernessFailsafe = true;
        }
        if (this.course.getCourse() == CourseType.SEERS) {
            this.SEERS_FIRST_FLOOR_FAILSAFE_ROOM = new Area(2721, 3497, 2730, 3490, 1);
            this.seersFailsafe = true;
        }
        if (this.course.getCourse() == CourseType.BARBARIAN_OUTPOST) {
            this.BARBARIAN_TRAP_FAILSAFE_ROOM = new Area(2533, 3547, 2538, 3545, 0);
            this.BARBARIAN_TRAP2_FAILSAFE_ROOM = new Area(2545, 9956, 2556, 9947, 0);
            this.barbarianFailsafe = true;
        }

        LAST_OBSTACLE_AREA = obstacles.getLast().getArea();

        eatAtPercent = 50;
        debug("Eat at: " + eatAtPercent + "%");
    }

    @Override
    public void step() throws InterruptedException {
        int current = getBot().getClient().getSkillReal(Skill.AGILITY);

        if (current >= endLevel && myPosition().getPlane() == 0)
            setComplete();

        if (ourHealthPercent() <= eatAtPercent) {
            if (getBot().getInventory().contains(item -> item != null && item.hasAction("Eat"))) {
                if (interactInventory(item -> item != null && item.hasAction("Eat"), "Eat")) {
                    sleepUntil(2000, () -> getBot().getPlayers().getLocal().getAnimation() == 829);
                }
            }
        }

        if (getBot().getDialogues().inDialogue())
            getDialogue().execute();

        GameObject nextTarget;
        for (final AgilityObstacle obstacle : this.obstacles) {
            if (!obstacle.getArea().contains(getBot().getPlayers().getLocal())) continue;
            if (this.onArea(obstacle.getArea())) {
                nextTarget = obstacle.asTarget();
                if (nextTarget != null) {
                    if (course.getCourse() == CourseType.BARBARIAN_OUTPOST) {
                        if (BARBARIAN_TRAP_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal())) {
                            debug("Failsafe: Walking back in front of net.");
                            if (!new WalkEvent(getBot(), new Tile(2540, 3546, 0)).setAccuracy(0).executed())
                                return;
                        }
                    }

                    int lastHealth = ourHealthPercent();
                    int lastXP = getBot().getClient().getSkillExp(Skill.AGILITY);
                    if (getInteractEvent(nextTarget, obstacle.getAction()).executed()) {
                        sleepUntil(20000, 600, () -> getBot().getClient().getSkillExp(Skill.AGILITY) > lastXP
                                && !obstacle.getArea().contains(myPlayer()) && !myPlayer().isMoving() && !myPlayer().isAnimating()
                                || getBot().getDialogues().inDialogue() || ourHealthPercent() < lastHealth);
                        if (wildernessFailsafe)
                            wildernessFailsafeTile = obstacle.getName().equals("Log balance") ? new Tile(3002, 3946, 0) : new Tile(3005, 3951, 0);
                        if (LAST_OBSTACLE_AREA.contains(myPosition())) {
                            this.course = getBestCourse(current);
                            this.obstacles.clear();
                            this.obstacles = course.getObstacles(getBot());
                            this.startingTile = course.tile();
                            LAST_OBSTACLE_AREA = obstacles.getLast().getArea();
                        }
                    }
                }
            }
            return;
        }

        checkStamina();
        onLost();
        sleep(700);
    }

    private void onLost() throws InterruptedException {
        if (getBot().getPlayers().getLocal().getTile().getPlane() != 0 ||
                seersFailsafe && SEERS_FIRST_FLOOR_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal()) || getBot().getPlayers().getLocal().isMoving()
                || getBot().getPlayers().getLocal().isAnimating()) {
            if (seersFailsafe && SEERS_FIRST_FLOOR_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal()))
                getWeb(startingTile).execute();
        } else {
            if (wildernessFailsafe) {
                if (WILDERNESS_COURSE_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal())) {
                    debug("Failsafe: walking to " + wildernessFailsafeTile);
                    getWeb(wildernessFailsafeTile).execute();
                }
            } else if (barbarianFailsafe) {
                if (BARBARIAN_TRAP2_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal())) {
                    debug("Failsafe: leaving trap room");
                    GameObject ladder = getBot().getGameObjects().closest(o -> o != null && o.hasAction("Climb-up") && o.getName().equals("Ladder"));
                    if (ladder != null && getInteractEvent(ladder, "Climb-up").executed())
                        sleepUntil(3000, () -> !BARBARIAN_TRAP2_FAILSAFE_ROOM.contains(getBot().getPlayers().getLocal()));
                }
            } else {
                getWeb(startingTile).setDestinationAccuracy(0).execute();
            }
        }
    }

    private boolean onArea(Area area) throws InterruptedException {
        if (!this.loot)
            return true;

        GroundItem markOfGrace = getBot().getGroundItems().closest(groundItem -> groundItem != null && area.contains(groundItem)
                && groundItem.getName().equals("Mark of grace"));

        if (markOfGrace != null && area.contains(getBot().getPlayers().getLocal())) {
            debug("Mark of Grace: Present");
            if (!getBot().getInventory().isFull() || getBot().getInventory().contains(i -> i != null && i.hasName("Mark of grace"))) {
                if (getInteractEvent(markOfGrace, "Take").executed())
                    sleepUntil(3000, () -> !markOfGrace.exists());
                return true;
            } else {
                if (getBot().getInventory().contains(item -> item != null && item.hasAction("Eat"))) {
                    if (interactInventory(item -> item != null && item.hasAction("Eat"), "Eat")) {
                        sleepUntil(2000, () -> getBot().getPlayers().getLocal().getAnimation() == 829);
                    }
                } else loot = false;
            }
        }

        return true;
    }

    private void checkStamina() throws InterruptedException {
        if (getBot().getInventory().contains(i -> i != null && i.hasName("Stamina potion")) && getBot().getSettings().getRunEnergy() < 60
                && getBot().getVarps().getVarp(staminaConfig) == isNotStamina) {
            if (interactInventory(i -> i != null && i.hasName("Stamina potion"), "Drink")) {
                sleepUntil(2000, () -> getBot().getVarps().getVarp(staminaConfig) == isStamina);
            }
        }
    }

    public Course getBestCourse(int lvl) {
        Course next;
        if (lvl < 10)
            next = new Course(CourseType.GNOME);
        else if (lvl < 20)
            next = new Course(CourseType.DRAYNOR);
        else if (lvl < 30)
            next = new Course(CourseType.AL_KHARID);
        else if (lvl < 50)
            next = new Course(CourseType.VARROCK);
        else if (lvl < 60)
            next = new Course(CourseType.FALADOR);
        else
            next = new Course(CourseType.SEERS);

        this.startingTile = next.tile();
        this.obstacles = next.getObstacles(getBot());
        this.loot = true;

        return next;
    }
}
