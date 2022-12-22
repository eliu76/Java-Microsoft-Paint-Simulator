/*Evan Liu
 * Paint
 * Period 1
 * This program provides a GUI with a menu for the user to interact with 
 * and a panel where the user can draw with their mouse and upload/save files onto the panel
 */
import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;
import java.io.*;

public class Liu_Paint extends JFrame implements ActionListener{

	private DrawPanel display; //main section of the GUI
	private Color currentColor; 
	private int size = 10; //current size that user selected, default is 10

	public Liu_Paint() {

		setTitle("Paint");
		setSize(450,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");

		//creates the menu options
		JMenuItem load = new JMenuItem("Load");
		JMenuItem save = new JMenuItem("Save");
		JMenuItem impose = new JMenuItem("Impose");

		file.add(load);
		file.add(save);
		file.add(impose);

		JMenu options = new JMenu("Options");
		JMenu size = new JMenu("Size");
		JMenuItem color = new JMenuItem("Color");

		JMenuItem size10 = new JMenuItem("10");
		JMenuItem size25 = new JMenuItem("25");
		JMenuItem size50 = new JMenuItem("50");

		//adds sub-menu options onto the GUI
		size.add(size10);
		size.add(size25);
		size.add(size50);
		options.add(color);
		options.add(size);

		menuBar.add(file);
		menuBar.add(options);

		setJMenuBar(menuBar);

		display = new DrawPanel();
		currentColor = Color.BLACK;
		JButton clear = new JButton("Clear");

		display.setBounds(0, 0, 450, 580);
		clear.setBounds(0, 580, 450, 60);

		load.addActionListener(this);
		save.addActionListener(this);
		impose.addActionListener(this);
		color.addActionListener(this);
		size10.addActionListener(this);
		size25.addActionListener(this);
		size50.addActionListener(this);
		clear.addActionListener(this);

		add(display);
		add(clear);

		setVisible(true);
	}

	//what hapenns when each menu option is pressed
	public void actionPerformed(ActionEvent ae)  {

		//calls fileIn which loads in the file and displays it
		if(ae.getActionCommand().equals("Load") ) {

			display.clearPanel();
			fileIn("Load");
		}

		else if(ae.getActionCommand().equals("Save")) {

			fileIn("Save");
		}

		//doesn't clear the panel
		else if(ae.getActionCommand().equals("Impose")) {

			fileIn("Load");
		}

		else if(ae.getActionCommand().equals("10")) {

			size = 10;
		}

		else if(ae.getActionCommand().equals("25")) {

			size = 25;
		}

		else if(ae.getActionCommand().equals("50")) {

			size = 50;
		}

		//opens color chooser window
		else if(ae.getActionCommand().equals("Color")) {

			JColorChooser chooser = new JColorChooser();
			currentColor = chooser.showDialog(null, "Select a color", null);
		}

		//empties points arraylist and sets color to black and size to 10
		else if(ae.getActionCommand().equals("Clear")) {

			display.clearPanel();
		}
	}

	//represents a singular point on the panel with x y location, color, and size
	public class Point{

		private int xLoc;
		private int yLoc;
		private Color color = Color.BLACK;
		private int psize = 10;

		public Point(int x, int y, Color c, int s){

			xLoc = x;
			yLoc = y;
			color = c;
			psize = s;
		}
		
		//displays each point as numbers with spaces in between (color becomes red value, green value, blue value)
		public String toString() {
			
			return xLoc + " " + yLoc + " " + color.getRed() + " " + color.getGreen() + " " + color.getBlue() + " " + psize;
		}
	}

	public class DrawPanel extends JPanel implements MouseMotionListener{

		private ArrayList<Point> points; //stores points on the jdrawpanel

		public DrawPanel(){

			this.addMouseMotionListener(this);  
			points = new ArrayList<Point>();
		}

		public void paintComponent(Graphics g){		

			super.paintComponent(g);
			setBackground(Color.white);

			for(Point nextP: points){

				g.setColor(nextP.color);
				g.fillOval(nextP.xLoc, nextP.yLoc, nextP.psize, nextP.psize);
			}
		}

		//whenever a button is held and the user is dragging their mouse
		public void mouseDragged(MouseEvent me) {

			points.add(new Point(me.getX(), me.getY(), currentColor, size));
			this.repaint();			
		}

		//clears the panel and resets color to black and size to 10
		private void clearPanel() {
			
			display.points.clear();
			currentColor = Color.black;
			size = 10;

			this.repaint();
		}
		
		public void mouseClicked(MouseEvent me) {
		}
		public void mouseEntered(MouseEvent arg0) {
		}
		public void mouseExited(MouseEvent arg0) {
		}
		public void mousePressed(MouseEvent arg0) {
		}
		public void mouseReleased(MouseEvent arg0) {
		}
		public void mouseMoved(MouseEvent me) {
		} 
	}

	//displays the file chooser window and changes words from load to save based off of inputed string
	public void fileIn(String option) {

		try {

			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

		}catch(Exception e) {

			System.out.println("That look and feel is not found");
			System.exit(-1);
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

		int result;

		if(option.equals("Load")) {

			result = fileChooser.showOpenDialog(null);
		}
		
		else {

			result = fileChooser.showSaveDialog(null);
		}

		File f = null;
		Scanner sc = null;

		//loads in the selected file and adds in to the points arraylist where it is displayed
		if(option.equals("Load")) {
			
			if(result == JFileChooser.APPROVE_OPTION){

				f = fileChooser.getSelectedFile();

				try{

					sc = new Scanner(f);

				}catch(FileNotFoundException e){

					System.out.println("File Not Found!");
					System.exit(-1);
				}

				while(sc.hasNextLine()){

					display.points.add(new Point(sc.nextInt(), sc.nextInt(), new Color(sc.nextInt(), sc.nextInt(), sc.nextInt()), sc.nextInt()));
					this.repaint();
				}
			}
		}
		
		//saves the points array as a text file 
		else if(option.equals("Save")) {
			
			try{
				
				//sets the name of file to what the user typed in
				FileWriter outFile = new FileWriter(fileChooser.getSelectedFile().getName());
				
				for(int i = 0; i < display.points.size(); i++){
					
					outFile.write(display.points.get(i) + "\n");
				}
				
				outFile.close();
			
			}catch(IOException e){
				
				System.out.println("IO issue");
				System.exit(-1);
			}
		}
	}

	public static void main(String[] args){

		new Liu_Paint();
	}
}
