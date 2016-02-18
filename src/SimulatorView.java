import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location 
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2011.07.31
 */
public class SimulatorView extends JFrame
{
	// Colors used for empty locations.
	private static final Color EMPTY_COLOR = Color.white;

	// Color used for objects that have no defined color.
	private static final Color UNKNOWN_COLOR = Color.gray;

	private final String STEP_PREFIX = "Step: ";
	private final String POPULATION_PREFIX = "Population: ";
	private JLabel stepLabel, population;
	private FieldView fieldView;

	// A map for storing colors for participants in the simulation
	private Map<Class, Color> colors;
	// A statistics object computing and storing simulation information
	private FieldStats stats;

	private JPanel statsPanel;

	private JPanel buttonPanel;
	private JButton playButton;
	private JButton pauseButton;
	
	private JPanel comboPanel;
	private JComboBox<Array> rabbitList; 
	private JComboBox<String> foxList;
	
	public static String[] foxArray = {"Red", "Black", "Purple"};
	public static String[] rabbitArray = {"Blue", "Yellow", "Green"};
	private JButton btnChangeFox;
	private JButton btnChangeRabbit;


	/**
	 * Create a view of the given width and height.
	 * @param height The simulation's height.
	 * @param width  The simulation's width.
	 */
	public SimulatorView(int height, int width)
	{
		stats = new FieldStats();
		colors = new LinkedHashMap<Class, Color>();

		setTitle("Fox and Rabbit Simulation");

		statsPanel = new JPanel(new BorderLayout());

		stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
		statsPanel.add(stepLabel, BorderLayout.WEST);
		population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);
		statsPanel.add(population, BorderLayout.EAST);

		buttonPanel = new JPanel(new BorderLayout());

		playButton = new JButton("play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Simulator.start();
			}
		});
		buttonPanel.add(playButton, BorderLayout.WEST);		
		//////		
		pauseButton = new JButton("pause");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg1) {
				Simulator.pause();
			}
		});
		buttonPanel.add(pauseButton, BorderLayout.EAST);
		
		comboPanel = new JPanel(new FlowLayout());
			
		foxList = new JComboBox(foxArray);		
		foxList.setToolTipText("Fox color");
		comboPanel.add(foxList);	
		
		btnChangeFox = new JButton("Change Fox");
		btnChangeFox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg2) {
				////////
				String foxColorString = foxList.getSelectedItem().toString();
								
				if (foxColorString.equals("Red")){
					setColor(Fox.class, Color.red);
				}
				
				if (foxColorString.equals("Black")){
					setColor(Fox.class, Color.black);
				}
				
				if (foxColorString.equals("Purple")){
					setColor(Fox.class, Color.magenta);
				}		
			}
		});
		comboPanel.add(btnChangeFox);
			
		rabbitList = new JComboBox(rabbitArray);
		rabbitList.setToolTipText("Rabbit color");
		comboPanel.add(rabbitList);
		
		btnChangeRabbit = new JButton("Change Rabbit");
		btnChangeRabbit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg3) {
				///////
				String rabbitColorString = rabbitList.getSelectedItem().toString();
				
				if (rabbitColorString.equals("Blue")){
					setColor(Rabbit.class, Color.blue);
				}
				
				if (rabbitColorString.equals("Yellow")){
					setColor(Rabbit.class, Color.yellow);
				}
				
				if (rabbitColorString.equals("Green")){
					setColor(Rabbit.class, Color.green);
				}
				
			}
		});
		comboPanel.add(btnChangeRabbit);	
		
		setLocation(350, 50);

		fieldView = new FieldView(height, width);

		Container contents = getContentPane();
		contents.add(statsPanel, BorderLayout.NORTH);
		contents.add(fieldView, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(comboPanel, BorderLayout.CENTER);
			
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Define a color to be used for a given class of animal.
	 * @param animalClass The animal's Class object.
	 * @param color The color to be used for the given class.
	 */
	public void setColor(Class animalClass, Color color)
	{
		colors.put(animalClass, color);
	}

	/**
	 * @return The color to be used for a given class of animal.
	 */
	private Color getColor(Class animalClass)
	{
		Color col = colors.get(animalClass);
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
				Object animal = field.getObjectAt(row, col);
				if(animal != null) {
					stats.incrementCount(animal.getClass());
					fieldView.drawMark(col, row, getColor(animal.getClass()));
				}
				else {
					fieldView.drawMark(col, row, EMPTY_COLOR);
				}
			}
		}
		stats.countFinished();

		population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field));
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
	
	public JComboBox getComboBox() {
		return rabbitList;
	}
}
