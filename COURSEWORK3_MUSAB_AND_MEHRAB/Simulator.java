import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
/**
 * A simple simulator of savannah species, based on a rectangular field
 * containing multiple actors, from animals to plants.
 * 
 * @author David J. Barnes and Michael Kölling (Edited by Musab Khan + Mehrab Rahman)
 * @version 2016.02.29 (2)
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a lion will be created in any given grid position.
    private static final double LION_CREATION_PROBABILITY = 0.03;
    // The probability that a gazelle will be created in any given grid position.
    private static final double GAZELLE_CREATION_PROBABILITY = 0.20;    
    // The probability that a tiger will be created in any given grid position. :)
    private static final double TIGER_CREATION_PROBABILITY = 0.03;
    // The probability that a zebra will be created in any given grid position.
    private static final double ZEBRA_CREATION_PROBABILITY = 0.20;    
    // The probability that a grass patch will be created in any given grid position. :)
    private static final double GRASS_CREATION_PROBABILITY = 0.5;
    // The probability that a cheetah will be created in any given grid position. :)
    private static final double CHEETAH_CREATION_PROBABILITY = 0.03;
    // List of animals in the field.
    private List<Actor> actors;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }

    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }

        actors = new ArrayList<>();
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Gazelle.class, Color.ORANGE);
        view.setColor(Lion.class, Color.BLUE);
        view.setColor(Grass.class, Color.GREEN);
        view.setColor(Tiger.class, Color.RED);
        view.setColor(Zebra.class, Color.BLACK);
        view.setColor(Cheetah.class, Color.PINK);
        // Setup a valid starting point.
        reset();
    }

    /**
     * Run the simulation from its current state for a reasonably long period,
     * (100 steps/ 100 hours).
     */
    public void runLongSimulation()
    {
        simulate(100);
    }

    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
            //  delay(125);   // uncomment this to run more slowly
        }
    }

    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * actor.
     */
    public void simulateOneStep()
    {
        step++;
        Clock.incrementClockTime();
        if(Clock.returnClockTimeAsInt() == 24)
        {
            Clock.changeClockTime(0); // Reset clock time if one day has passed.
        }

        // Provide space for newborn animals.
        List<Actor> newAnimals = new ArrayList<>();        
        // Let all actors act.
        view.resetDiseasedActorList();
        // Reset diseased animal list before recounting.
        for(Iterator<Actor> it = actors.iterator(); it.hasNext(); ) {
            Actor actor = it.next();
            actor.act(newAnimals);
            if(! actor.isAlive()) {
                it.remove();
            }
            if(actor.hasDisease()) {
                view.incrementDiseasedActorList(); // Count number of diseased animal and add them to list to be displayed in simulator.
            }
        }

        // Add the newly made actors to the main lists.
        actors.addAll(newAnimals);

        view.showStatus(step, field);
    }

    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        actors.clear();
        populate();
        Clock.changeClockTime(0);
        view.resetDiseasedActorList();

        // Show the starting state in the view.
        view.showStatus(step, field);
    }

    /**
     * Randomly populate the field with the actors.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= LION_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Lion lion = new Lion(true, field, location);
                    actors.add(lion);
                }
                else if(rand.nextDouble() <= GAZELLE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Gazelle gazelle = new Gazelle(true, field, location);
                    actors.add(gazelle);
                }
                else if(rand.nextDouble() <= GRASS_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Grass grass = new Grass(true, field, location);
                    actors.add(grass);
                }
                else if(rand.nextDouble() <= TIGER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Tiger tiger = new Tiger(true, field, location);
                    actors.add(tiger);
                }
                else if(rand.nextDouble() <= ZEBRA_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Zebra zebra = new Zebra(true, field, location);
                    actors.add(zebra);
                }
                else if(rand.nextDouble() <= CHEETAH_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Cheetah cheetah = new Cheetah(true, field, location);
                    actors.add(cheetah);
                }
                // else leave the location empty.
            }
        }
    }

    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
}
