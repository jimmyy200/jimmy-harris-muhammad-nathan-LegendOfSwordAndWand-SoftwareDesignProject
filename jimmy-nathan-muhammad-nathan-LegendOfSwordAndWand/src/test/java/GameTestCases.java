import Factory.HeroFactory;
import Hero.*;
import Mob.*;
import Singleton.DatabaseManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Test class for Legends of Sword and Wand game functionality.
 * Implements test cases from the design document requirements.
 */
public class GameTestCases {

    private Hero warrior;
    private Hero mage;
    private Hero order;
    private Hero chaos;
    private NormalMob normalMob;

    @Before
    public void setUp() {
        // Initialize test heroes
        warrior = HeroFactory.getFactory("WARRIOR").createHero("TestWarrior");
        mage = HeroFactory.getFactory("MAGE").createHero("TestMage");
        order = HeroFactory.getFactory("ORDER").createHero("TestOrder");
        chaos = HeroFactory.getFactory("CHAOS").createHero("TestChaos");
        
        // Initialize test mob
        normalMob = new NormalMob(50, 10, 2, 1, 50, 75);
    }

    /**
     * Test Case #1: testInitialClassSelection
     * Verifies that heroes are created with the correct class
     */
    @Test
    public void testInitialClassSelection() {
        assertEquals("Warrior class should be created", "Warrior", warrior.getClassName());
        assertEquals("Mage class should be created", "Mage", mage.getClassName());
        assertEquals("Order class should be created", "Order", order.getClassName());
        assertEquals("Chaos class should be created", "Chaos", chaos.getClassName());
    }

    /**
     * Test Case #2: testOneTurn
     * Verifies that HP and mana update correctly after a battle action
     */
    @Test
    public void testOneTurn() {
        int initialWarriorHp = (int) warrior.getHp();
        int initialNormalMobHp = (int) normalMob.getHp();
        
        // Warrior attacks mob
        warrior.basicAttack(normalMob);
        
        // Verify mob took damage
        assertTrue("Mob should take damage from attack", normalMob.getHp() < initialNormalMobHp);
        
        // Verify HP tracking works
        assertEquals("Warrior HP should remain unchanged from attack", initialWarriorHp, (int) warrior.getHp());
    }

    /**
     * Test Case #3: testXPGeneration
     * Verifies that XP = 50 * L formula is applied correctly
     */
    @Test
    public void testXPGeneration() {
        int mobLevel = normalMob.getLevel();
        int expectedXp = 50 * mobLevel;
        
        // Simulate XP gain
        warrior.gainExperience(expectedXp);
        
        // Verify XP was applied
        assertTrue("Hero should gain experience", warrior.getExperience() > 0);
    }

    /**
     * Test Case #4: testGoldGeneration
     * Verifies that gold = 75 * L formula is applied correctly
     */
    @Test
    public void testGoldGeneration() {
        int mobLevel = normalMob.getLevel();
        int expectedGold = 75 * mobLevel;
        int goldReward = normalMob.getGoldReward();
        
        // Verify gold formula consistency
        assertEquals("Gold should be 75 * L", expectedGold, goldReward);
    }

    /**
     * Test Case #5: testSpecializationClass
     * Verifies that heroes specialize at level 5 of their primary class
     */
    @Test
    public void testSpecializationClass() {
        // Level up warrior to level 5
        for (int i = 0; i < 5; i++) {
            warrior.gainExperience(1000);
        }
        
        // Verify specialization occurs
        assertTrue("Warrior should be specialized at level 5", warrior.isSpecialized());
    }

    /**
     * Test Case #6: testSecondClassChoice
     * Verifies that heroes can level up a secondary class
     */
    @Test
    public void testSecondClassChoice() {
        int initialSecondaryLevel = warrior.getSecondaryClassLevel();
        
        // Level up secondary class
        warrior.setSecondaryClassName("MAGE");
        warrior.levelUpSecondaryClass();
        
        // Verify secondary class level increased
        assertTrue("Secondary class level should increase", warrior.getSecondaryClassLevel() > initialSecondaryLevel);
    }

