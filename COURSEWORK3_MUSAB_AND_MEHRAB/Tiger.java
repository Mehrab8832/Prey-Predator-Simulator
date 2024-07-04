import java.util.List;
import java.util.Iterator;
import java.util.Random;
/**
 * A simple model of a tiger.
 * Tigers age, move, eat gazelle and zebras, and die.
 * 
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29 (2)
 */
public class Tiger extends Animals
{
    // Characteristics shared by all tigers (class variables).

    // The age at which a tiger can start to breed.
    private static final int BREEDING_AGE = 12;
    // The age to which tiger can live.
    private int MAX_AGE = 180;
    // The likelihood of a tiger breeding.
    private static final double BREEDING_PROBABILITY = 0.35;
    // The maximum number of births per breeding session.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a single zebra. 
    private static final int ZEBRA_FOOD_VALUE = 30;
    // he food value of a single gazelle.
    private static final int GAZELLE_FOOD_VALUE = 25;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // The maximum age of a diseased tiger.
    private static final int DISEASED_MAX_AGE = 125;

    // Individual characteristics (instance fields).
    // The tiger's age.
    private int age;
    // The tiger's food level, which is increased by eating food.
    private int foodLevel;

    /**
     * Create a tiger. A tiger can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the tiger will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Tiger(boolean randomAge, Field field, Location location)
    {
        super(field, location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(ZEBRA_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = ZEBRA_FOOD_VALUE;
        }
    }

    /**
     * This is what the fox does most of the time: it hunts for
     * rabbits. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newTigers A list to return newly born tigers.
     */
    public void act(List<Actor> newTigers)
    {
        incrementAge();
        if (canMove())
        {   
            if (Clock.returnClockTimeAsInt() >  12 || Clock.returnClockTimeAsInt() < 8){
                // Checks if it can move despite any foggy weather and if tiger is asleep!
                incrementHunger();
                if(hasDisease())
                {
                    incrementHunger();
                    MAX_AGE = DISEASED_MAX_AGE;
                }
                if(isAlive()) {
                    giveBirth(newTigers);            
                    // Move towards a source of food if found.
                    Location newLocation = findFood();
                    if(newLocation == null) { 
                        // No food found - try to move to a free location.
                        newLocation = getField().freeAdjacentLocation(getLocation());
                    }
                    // See if it was possible to move.
                    if(newLocation != null) {
                        setLocation(newLocation);
                    }
                    else {
                        // Overcrowding.
                        setDead();
                    }
                }
            }
        }
    }

    /**
     * Increase the age. This could result in this tiger's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Make this fox more hungry. This could result in this tiger's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Look for food adjacent to the current location.
     * Only the first live gazelle is eaten, then zebra, then it tramples over any grass.
     * @return Where food was found, or null if it wasn't.
     */
    public Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object actor = field.getObjectAt(where);
            if(actor instanceof Gazelle) {
                Gazelle gazelle = (Gazelle) actor;
                if(gazelle.isAlive()) { 
                    if(gazelle.hasDisease() && !hasDisease())
                    {
                        changeDiseaseStatus();
                    }
                    gazelle.setDead();
                    foodLevel = GAZELLE_FOOD_VALUE;
                    return where;
                }
            }
            else if (actor instanceof Zebra) {
                Zebra zebra = (Zebra) actor;
                if(zebra.isAlive()) { 
                    if(zebra.hasDisease() && !hasDisease())
                    {
                        changeDiseaseStatus();
                    }
                    zebra.setDead();
                    foodLevel = ZEBRA_FOOD_VALUE;
                    return where;
                }
            }
            else if(actor instanceof Grass) {
                Grass grass = (Grass) actor;
                return where;
            }
        }
        return null;
    }

    /**
     * Check whether or not this tiger is to give birth at this step.
     * It must breed with the opposite sex and the other tiger must be old enough.
     * New births will be made into free adjacent locations.
     * @param newGazelles A list to return newly born tigers.
     */
    private void giveBirth(List<Actor> newTigers)
    {
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        if(!isMale()) {
            // Checks if tiger is female so it can 'breed'.
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Tiger) {
                    Tiger tiger = (Tiger) animal;
                    List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
                    // Below if statement checks if they can breed, checking sex, age, freelocation size, etc.
                    if(tiger.isAlive() && isAlive() && (tiger.isMale()) && (freeLocations.size() > 0)) {
                        int births = breed();
                        for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                            Location loc = freeLocations.remove(0);
                            Tiger young = new Tiger(false, field, loc);
                            // Below if statements can make any of the animals involved in the session ill if one of them is diseased.
                            if (hasDisease() || tiger.hasDisease()) {
                                young.changeDiseaseStatus(); 
                            }
                            if (tiger.hasDisease() && !hasDisease()) {
                                changeDiseaseStatus();
                            }
                            else if(!tiger.hasDisease() && hasDisease()) {
                                tiger.changeDiseaseStatus();
                            }
                            newTigers.add(young);
                        }
                    }
                }
            }
        }

    }

    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A fox can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}