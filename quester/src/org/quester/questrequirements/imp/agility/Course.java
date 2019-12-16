package org.quester.questrequirements.imp.agility;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.api.map.Area;
import org.quantumbot.api.map.Tile;

import java.util.LinkedList;

public class Course {
    private final CourseType course;

    public Course(final CourseType course){
        this.course = course;
    }

    public LinkedList<AgilityObstacle> getObstacles(QuantumBot ctx) {
        LinkedList<AgilityObstacle> obstacles = new LinkedList<AgilityObstacle>();
        switch (getCourse()) {
            case GNOME:
                obstacles.add(new AgilityObstacle(ctx, "Log balance", "Walk-across", new Area(2491, 3435, 2468, 3440, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Obstacle net", "Climb-over", new Area(2469, 3425, 2480, 3429, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tree branch", "Climb", new Area(2471, 3422, 2476, 3424, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Balancing rope", "Walk-on", new Area(2472, 3421, 2482, 3418, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Tree branch", "Climb-down", new Area(2483, 3418, 2488, 3421, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Obstacle net", "Climb-over", new Area(2490, 3426, 2480, 3414, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Obstacle pipe", "Squeeze-through", new Area(2481, 3427, 2490, 3435, 0)));
                break;

            case DRAYNOR:
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(3100, 3273, 3110, 3285, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3097, 3277, 3102, 3281, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3086, 3272, 3094, 3279, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Narrow wall", "Balance", new Area(3095, 3268, 3085, 3263, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Wall", "Jump-up", new Area(3089, 3256, 3082, 3262, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3097, 3255, 3086, 3250, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Crate", "Climb-down", new Area(3103, 3262, 3096, 3255, 3)));
                break;

            case AL_KHARID:
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(3265, 3186, 3284, 3199, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3272, 3180, 3278, 3276, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Cable", "Swing-across", new Area(3265, 3161, 3272, 3173, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Zip line", "Teeth-grip", new Area(3283, 3176, 3313, 3160, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tropical tree", "Swing-across", new Area(3314, 3167, 3320, 3160, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Roof top beams", "Climb", new Area(3312, 3173, 3318, 3179, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3312, 3180, 3318, 3186, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3298, 3186, 3306, 3193, 3)));
                break;

            case VARROCK:
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(3218, 3409, 3226, 3422, 0))); // 1
                obstacles.add(new AgilityObstacle(ctx, "Clothes line", "Cross", new Area(3211, 3420, 3220, 3409, 3))); // 2
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(3200, 3421, 3209, 3411, 3))); // 3
                obstacles.add(new AgilityObstacle(ctx, "Wall", "Balance", new Area(3183, 3418, 3198, 3407, 1))); // 4
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(new int[][]{
                        { 3200, 3407 },
                        { 3200, 3400 },
                        { 3190, 3400 },
                        { 3190, 3406 }
                }, 3))); // 5
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(new int[][]{
                        { 3212, 3404 },
                        { 3211, 3382 },
                        { 3179, 3380 },
                        { 3180, 3399 },
                        { 3198, 3399 }
                }, 3))); // 6
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(3235, 3406, 3217, 3391, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Ledge", "Hurdle", new Area(3241, 3409, 3236, 3403, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Edge", "Jump-off", new Area(3243, 3418, 3234, 3408, 3)));
                break;

            case BARBARIAN_OUTPOST:
                obstacles.add(new AgilityObstacle(ctx, "Ropeswing", "Swing-on", new Area(2543, 3556, 2553, 3550, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Log balance", "Walk-across", new Area(2542, 3549, 2553, 3542, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Obstacle net", "Climb-over", new Area(2533, 3549, 2542, 3542, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Balancing ledge", "Walk-across", new Area(2535, 3547, 2538, 3545, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Ladder", "Climb-down", new Area(2530, 3548, 2532, 3543, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Crumbling wall", "Climb-over", new Area(new int[][]{
                        { 2537, 3555 },
                        { 2537, 3548 },
                        { 2533, 3548 },
                        { 2533, 3542 },
                        { 2529, 3542 },
                        { 2529, 3550 },
                        { 2528, 3555 },
                        { 2528, 3556 },
                        { 2528, 3557 },
                        { 2536, 3557 },
                        { 2537, 3557 }
                }, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Crumbling wall", "Climb-over", new Area(2537, 3554, 2539, 3552, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Crumbling wall", "Climb-over", new Area(2540, 3554, 2542, 3552, 0)));
                break;

            case CANIFIS:
                obstacles.add(new AgilityObstacle(ctx, "Tall tree", "Climb", new Area(3512, 3492, 3501, 3482, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3502, 3499, 3513, 3488, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3494, 3508, 3507, 3502, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3483, 3508, 3493, 3495, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3471, 3502, 3481, 3488, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Pole-vault", "Vault", new Area(3472, 3489, 3486, 3473, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3489, 3469, 3503, 3478, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3507, 3487, 3517, 3473, 2)));
                break;

            case APE_ATOLL:
                obstacles.add(new AgilityObstacle(ctx, "Stepping stone", "Jump-to", new Area(2757, 2740, 2754, 2746, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tropical tree", "Climb", new Area(2753, 2739, 2751, 2743, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Monkeybars", "Swing Across", new Area(2755, 2739, 2750, 2744, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Skull slope", "Climb-up", new Area(2748, 2740, 2746, 2742, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Rope", "Swing", new Area(new int[][]{
                        { 2744, 2745 },
                        { 2729, 2746 },
                        { 2740, 2732 },
                        { 2744, 2729 },
                        { 2751, 2724 },
                        { 2753, 2724 },
                        { 2755, 2724 },
                        { 2754, 2734 },
                        { 2753, 2737 },
                        { 2747, 2737 },
                        { 2746, 2740 }
                }, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tropical tree", "Climb-down", new Area(new int[][]{
                        { 2755, 2738 },
                        { 2763, 2738 },
                        { 2768, 2727 },
                        { 2769, 2723 },
                        { 2756, 2723 },
                        { 2755, 2723 },
                        { 2754, 2723 },
                        { 2754, 2737 }
                }, 0)));
                break;

            case FALADOR:
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(3033, 3338, 3043, 3347, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3036, 3342, 3040, 3343, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Hand holds", "Cross", new Area(3053, 3351, 3046, 3339, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3046, 3359, 3052, 3354, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3044, 3361, 3050, 3368, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3034, 3361, 3041, 3364, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(3026, 3352, 3029, 3354, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(3022, 3351, 3009, 3360, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Ledge", "Jump", new Area(3022, 3342, 3015, 3350, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Ledge", "Jump", new Area(3014, 3343, 3009, 3348, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Ledge", "Jump", new Area(3014, 3342, 3008, 3334, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Ledge", "Jump", new Area(new int[][]{
                        { 3014, 3334 },
                        { 3012, 3333 },
                        { 3019, 3331 },
                        { 3019, 3335 }
                }, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Edge", "Jump", new Area(3027, 3335, 3019, 3331, 3)));
                break;

            case WILDERNESS_COURSE:
                obstacles.add(new AgilityObstacle(ctx, "Obstacle pipe", "Squeeze-through", new Area(new int[][]{
                        { 3003, 3948 },
                        { 3003, 3939 },
                        { 3000, 3937 },
                        { 3000, 3931 },
                        { 3004, 3931 },
                        { 3007, 3934 },
                        { 3007, 3948 }
                }, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Ropeswing", "Swing-on", new Area(3003, 3949, 3009, 3955, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Stepping stone", "Cross", new Area(new int[][]{
                        { 3009, 3958 },
                        { 3002, 3958 },
                        { 3000, 3959 },
                        { 2997, 3959 },
                        { 2995, 3967 },
                        { 3009, 3967 }
                }, 0))); //2998, 3958, 3008, 3966
                obstacles.add(new AgilityObstacle(ctx, "Log balance", "Walk-across", new Area(new int[][]{
                        { 2996, 3966 },
                        { 2990, 3963 },
                        { 2990, 3953 },
                        { 2994, 3949 },
                        { 2999, 3949 },
                        { 2999, 3943 },
                        { 3003, 3943 },
                        { 3003, 3957 },
                        { 3000, 3959 },
                        { 2997, 3959 },
                        { 2997, 3966 }
                }, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Rocks", "Climb", new Area(new int[][]{
                        { 2988, 3936 },
                        { 2999, 3936 },
                        { 3002, 3939 },
                        { 3003, 3942 },
                        { 3002, 3943 },
                        { 2996, 3943 },
                        { 2996, 3949 },
                        { 2993, 3948 },
                        { 2990, 3946 },
                        { 2988, 3946 }
                }, 0)));
                break;

            case SEERS:
                obstacles.add(new AgilityObstacle(ctx, "Wall", "Climb-up", new Area(2725, 3486, 2736, 3493, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(2719, 3490, 2732, 3498, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(2705, 3488, 2713, 3495, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(2716, 3475, 2708, 3483, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Jump", new Area(2698, 3468, 2717, 3477, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Edge", "Jump", new Area(2690, 3457, 2704, 3468, 2)));
                break;

            case POLLNIVNEACH:
                obstacles.add(new AgilityObstacle(ctx, "Basket", "Climb-on", new Area(3346, 2957, 3360, 2970, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Market stall", "Jump-on", new Area(3346, 2971, 3352, 2962, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Banner", "Grab", new Area(3359, 2980, 3352, 2973, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(3364, 2979, 3360, 2974, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Tree", "Jump-to", new Area(3371, 2978, 3366, 2974, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(3365, 2982, 3369, 2986, 1)));
                obstacles.add(new AgilityObstacle(ctx, "Monkeybars", "Cross", new Area(new int[][]{
                        { 3354, 2991 },
                        { 3362, 2991 },
                        { 3362, 2989 },
                        { 3370, 2989 },
                        { 3370, 2979 },
                        { 3353, 2979 }
                }, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Tree", "Jump-on", new Area(3355, 2998, 3370, 2989, 2)));
                obstacles.add(new AgilityObstacle(ctx, "Drying line", "Jump-to", new Area(3355, 2999, 3368, 3006, 2)));
                break;

            case RELLEKA:
                obstacles.add(new AgilityObstacle(ctx, "Rough wall", "Climb", new Area(2629, 3671, 2620, 3681, 0)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(2620, 3669, 2628, 3677, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(2623, 3656, 2615, 3668, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Leap", new Area(2632, 3657, 2625, 3651, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Gap", "Hurdle", new Area(2646, 3655, 2638, 3649, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Tightrope", "Cross", new Area(2652, 3665, 2642, 3656, 3)));
                obstacles.add(new AgilityObstacle(ctx, "Pile of fish", "Jump-in", new Area(2669, 3687, 2652, 3665, 3)));
                break;

        }
        return obstacles;
    }

    public Tile tile() {
        Tile getTile = null;
        switch (getCourse()) {
            case GNOME:
                getTile = new Tile(2475, 3438, 0);
                break;
            case DRAYNOR:
                getTile = new Tile(3104, 3279, 0);
                break;
            case AL_KHARID:
                getTile = new Tile(3273, 3195, 0);
                break;
            case VARROCK:
                getTile = new Tile(3222, 3414, 0);
                break;
            case BARBARIAN_OUTPOST:
                getTile = new Tile(2551, 3554, 0);
                break;
            case CANIFIS:
                getTile = new Tile(3508, 3487, 0);
                break;
            case APE_ATOLL:
                getTile = new Tile(2755, 2742, 0);
                break;
            case FALADOR:
                getTile = new Tile(3037, 3340, 0);
                break;
            case WILDERNESS_COURSE:
                getTile = new Tile(3004, 3937, 0);
                break;
            case SEERS:
                getTile = new Tile(2729, 3488, 0);
                break;
            case POLLNIVNEACH:
                getTile = new Tile(3352, 2961, 0);
                break;
            case RELLEKA:
                getTile = new Tile(2625, 3677, 0);
                break;
        }
        return getTile;
    }

    public CourseType getCourse() {
        return course;
    }

}
