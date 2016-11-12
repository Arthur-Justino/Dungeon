import java.util.Scanner;
import java.util.Random;
import java.util.Arrays;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A text-based adventure action game.
 * 
 * @author Maas Lalani
 * @version 1.3 2016-11-12
 */
public class TheDungeon
{
    /** The menu option for attacking. */
    public static final int ATTACK = 1;

    /** The string to answer yes or no questions affirmatively. Not case-sensitive. */
    public static final String CONFIRMATION = "yes";

    /** The delay used for display messages. */
    public static final long DELAY = 2000;

    /** The menu option for exiting the game. */
    public static final int EXIT = 4;

    /** The RANDOM number generator of this game. */
    public static final Random RANDOM = new Random();

    /** The menu option for running away. */
    public static final int RUN = 3;

    /** The way of gathering input of this game. */
    public static final Scanner SCANNER = new Scanner(System.in);

    /** The menu option for using a potion. */
    public static final int USE_POTION = 2;

    /**
     * The dungeon game.
     * 
     * @param argument not used
     */
    public static void main(String[] argument)
    {
        // Main character
        Player player = new Player();

        // Game variables
        /* The following three chance variables are percentages */
        int armourDropChance = 10;
        int healthPotionDropChance = 50;
        int swordDropChance = 10;
        boolean running = true;
        boolean ranAway = false;

        // Game introduction
        System.out.println("\fWelcome to the dungeon.");

        System.out.print("Would you like to load your previous game? ");
        String loadGameState = SCANNER.nextLine();

        if (loadGameState.equalsIgnoreCase(CONFIRMATION))
        {
            System.out.print("\nWhat is your name? ");
            String name = SCANNER.nextLine();
            player.setName(name);

            /* Search for the user's name in the database */
            try
            {
                State.loadState(player);
            }
            catch (FileNotFoundException exception)
            {
                System.out.println("\nYour saved game was not found. Starting a new unsaved game.");
                delay();
            } // end of catch(FileNotFoundException exception)
            catch (IOException exception)
            {
                System.out.println("Input from the keyboard could not be read. Please restart the game.");
            } // end of catch (IOException exception)
        } // end of if (loadGameState.equalsIgnoreCase("Y"))

        // Game loop
        while (running) 
        {
            // Main enemy 
            Enemy villain = new Enemy();

            while (villain.health() > 0)
            {
                printStatistics(player, villain);

                startBattle();

                int choice;

                try 
                {
                    choice = Integer.parseInt(SCANNER.nextLine());
                }
                catch (NumberFormatException exception)
                {
                    choice = RUN;
                } // end of catch (NumberFormatException exception)

                switch (choice)
                {
                    case ATTACK:
                        ranAway = false;
                        int playerAttack = player.attack();
                        int enemyAttack = villain.attack();
    
                        System.out.println("\nYou dealt " + playerAttack + " damage.");
                        System.out.println("You took " + enemyAttack + " damage.");
    
                        villain.takeDamage(playerAttack);
                        player.takeDamage(enemyAttack);
    
                        delay();
                        break;

                    case USE_POTION:
                        if (player.health() > player.FULL_HEALTH - player.POTION_HEALING) 
                        {
                            System.out.println("\n You are healthy, and do not need a potion.");
                            break;
                        }
                        player.usePotion();
    
                        System.out.println("\nYou drank the potion. Health restored by: " + Player.POTION_HEALING + " HP");
                        System.out.println("Current HP: " + player.health());
    
                        delay();
                        break;

                    case RUN:
                        System.out.println("\nYou successfully ran away!");
                        delay();
    
                        /* Kill the enemy by dealing damage equivalent to its health. */
                        villain.takeDamage(villain.health());
    
                        ranAway = true;
                        break;

                    case EXIT:
                        System.out.println("\fExiting game...");
                        System.out.print("Would you like to save your progress? ");
    
                        if (SCANNER.nextLine().equalsIgnoreCase(CONFIRMATION))
                        {
                            State.saveState(player);
                        } // end of if (SCANNER.nextLine().equalsIgnoreCase(CONFIRMATION))
    
                        running = false;
                        return;
                } // end of switch (choice)

                if (player.health() <= 0)
                {
                    System.out.println("\nUh oh! You have died, game over.");

                    System.out.print("Type '1' to try again. ");
                    int continueGame = SCANNER.nextInt();

                    if (continueGame == 1) 
                    {
                        running = true;
                        player.reset();
                    } // end of if (input.equals("1")) 
                    else 
                    {
                        System.out.println("\nProgram terminated.");

                        /* Kill the enemy by dealing damage equivalent to its health. */
                        villain.takeDamage(villain.health());
                        running = false;
                        return;
                    } // end of if (input.equals("1"))   
                }  // end of if (player.health() <= 0)
            } // end of loop while (villain.health() > 0)

            if (!ranAway)
            {
                /* The enemy has died and the player did not run away. This means the player killed the enemy. Reward the player. */
                player.increaseEnemiesKilled();

                if (RANDOM.nextInt(100) < swordDropChance)
                {
                    if (player.hasSword())
                    {
                        System.out.println("\n The " + villain.name() + " dropped a sword, but you already have one.");
                    } // end of if (player.hasSword())
                    else
                    {
                        player.addSword("");
                        System.out.println("\n The " + villain.name() + " dropped a " + player.getSword().name() + ".\nYour attack damage has now increased by " + player.getSword().damageIncrease() + ".");
                    } // end of if (player.hasSword())
                    delay();            
                } // end of if (RANDOM.nextInt(100) < swordDropChance)

                else if (RANDOM.nextInt(100) < armourDropChance)
                {
                    if (player.hasArmour())
                    {
                        System.out.println("\nThe " + villain.name() + " dropped some armour, but you already have some.");
                    } // end of if (player.hasArmour())
                    else
                    {
                        player.addArmour("leather");
                        System.out.println("\nThe " + villain.name() + " dropped " + player.getArmour().name() + ".\nYour damage taken has now decreased by " + player.getArmour().damageBlocked() + ".");
                    } // end of if (player.hasArmour())
                    delay(); 
                } // end of else if (RANDOM.nextInt(100) < armourDropChance)

                else if (RANDOM.nextInt(100) < healthPotionDropChance)
                {
                    player.addPotions(1);
                    System.out.println("\nThe " + villain.name() + " dropped a health potion.");
                    delay();
                } // end of else if (RANDOM.nextInt(100) < healthPotionDropChance)  
            } // end of if(!ranAway)
        } // end of loop while (running)
    } // end of main(String[] argument)

