package Mob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// fixes Feature Envy from GamePanel
// fixes refactor 6
public class MobSpawner {

    private static final Random random = new Random();

    // spawn random enemies from 1-5
    public static List<Mob> spawnEnemies(int partyCumLevel) {
        int numMobs = 1 + random.nextInt(5);

        int maxEnemyCumLevel = Math.max(numMobs, partyCumLevel);
        int minEnemyCumLevel = Math.max(numMobs, partyCumLevel - 10);
        int enemyCumLevel = minEnemyCumLevel
                + random.nextInt(Math.max(1, maxEnemyCumLevel - minEnemyCumLevel + 1));

        int[] mobLevels = distributeLevels(numMobs, enemyCumLevel);

        List<Mob> mobs = new ArrayList<>();
        for (int lvl : mobLevels) {
            mobs.add(createMob(lvl));
        }
        return mobs;
    }

    // make sure that the levels aren't ever higher than the hero party
    private static int[] distributeLevels(int numMobs, int enemyCumLevel) {
        int[] levels = new int[numMobs];
        Arrays.fill(levels, 1);
        int remaining = enemyCumLevel - numMobs;
        for (int i = 0; i < remaining; i++) {
            levels[random.nextInt(numMobs)]++;
        }
        for (int i = 0; i < numMobs; i++) {
            levels[i] = Math.min(10, levels[i]);
        }
        return levels;
    }

    // scale mob with levels
    private static NormalMob createMob(int level) {
        double hp    = 15 + level * 10;
        int    power = 4  + level * 2;
        int    def   = level - 1;
        int    xp    = 50 * level;
        int    gold  = 75 * level;
        return new NormalMob(hp, power, def, level, xp, gold);
    }
}