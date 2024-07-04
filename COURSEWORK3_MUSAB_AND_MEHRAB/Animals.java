import java.util.Random;
/**
 * Class representing all animals in the simulation.
 */
public abstract class Animals extends Actor
{
    private boolean isMale;
    // The animal sex, true if male, false if not.
    
    public Animals(Field field, Location location)
    {
       super(field, location); 
       Random rand = new Random();
       isMale = rand.nextBoolean();
    }    
    
    /**
     * A method which all animals must do, predators hunting for other smaller animals while
     * prey hunt for grass (or any plant species) only.
     */
    abstract protected Location findFood();
    
    /**
     * If it's foggy, there is a half chance if the animal can
     * move or not.
     */
    protected boolean canMove()
    {
        boolean mobility = true;
        if (SimulatorView.returnWeather().equals("Foggy")){
            Random rand = new Random();
            mobility = rand.nextBoolean();
        }
        return mobility;
    }
    
    /**
     * Returns animal's sex.
     * @return True if male, false if not.
     */
    protected boolean isMale() 
    {
        return isMale;   
    }
    
}
