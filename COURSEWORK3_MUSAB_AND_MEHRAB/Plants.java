/**
 * Class representing all plants in the simulation.
 */
public abstract class Plants extends Actor
{
    private static final int CHANCE_OF_DISEASE = 9999;    
    // Chance that all plants can get a disease.
    /**
     * Constructor for objects of class Plants
     */
    public Plants(Field field, Location location)
    {
       super(field, location);
    }  
     
    /**
     * @Returns chance of disease
     */
    protected int returnChanceOfDisease()
    {
        return CHANCE_OF_DISEASE;
    }
    
    /**
     * It will check if it is raining and if so, it returns true to 
     * make plants grow faster (Check any plant class for proof such
     * as grass)
     * @return True if raining, false if not.
     */
    protected boolean isRainy()
    {
        boolean rainyGrowth = false;
        if (SimulatorView.returnWeather().equals("Rainy")){
            rainyGrowth = true;
        }
        return rainyGrowth;
    }
}
