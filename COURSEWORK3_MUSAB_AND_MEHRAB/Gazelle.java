import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a gazelle.
 * Gazelles age, move, breed, and die.
 * 
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29 (2)
 */
public class Gazelle extends Animals
{
    // Characteristics shared by all gazelles (class variables).

    // The age at which a gazelle can start to breed.
    private static final int BREEDING_AGE = 5;
    // The age to which a gazelle can live.
    private int MAX_AGE = 60;
    // The likelihood of a gazelle breeding.
    private static final double BREEDING_PROBABILITY = 0.12;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // Maximum age of diseased gazelle.
    private static final int DISEASED_MAX_AGE = 45;
    // Value of grass for all gazelle's
    private static final int GRASS_FOOD_VALUE = 55;


    // Individual characteristics (instance fields).

    // The gazelle's age.
    private int age;
    // The gazelle's food level, which is increased by eating grass patches.
    private int foodLevel;


    /**
     * Create a new gazelle. A gazelle may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the gazelle will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Gazelle(boolean randomAge, Field field, Location location)
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
     * Look for gazelles adjacent to the current location.
     * Only the first live gazelle is eaten.
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
                    if(grass.hasDisease() && !hasDisease()) {
                        changeDiseaseStatus(); // If grass is diseased, the gazelle will also become diseased (assuming it already isn't).
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
     * This is what the gazelle does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * @param newGazelles A list to return newly born gazelles.
     */
    public void act(List<Actor> newGazelles)
    {
        incrementAge();
        if ((Clock.returnClockTimeAsInt()) <  19 && canMove())
        {                 
            // Above if statement checks if gazelle is asleep and if it is able to move given it's not foggy weather.
            incrementHunger();
            if(hasDisease())
            {
                // If gazelle is ill, increment the hunger once more and decrease max age.
                incrementHunger();
                MAX_AGE = DISEASED_MAX_AGE;
            }
            if(isAlive()) {
                giveBirth(newGazelles);            
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
     * This could result in the gazelle's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Check whether or not this gazelle is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newGazelles A list to return newly born gazelles.
     */
    private void giveBirth(List<Actor> newGazelles)
    {
        // New gazelles are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        if(!isMale()) {
            // Check if gazelle is female as only female gazelle's can 'give birth'.
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Gazelle) {
                    Gazelle gazelle = (Gazelle) animal;
                    List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
                    // Below if statement checks if they can breed, checking sex, age, freelocation size, etc.
                    if(gazelle.isAlive() && isAlive() && (gazelle.isMale()) && (freeLocations.size() > 0) && gazelle.canBreed()) {
                        int births = breed();
                        for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                            Location loc = freeLocations.remove(0);
                            Gazelle young = new Gazelle(false, field, loc);
                            // Below if statements can make any of the animals involved in the session ill if one of them is diseased.
                            if (hasDisease() || gazelle.hasDisease()) {
                                young.changeDiseaseStatus(); 
                            }
                            
                            if (gazelle.hasDisease() && !hasDisease()) {
                                changeDiseaseStatus();
                            }
                            else if(!gazelle.hasDisease() && hasDisease()) {
                                gazelle.changeDiseaseStatus();
                            }
                            newGazelles.add(young);
                        }
                    }
                }
            }
        }
    }

    /**
     * Make this gazelle more hungry. This could result in the gazelle's death.
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
     * A gazelle can breed if it has reached the breeding age.
     * @return true if the gazelle can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
