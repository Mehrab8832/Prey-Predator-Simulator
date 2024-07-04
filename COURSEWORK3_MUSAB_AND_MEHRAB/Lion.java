import java.util.List;
import java.util.Iterator;
import java.util.Random;
/**
 * A simple model of a lion.
 * Lions age, move, eat gazelles and zebras, breed with the opposite sex and die.
 * 
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29 (2) (Edited)
 */
public class Lion extends Animals
{
    // Characteristics shared by all lions (class variables).

    // The age at which a lion can start to breed.
    private static final int BREEDING_AGE = 15;
    // The age to which a lion can live.
    private int MAX_AGE = 150;
    // The likelihood of a lion breeding.
    private static final double BREEDING_PROBABILITY = 0.35;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a single gazelle. 
    private static final int GAZELLE_FOOD_VALUE = 25;
    //  The food value of a single zebra
    private static final int ZEBRA_FOOD_VALUE = 30;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // Maximum age of lion with disease.
    private static final int DISEASED_MAX_AGE = 115;

    // Individual characteristics (instance fields).
    // The lion's age.
    private int age;
    // The lion's food level, which is increased by eating gazelles and zebras.
    private int foodLevel;

    /**
     * Create a lion. A lion can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the lion will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Lion(boolean randomAge, Field field, Location location)
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
     * This is what the lion does most of the time: it hunts for
     * gazelles. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newLiones A list to return newly born lions.
     */
    public void act(List<Actor> newLions)
    {
        incrementAge();
        // Checks if it foggy and lion is awake to do activities.
        if(canMove())
        {   
            if (Clock.returnClockTimeAsInt() >  12 || Clock.returnClockTimeAsInt() < 8){
                incrementHunger();
                // Checks if lion is ill, if so it will become worse in health.
                if(hasDisease())
                {
                    incrementHunger();
                    MAX_AGE = DISEASED_MAX_AGE;
                }

                if(isAlive()) {
                    giveBirth(newLions);            
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
     * Increase the age. This could result in the lion's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Make this lion more hungry. This could result in the lion's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Look for gazelles and zebras adjacent to the current location.
     * Only the first live gazelle is eaten, if not it's a zebra and if not grass (But it doesn't eat grass but tramples over it!).
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
     * Check whether or not this lion is to give birth at this step.
     * First looks for lion and checks if is the opposite gender and is at breeding age.
     * New births will be made into free adjacent locations.
     * @param newLions A list to return newly born lions.
     */
    private void giveBirth(List<Actor> newLions)
    {
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        if(!isMale()) {
            // Check if animal is female as only female animals can 'give birth'.
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Lion) {
                    Lion lion = (Lion) animal;
                    List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
                    // Below if statement checks if they can breed, checking sex, age, freelocation size, etc.
                    if(lion.isAlive() && isAlive() && (lion.isMale()) && (freeLocations.size() > 0) && lion.canBreed()) {
                        int births = breed();
                        for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                            Location loc = freeLocations.remove(0);
                            Lion young = new Lion(false, field, loc);
                            // Below if statements can make any of the animals involved in the session ill if one of them is diseased.
                            if (hasDisease() || lion.hasDisease()) {
                                young.changeDiseaseStatus(); 
                            }
                             if (lion.hasDisease() && !hasDisease()) {
                                changeDiseaseStatus();
                            }
                            else if(!lion.hasDisease() && hasDisease()) {
                                lion.changeDiseaseStatus();
                            }
                            newLions.add(young);
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
     * A lion can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