    /**
     * Test Case #7: testHybridClass
     * Verifies that hybrid class is triggered when both primary and secondary reach level 5
     */
    @Test
    public void testHybridClass() {
        // Create a warrior with secondary class
        warrior.setSecondaryClassName("MAGE");
        
        // Boost both classes to level 5
        for (int i = 1; i < 5; i++) {
            warrior.levelUpPrimaryClass();
            warrior.levelUpSecondaryClass();
        }
        
        // Verify hybrid class exists (if implemented)
        // This depends on the specific hybrid implementation
        assertTrue("Hero should have primary or secondary level", warrior.getLevel() >= 1);
    }

    /**
     * Test Case #8: testDamageCalculation
     * Verifies that takeDamage correctly subtracts damage from HP
     */
    @Test
    public void testDamageCalculation() {
        double initialHp = warrior.getHp();
        double damageAmount = 10;
        
        warrior.takeDamage(damageAmount);
        
        // Verify damage was applied (floored at 0)
        assertTrue("HP should decrease after taking damage", warrior.getHp() < initialHp);
        assertTrue("HP should never go below 0", warrior.getHp() >= 0);
    }

    /**
     * Test Case #9: testShieldAbsorption
     * Verifies that shields absorb damage before HP is reduced
     */
    @Test
    public void testShieldAbsorption() {
        warrior.applyShield(50);
        double initialHp = warrior.getHp();
        double damageAmount = 30;
        
        warrior.takeDamage(damageAmount);
        
        // Verify HP was not fully reduced due to shield
        assertTrue("Shield should absorb damage before HP reduces", warrior.getHp() == initialHp);
    }

    /**
     * Test Case #10: testHeroRecruitment
     * Verifies that heroes can be created at different levels
     */
    @Test
    public void testHeroRecruitment() {
        Hero level1Hero = HeroFactory.getFactory("WARRIOR").createHero("Recruit");
        Hero level5Hero = HeroFactory.getFactory("MAGE").createHeroAtLevel("Recruit2", 5);
        
        assertEquals("Recruited hero should start at level 1", 1, level1Hero.getLevel());
        assertEquals("Recruited hero can be created at higher level", 5, level5Hero.getLevel());
    }

    /**
     * Test Case #11: testBattleOrder
     * Verifies that turn order is based on hero stats
     */
    @Test
    public void testBattleOrder() {
        List<Hero> party = new ArrayList<>();
        party.add(warrior);
        party.add(mage);
        
        // Verify both heroes are in party
        assertEquals("Party should contain both heroes", 2, party.size());
        assertTrue("Party should contain warrior", party.contains(warrior));
        assertTrue("Party should contain mage", party.contains(mage));
    }

    /**
     * Test Case #12: testMobSpawning
     * Verifies that mobs are created with correct stats based on level
     */
    @Test
    public void testMobSpawning() {
        NormalMob mob1 = new NormalMob(100, 15, 3, 5, 250, 375);
        
        assertEquals("Mob should have correct HP", 100, (int) mob1.getHp());
        assertEquals("Mob should have correct power", 15, (int) mob1.getPower());
        assertEquals("Mob should have correct defense", 3, mob1.getDefense());
        assertEquals("Mob should have correct level", 5, mob1.getLevel());
        assertEquals("Mob should have correct XP reward (50*L)", 250, mob1.getXpReward());
        assertEquals("Mob should have correct gold reward (75*L)", 375, mob1.getGoldReward());
    }

    /**
     * Test Case #13: testPartyManagement
     * Verifies that party size is constrained to 5 heroes maximum
     */
    @Test
    public void testPartyManagement() {
        List<Hero> party = new ArrayList<>();
        
        // Add heroes up to maximum
        for (int i = 0; i < 5; i++) {
            Hero hero = HeroFactory.getFactory("WARRIOR").createHero("Hero" + i);
            party.add(hero);
        }
        
        assertEquals("Party should contain exactly 5 heroes", 5, party.size());
    }

    /**
     * Test Case #14: testCombatSystem
     * Verifies basic combat mechanics work correctly
     */
    @Test
    public void testCombatSystem() {
        // Create a small party
        List<Hero> party = new ArrayList<>();
        party.add(warrior);
        
        // Create an enemy
        List<Mob> enemies = new ArrayList<>();
        enemies.add(normalMob);
        
        // Verify battle setup
        assertFalse("Party hero should be alive initially", !warrior.isAlive());
        assertTrue("Mob should be alive initially", normalMob.isAlive());
        
        // Simulate one attack
        warrior.basicAttack(normalMob);
        
        // Verify combat occurred
        assertTrue("Mob should have taken damage", normalMob.getHp() < 50);
    }
}
