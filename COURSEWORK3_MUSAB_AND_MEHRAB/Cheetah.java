import java.util.List;
import java.util.Iterator;
import java.util.Random;
/**
 * A simple model of a cheetah.
 * Cheetahs age, move, eat gazelle and zebras, and die.
 * 
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29 (2)
 */
public class Cheetah extends Animals
{
    // Characteristics shared by all cheetahes (class variables).

    // The age at which a cheetah can start to breed.
    private static final int BREEDING_AGE = 12;
    // The age to which a cheetah can live.
    private int MAX_AGE = 170;
    // The likelihood of a cheetah breeding.
    private static final double BREEDING_PROBABILITY = 0.35;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a gazelle.
    private static final int GAZELLE_FOOD_VALUE = 25;
    // The food value of a zebra
    private static final int ZEBRA_FOOD_VALUE = 30;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // The maximum age of a cheetah after getting disease.
    private static final int DISEASED_MAX_AGE = 125;

    // Individual characteristics (instance fields).
    // The cheetah's age.
    private int age;
    // The cheetah's food level, which is increased by eating gazelles or zebras.
    private int foodLevel;

    /**
     * Create a cheetah. A cheetah can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the cheetah will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Cheetah(boolean randomAge, Field field, Location location)
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
     * This is what the cheetah does most of the time: it hunts for
     * gazelles and zebras. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newCheetahes A list to return newly born cheetahs.
     */
    public void act(List<Actor> newCheetahs)
    {
        incrementAge();
        if (canMove()) {   
            if (Clock.returnClockTimeAsInt() >  12 || Clock.returnClockTimeAsInt() < 8){
                incrementHunger();
                if(hasDisease()) {
                    incrementHunger();
                    MAX_AGE = DISEASED_MAX_AGE;
                }

                if(isAlive()) {
                    giveBirth(newCheetahs);            
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
     * Increase the age. This could result in the cheetah's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Make this cheetah more hungry. This could result in the cheetah's death.
     */
    private void incrementHunger()
    {
        foodLevel--;

        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Look for gazelle and zebras adjacent to the current location.
     * Only the first live gazelle and zebras is eaten.
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
                // Simulates a predator stepping over grass, but it doesn't consume it, therefore food level doesn't increase.
                Grass plant = (Grass) actor;
                return where;

            }
        }
        return null;
    }

    /**
     * Check whether or not this gazelle and zebras is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newCheetahs A list to return newly born gazelle and zebrass.
     */
    private void giveBirth(List<Actor> newCheetahs)
    {
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        if(!isMale()) {
            // Check if animal is female as only female animals can 'give birth'.
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Cheetah) {
                    Cheetah cheetah = (Cheetah) animal;
                    List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
                    // Below if statement checks if they can breed, checking sex, age, freelocation size, etc.
                    if(cheetah.isAlive() && isAlive() && (cheetah.isMale()) && (freeLocations.size() > 0)) {
                        int births = breed();
                        for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                            Location loc = freeLocations.remove(0);
                            Cheetah young = new Cheetah(false, field, loc);
                            // Below if statements can make any of the animals involved in the session ill if one of them is diseased.
                            if (hasDisease() || cheetah.hasDisease()) {
                                young.changeDiseaseStatus(); 
                            }
                            if (cheetah.hasDisease() && !hasDisease()) {
                                changeDiseaseStatus();
                            }
                            else if(!cheetah.hasDisease() && hasDisease()) {
                                cheetah.changeDiseaseStatus();
                            }
                            newCheetahs.add(young);
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
     * A cheetah can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
