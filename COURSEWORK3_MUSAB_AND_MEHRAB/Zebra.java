import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a zebra.
 * Zebras age, move, breed, and die.
 * 
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29 (2)
 */
public class Zebra extends Animals
{
    // Characteristics shared by all zebras (class variables).

    // The age at which a zebra can start to breed.
    private static final int BREEDING_AGE = 7;
    // The age to which a zebra can live.
    private int MAX_AGE = 70;
    // The likelihood of a zebra breeding.
    private static final double BREEDING_PROBABILITY = 0.27;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // The maximum age of a diseased zebra.
    private static final int DISEASED_MAX_AGE = 50;
    // Food value for grass for each zebra.
    private static final int GRASS_FOOD_VALUE = 55;

    // Individual characteristics (instance fields).

    // The zebra's age.
    private int age;
    // The fox's food level, which is increased by eating zebras.
    private int foodLevel;

    /**
     * Create a new zebra. A zebra may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the zebra will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Zebra(boolean randomAge, Field field, Location location)
    {
        super(field, location);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = GRASS_FOOD_VALUE;
        }
    }

    /**
     * Look for zebras adjacent to the current location.
     * Only the first live zebra is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    public Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Grass) {
                Grass grass = (Grass) animal;
                if(grass.isAlive()) { 
                    if(grass.hasDisease() && !hasDisease())
                    {
                        changeDiseaseStatus();  // If grass has disease and zebra is already not ill, it will become ill.
                    }
                    grass.setDead();
                    foodLevel = GRASS_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * This is what the zebra does most of the time - it runs 
     * around. Sometimes it will breed, eat grass or die of old age.
     * @param newZebras A list to return newly born zebras.
     */
    public void act(List<Actor> newZebras)
    {
        incrementAge();
        if (Clock.returnClockTimeAsInt() <  20 && canMove())
        {                 
            // Above if statement checks if zebra is asleep and if it is able to move if it is foggy weather.
            incrementHunger();
            if(hasDisease())
            {
                // If zebra is ill, increment the hunger once more and decreases age.
                incrementHunger();
                MAX_AGE = DISEASED_MAX_AGE;
            }
            if(isAlive()) {
                giveBirth(newZebras);            
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

    /**
     * Increase the age.
     * This could result in the zebra's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Check whether or not this zebra is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newZebras A list to return newly born zebras.
     */
    private void giveBirth(List<Actor> newZebras)
    {
        // New zebras are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        if(!isMale()) {
            // Check if zebra is female as only female zebra's can 'give birth'.
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Zebra) {
                    Zebra zebra = (Zebra) animal;
                    List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
                    // Below if statement checks if they can breed, checking sex, age, freelocation size, etc.
                    if(zebra.isAlive() && isAlive() && (zebra.isMale()) && (freeLocations.size() > 0) && zebra.canBreed()) {
                        int births = breed();
                        for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                            Location loc = freeLocations.remove(0);
                            Zebra young = new Zebra(false, field, loc);
                            // Below if statements can make any of the animals involved in the session ill if one of them is diseased.
                            if (hasDisease() || zebra.hasDisease()) {
                                young.changeDiseaseStatus();   
                            }
                            if (zebra.hasDisease() && !hasDisease()) {
                                changeDiseaseStatus();
                            }
                            else if(!zebra.hasDisease() && hasDisease()) {
                                zebra.changeDiseaseStatus();
                            }
                            newZebras.add(young);
                        }
                    }
                }
            }
        }
    }

    /**
     * Make this fox more hungry. This could result in the fox's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
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
     * A zebra can breed if it has reached the breeding age.
     * @return true if the zebra can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}