    /**
     * The battle prompt menu of this program.
     */
    public static void startBattle()
    {
        System.out.println("\n1. Attack.");
        System.out.println("2. Use potion.");
        System.out.println("3. Run!");
        System.out.println("4. Exit Game.");

        System.out.print("\nChoice? ");
    } // end of startBattle()

    /**
     * Prints the statistics of this game, includes the player's and enemy's state..
     */
    public static void printStatistics(Player player, Enemy villain	)
    {
        // Statistics
        System.out.println("\f# A " + villain.name() + " appeared #");

        System.out.println("\n# You have " + player.health() + " HP #");
        System.out.println("# Enemy has " + villain.health() + " HP #");
        System.out.println("# Potions left: " + player.getPotions() + " #");
        System.out.println("# Enemies killed: " + player.enemiesKilled() + " #");

        // Sword
        if (player.hasSword())
        {
            System.out.println("\n# Sword type: " + player.getSword().name() + " | hitpoints: " + player.getSword().hitpoints() + "  #");
        } // end of if (player.hasSword())

        // Armour 
        if (player.hasArmour())
        {
            System.out.println("\n# Armour type: " + player.getArmour().name() + " | Armour hitpoints: " + player.getArmour().hitpoints() + "  #");
        } // end of if (player.hasArmour())
    }

    /*
     * Puts thread to sleep to allow the user to read the display messages.
     */
    private static void delay()
    {
        try
        {
            Thread.sleep(DELAY);
        }
        catch (InterruptedException exception)
        {
            System.out.println("\fThe game experienced an interrupted exception.");
            System.out.println("The game state was not saved.");
            System.out.println("Please restart the game.");

            System.exit(0);
        } // end of catch (InterruptedException)
    } // end of method delay()
} // end of class TheDungeon