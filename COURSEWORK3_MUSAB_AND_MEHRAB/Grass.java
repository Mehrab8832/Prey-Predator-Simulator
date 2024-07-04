import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a grass patch.
 * They can produce offspirng, die of old age, or be eaten.
 *
 * @Musab Khan and Mehrab Rahman
 */
public class Grass extends Plants
{
    // instance variables - replace the example below with your own
    // Age of grass
    private int age;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    // The age at which a grass can start to breed.
    private static final int BREEDING_AGE = 2;
    // The age to which a grass can live.
    private int MAX_AGE = 200;
    // The likelihood of a grass breeding.
    private static final double BREEDING_PROBABILITY = 0.6;
    // The likelihood of a grass breeding during rainy weather.
    private static final double RAINING_BREEDING_PROBABILITY = 0.75;
    // The maximum age of a diseased grass patch.
    private static final int DISEASED_MAX_AGE = 150;
    // Max amount of children grass can create.
    private static final int MAX_SEED_SIZE = 4;

    /**
     * Constructor for objects of class Plant
     */
    public Grass(boolean randomAge, Field field, Location location)
    {
        super(field, location);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }

    /**
     * Increase the age.
     * This could result in the grass patch's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * What the grass patch will do when the simulation is simulated by one step.
     * They can produce offspring, get disease randomly or die of old age.
     */
    public void act(List<Actor> newGrass)
    {
        incrementAge();
        if(isAlive() && Clock.returnClockTimeAsInt() > 4 && Clock.returnClockTimeAsInt() < 18) {
            giveBirth(newGrass);
            if (isRainy()){
                giveBirth(newGrass);
            }            
            Random rand = new Random();
            int chanceOfDisease = rand.nextInt(10000); // Create a random chance for each grass patch that they can gain a disease from, it being a 1 in 10000 chance.
            if(chanceOfDisease == returnChanceOfDisease())
            {
                changeDiseaseStatus();
                MAX_AGE = DISEASED_MAX_AGE;
            }
        }
        else if(!isAlive()) {
            setDead();
        }
    }

    /**
     * Check whether or not this grass patch is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newGrass A list to return newly born grass patches.
     */
    private void giveBirth(List<Actor> newGrass)
    {
        // New grass patches are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.adjacentLocations(getLocation());
        Iterator<Location> it = free.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            List<Location> freeLocations = field.getFreeAdjacentLocations(getLocation());
            int births = breed();
            for(int b = 0; b < births && freeLocations.size() > 0; b++) {
                Location loc = freeLocations.remove(0);
                Grass young = new Grass(false, field, loc);
                if (hasDisease()) {
                    young.changeDiseaseStatus();  // If grass patch has disease, its children will also have disease.
                }
                newGrass.add(young);
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
        double breedingProbability = BREEDING_PROBABILITY;
        if (isRainy()){
            breedingProbability = RAINING_BREEDING_PROBABILITY;
        }        
        if(canBreed() && rand.nextDouble() <= breedingProbability) {
            births = rand.nextInt(MAX_SEED_SIZE) + 1;
        }
        return births;
    }

    /**
     * A grass patch can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
