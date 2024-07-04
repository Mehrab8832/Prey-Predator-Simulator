
/**
 * The class containing the clock time for the simulator.
 * Clock displays time as a 24 hour format.
 * Used to simulate sleeping patterns within animals.
 *
 * @author Musab Khan and Mehrab Rahman
 * @version 2016.02.29
 */
public class Clock
{
    // Variable containing current clock time.
    private static int clockTime;

    /**
     * Constructor for objects of class Clock
     */
    public Clock()
    {
        clockTime = 0;
    }

    /**
     * @return Clock time as string.
     */
    public static String returnClockTimeAsString()
    {  
        String clockTimeToDisplay = "" + clockTime + ":00";
        if (clockTime < 10) {
            clockTimeToDisplay = "0" + clockTime + ":00"; // Displays times before 10 as double digits.
        }
        return clockTimeToDisplay;
    }
    
    /**
     * @Return clock time as a single int.
     */
    public static int returnClockTimeAsInt()
    {
        return clockTime;
    }
    
    /**
     * Increment clock time by one.
     */
    public static void incrementClockTime()
    {
        clockTime++;
    }
    
    /**
     * Change the clock time depending on the parameter.
     * @param newTime which clockTime is changed to.
     */
    public static void changeClockTime(int newTime)
    {
        clockTime = newTime;
    }
}
