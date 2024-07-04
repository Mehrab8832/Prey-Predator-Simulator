import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location 
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * @author David J. Barnes and Michael Kölling (Edited by Musab Khan and Mehrab Rahman).
 * @version 2016.02.29
 */
public class SimulatorView extends JFrame
{
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for objects that have no defined color.
    private static final Color UNKNOWN_COLOR = Color.gray;

    private final String STEP_PREFIX = "Step: ";
    private final String POPULATION_PREFIX = "Population: ";
    private JLabel stepLabel, population, infoLabel;
    private FieldView fieldView;
    
    // A map for storing colors for participants in the simulation
    private Map<Class, Color> colors;
    // A statistics object computing and storing simulation information
    private FieldStats stats;
    
    // List of weathers that can be in simulator.
    private static ArrayList<Weather> weatherLists;  
    // Current weather of simulation.
    private static Weather currentWeather;
    // Current number of diseased species in simulation.
    private int numOfDiseasedSpecies = 0;

    /**
     * Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width.
     */
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

        setTitle("Savanna Simulation");
        stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
        population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);
        
        // Initialization of weather variables.
        Weather sunny;
        Weather foggy;
        Weather rainy;        
        
        // Creation of weather variables.
        sunny = new Weather("Sunny", 0);
        foggy = new Weather("Foggy", 0);
        rainy = new Weather("Rainy", 0);
        
        // Creation and initialization of weather list.
        weatherLists = new ArrayList<>();        
        weatherLists.add(sunny);
        weatherLists.add(foggy);
        weatherLists.add(rainy);
        setWeather();
        
        setLocation(100, 50);
        
        fieldView = new FieldView(height, width);

        Container contents = getContentPane();
        infoLabel = new JLabel("The time is: " + Clock.returnClockTimeAsString() + ". And the current weather is: " + returnWeather(), JLabel.CENTER);
        
        JPanel infoPane = new JPanel(new BorderLayout());
        infoPane.add(stepLabel, BorderLayout.WEST);
        infoPane.add(infoLabel, BorderLayout.CENTER);
        contents.add(infoPane, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(population, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }
    
    /**
     * Set the current weather and it's duration, it can last from 1 - 10 hours/steps.
     */
    private void setWeather()
    {
        Random randomizer = new Random();
        Weather random = weatherLists.get(randomizer.nextInt(weatherLists.size())); // Randomize the weather
        random.changeWeatherLength(randomizer.nextInt(11)); // Randomize the length of the weather.
        currentWeather = random;        
    }
    
    /**
     * @Return weather name as a string.
     */
    public static String returnWeather()
    {
        return currentWeather.returnWeatherName();
    }
    
    /**
     * Define a color to be used for a given class of actor.
     * @param actorClass The actor's Class object.
     * @param color The color to be used for the given class.
     */
    public void setColor(Class actorClass, Color color)
    {
        colors.put(actorClass, color);
    }

    /**
     * Display a short information label at the top of the window.
     */
    public void setInfoText(String text)
    {
        infoLabel.setText(text);
    }

    /**
     * @return The color to be used for a given class of actor.
     */
    private Color getColor(Class actorClass)
    {
        Color col = colors.get(actorClass);
        if(col == null) {
            // no color defined for this class
            return UNKNOWN_COLOR;
        }
        else {
            return col;
        }
    }

    /**
     * Show the current status of the field.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed.
     */
    public void showStatus(int step, Field field)
    {
        if(!isVisible()) {
            setVisible(true);
        }
            
        stepLabel.setText(STEP_PREFIX + step);
        stats.reset();
        
        fieldView.preparePaint();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Object actor = field.getObjectAt(row, col);
                if(actor != null) {
                    stats.incrementCount(actor.getClass());
                    fieldView.drawMark(col, row, getColor(actor.getClass()));
                }
                
                else { 
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }
        stats.countFinished();
        
        currentWeather.decreaseWeatherLength();
        if(currentWeather.returnWeatherLength() <= 0) {
            setWeather(); // Set new weather once previous weather length is 0, so previous weather is replaced.
        }
        
        while(currentWeather.equals(weatherLists.get(0)) && (Clock.returnClockTimeAsInt() > 20 || Clock.returnClockTimeAsInt() < 4)) {
            setWeather(); // Don't want it to be sunny during the 'night'!
        } 
        
        population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field) + " Number of diseased species are: " + numOfDiseasedSpecies);
        infoLabel.setText("The time is: " + Clock.returnClockTimeAsString() + ". And the current weather is: " + returnWeather());
        fieldView.repaint();
    }

    /**
     * Determine whether the simulation should continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        return stats.isViable(field);
    }
    
    /**
     * Increment list of diseased actor's.
     */
    public void incrementDiseasedActorList() 
    {
         numOfDiseasedSpecies++;
    }
    
    /**
     * Reset list of diseased actor's. (For when simulation starts up again).
     */
    public void resetDiseasedActorList() 
    {
        numOfDiseasedSpecies = 0;
    }
    
    /**
     * Provide a graphical view of a rectangular field. This is 
     * a nested class (a class defined inside a class) which
     * defines a custom component for the user interface. This
     * component displays the field.
     * This is rather advanced GUI stuff - you can ignore this 
     * for your project if you like.
     */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private int gridWidth, gridHeight;
        private int xScale, yScale;
        Dimension size;
        private Graphics g;
        private Image fieldImage;

        /**
         * Create a new FieldView component.
         */
        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        /**
         * Tell the GUI manager how big we would like to be.
         */
        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                                 gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        /**
         * Prepare for a new round of painting. Since the component
         * may be resized, compute the scaling factor again.
         */
        public void preparePaint()
        {
            if(! size.equals(getSize())) {  // if the size has changed...
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) {
                    xScale = GRID_VIEW_SCALING_FACTOR;
                }
                yScale = size.height / gridHeight;
                if(yScale < 1) {
                    yScale = GRID_VIEW_SCALING_FACTOR;
                }
            }
        }
        
        /**
         * Paint on grid location on this field in a given color.
         */
        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale-1, yScale-1);
        }

        /**
         * The field view component needs to be redisplayed. Copy the
         * internal image to screen.
         */
        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    // Rescale the previous image.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}
