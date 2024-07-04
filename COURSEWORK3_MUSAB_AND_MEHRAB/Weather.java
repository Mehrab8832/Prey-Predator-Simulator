
/**
 * Class for each weather used in the simulation.
 *
 * @Musab Khan and Mehrab Rahman
 */
public class Weather
{
    private String weatherName;
    // Name of the weather
    private int weatherLengthTime;
    // How long the weather will last, if 0, the weather is inactive.
    /**
     * Constructor for objects of class Weather
     * @param weatherName gives name to weather object
     * @param weatherLengthTime becomes the new weather length.
     */
    public Weather(String weatherName, int weatherLengthTime)
    {
        this.weatherName = weatherName;
        this.weatherLengthTime = weatherLengthTime;
    }
    
    /**
     * Return weather name as string.
     */
    public String returnWeatherName()
    {
        return weatherName;
    }
    
    /**
     * Change weather length, how long it will last in hours/steps.
     * @param newWeatherLengthTime will be the new weatherLengthTime.
     */
    public void changeWeatherLength(int newWeatherLengthTime)
    {
        weatherLengthTime = newWeatherLengthTime;
    }
    
    /**
     * Decrease weather length by a step/hour.
     */
    public void decreaseWeatherLength()
    {
        weatherLengthTime--;
    }
    
    /**
     * @return weather length as an int.
     */
    public int returnWeatherLength()
    {
        return weatherLengthTime;
    }
}
