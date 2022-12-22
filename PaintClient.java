
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PaintClient extends JFrame implements ActionListener
{

	private Scanner reader;				// used to handle incoming messages
	private PrintWriter writer;			// outgoing messages to server
	private Socket sock;				// creates the connection

	private ArrayList<Point> points;
	private DrawPanel canvas;

	private Person me;
	private int currentSize;

	private DefaultListModel<String> friendModel;		//adds names of ppl in to the room
	private ArrayList<Person> allFriends;			//required for color coding

	private final String IP_ADDRESS ="192.168.86.25";

	// the GUI is provided for you
	public PaintClient(){

		setLayout(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(900, 650);

		currentSize = 25;
		points = new ArrayList<Point>();

		allFriends = new ArrayList<Person>();

		canvas = new DrawPanel();
		canvas.setBounds(40,40,575,525);
		add(canvas);

		friendModel = new DefaultListModel<String>();
		JList<String> friendList = new JList<String>(friendModel);
		
		
		friendList.setCellRenderer(new RowRenderer());
		
		
		JScrollPane friendScroll = new JScrollPane(friendList);
		friendScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Friends"));
		friendScroll.setBounds(675,40,150,250);
		add(friendScroll);

		JMenu sizeMenu = new JMenu("Size");
		String[] sizes = {"10","25","50"};
		for(String nextSize: sizes) {
			JMenuItem nextItem = new JMenuItem(nextSize);
			sizeMenu.add(nextItem);
			nextItem.addActionListener(this);
		}

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(sizeMenu);
		setJMenuBar(menuBar);

		JButton logOff = new JButton("Log Off");
		logOff.addActionListener(this);
		logOff.setBounds(775,440,80,18);
		add(logOff);

		setUpNetworking();

		// IncomingReader is an inner class that implements the Runnable interface
		// the run method is responsible for reading data from the server 

		Thread clientThread = new Thread(new IncomingReader());
		clientThread.start();

		setVisible(true);
	}
	
	// sets up the socket to read for the server on port 4242.  
	// sets up the PrintWriter as well.
	private void setUpNetworking() {

		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter your name: ");

		Random r = new Random();

		me = new Person(keyboard.nextLine(),new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256)));

		this.getContentPane().setBackground(me.color);

		setTitle("Paint Client - " +me.name);

		try {
			
			sock = new Socket(IP_ADDRESS, 4242);
			reader = new Scanner(sock.getInputStream());
			writer = new PrintWriter(sock.getOutputStream());
			
			//sends information about client to server 
			writer.println(me);
			writer.flush();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//reacts to a menu item or logoff being entered
	public void actionPerformed(ActionEvent ae) {
		String trigger = ae.getActionCommand();

		if(trigger.equals("10")||trigger.equals("25")||trigger.equals("50")) {
			currentSize = Integer.parseInt(trigger);
		}
		else {

			//log off was pressed
			try{
				writer.println("logoff:"+me.name);
				writer.flush();
				
				sock.close();				

			}catch(IOException e){
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	// responsible for listening for messages from the server
	// handles a point being received, a new person joining or leaving
	public class IncomingReader implements Runnable {

		// this is all done in a separate thread to allow you to read and draw at the same time
		public void run() {
			
			try {
				
				writer = new PrintWriter(sock.getOutputStream());
				Scanner in = new Scanner(sock.getInputStream());

				while(in.hasNextLine()) {
					
					String input = in.nextLine();
					
					String[] split = input.split(" ");
					
					//removes from friends list if logoff
					if(input.contains("logoff")) {
						
						friendModel.removeElement(split[0].substring(7));
					}
					
					else if(input.contains("joined")) {
						
						if(!split[0].substring(7).equals(me.name)) {
							
							allFriends.add(new Person(split[0].substring(7), new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]))));
							friendModel.addElement(split[0].substring(7));
						}
					}
					
					else {
						
						//work on this
						synchronized(points) {
							
							points.add(new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]), new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4])), Integer.parseInt(split[5])));
						}
						
						canvas.repaint();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//JPanel that can be drawn on by clicking and dragging the mouse
	public class DrawPanel extends JPanel implements MouseMotionListener{

		public DrawPanel(){

			this.addMouseMotionListener(this);

		}

		public void paintComponent(Graphics g){		

			super.paintComponent(g);
			setBackground(Color.white);

			synchronized(points) {
				//paint points
				for(Point nextP: points){

					g.setColor(nextP.ptColor);
					g.fillOval(nextP.xLoc, nextP.yLoc, nextP.ptSize, nextP.ptSize);
				}
			}
		}

		//called whenever the mouse is dragged
		//adds new Points to the AL, updates the canvas
		//and sends the point to the server
		public void mouseDragged(MouseEvent mouse) {
			
			Point toAdd = new Point(mouse.getX(), mouse.getY(), me.color, currentSize);
			
			synchronized(points) {
				
				points.add(toAdd);
			}
			
			writer.println(toAdd);
			writer.flush();
			this.repaint();	
		}

		//not used for this project
		public void mouseMoved(MouseEvent e) {}
	}

	//Do not edit this class
	public class Point{

		private int xLoc;
		private int yLoc;
		private Color ptColor;
		private int ptSize;

		public Point(int x, int y, Color color, int size){
			xLoc = x;
			yLoc = y;
			ptColor = color;
			ptSize = size;
		}

		public String toString() {
			return xLoc+" "+yLoc+" "+ptColor.getRed() +" "+ptColor.getGreen()+" "+ptColor.getBlue()+ " "+ptSize+" ";
		}
	}

	//this class is used to help color code the friends list. Do not edit this class.
	public class Person{
		private String name;
		private Color color;

		public Person(String n, Color c) {
			name = n;
			color = c;
		}

		//this constructor and equals method is really 
		//only used to help update the arraylist of people
		public Person(String n) {
			name = n;
		}

		public boolean equals(Object o) {
			Person p = (Person)o;
			return p.name.equals(name);
		}

		public String toString() {
			return name +" "+color.getRed()+ " "+color.getGreen()+" "+color.getBlue();
		}
	}

	//this class color codes the JList
	//uses the arraylist of friends.  Do not edit this class
	public class RowRenderer extends DefaultListCellRenderer{

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setBackground(allFriends.get(index).color); 
			return c;
		}
	}

	public static void main(String[] args) {
		new PaintClient();
	}
}

