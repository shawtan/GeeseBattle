package tron;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/*
 * This is the game client
 * It is what the players actually use to play the game
 * There will be an option for single player game play, or multiplayer
 * It has graphics
 * 
 * by Shaw Tan
 * 16/12/2014
 */


public class GeeseBattle extends JFrame {


	final int FALSE = 0;
	final int TRUE = 1;
	
	public static enum Direction{
		NORTH, EAST, SOUTH, WEST
	}
	
	final int WIDTH = 100;
	final int HEIGHT = 100;

	final int PPI = 4; 		//Pixel density
	final int TICK = 100;	//Timer speed
	
	final static int PORT = 8002;			//The network port the game uses

	final int NUM_PLAYERS = 2;		//Number of players
	
	//Colors for each player, and dead blocks
	final Color[] color = {Color.BLUE, Color.RED, Color.BLACK};
	
	//Communications announcement codes
	public static enum Code{
		SEND_START, SEND_DIR, SEND_ARR, SEND_LOSS, SEND_LOC
	}
//	final int SEND_START = 10;	//Game starting
//	final int SEND_DIR = 11;	//Send new direction
//	final int SEND_ARR = 12;	//Send board
//	final int SEND_LOSS = 13;	//A player lost
//	final int SEND_LOC = 14; 	//Send location
	
	private int SELF;		//Which player this is
	
	
	
	//Direction and location of the players
	private Direction dir[] = new Direction[2];
	private int locX[] = new int[2];
	private int locY[] = new int[2];
	
	//Game grid
	private byte[][] grid;
	
	private Timer timer;		//A timer for when the server's slow
	
	private KeyboardPanel kp;	//Player interacts with keyboard

	private JButton btnNew;		//Button used to start the game
	private JLabel lblStatus;	//Label to display winners for status messages

	//I/O with server
	private ObjectInputStream fromServer;
	private ObjectOutputStream toServer;

	public static void main (String args[]){
		new GeeseBattle();
	}

	public GeeseBattle(){

		//Create the window
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Tron");
		this.setResizable(false);

		JPanel pMenu = new JPanel();		//A panel on top of the window
		
		//Create button used to start the game
		btnNew = new JButton("New Game");
		btnNew.addActionListener(new ButtonListener());
		btnNew.setFocusable(false);
		pMenu.add(btnNew);

		//Create label for status messages
		lblStatus = new JLabel("Connecting");	//Client is connecting during start
		pMenu.add(lblStatus);
		pMenu.setBackground(Color.GRAY);
		this.add(pMenu,BorderLayout.NORTH);

		//Create keyboard panel to accept keypresses
		kp = new KeyboardPanel();
		kp.setFocusable(true);
		this.add(kp,BorderLayout.CENTER);

		//Window
		this.setVisible(true);		
		this.setSize(PPI*WIDTH,PPI*HEIGHT+this.getInsets().top+pMenu.getHeight());

		try {
			//Attempt to connect to server
			
			//Find out where to connect
			Socket socket = new Socket(JOptionPane.showInputDialog("Enter the server name:"), PORT);
//			Socket socket = new Socket("localhost",PORT);
			
			//I/O with server
			fromServer = new ObjectInputStream(socket.getInputStream());
			toServer = new ObjectOutputStream(socket.getOutputStream());

			//Find out which player this is
			SELF = fromServer.readInt();
			//Received means successful connection
			lblStatus.setText("Connected");
			
			//Update window to match the player
			this.setTitle("Tron Player " + (SELF+1));
			System.out.println("Player " + (SELF+1));
			
		} catch (UnknownHostException e) {
			//The server wasn't found
			lblStatus.setText("Cannot find server");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		timer = new Timer(TICK, new TimerListener());

		new Thread(new HandleInput()).start();

	}

	private void newGame(){
		//Initialize game
		//(should be the same as what the server has)
		locX[0] = 0+2;
		locX[1] = WIDTH-3;
		locY[0] = HEIGHT /2;
		locY[1] = HEIGHT /2;
		dir[0] = Direction.EAST;
		dir[1] = Direction.WEST;
		grid = new byte[WIDTH][HEIGHT];
		repaint();
		timer.start();
		lblStatus.setText("Game started");
		System.out.println("Starting game");
	}

	void endGame(int player){
		//Someone lost
		timer.stop();
		
		if (player < NUM_PLAYERS)
			lblStatus.setText("Player " + (player+1) + " lost.");
		else
			lblStatus.setText("Tie game");
	}
	class HandleInput implements Runnable{

		@Override
		public void run() {

			while (true) {
				try {
					//Get the command to know what's being sent
					Code cmd = (Code) fromServer.readObject();

					switch (cmd) {
					case SEND_DIR:
						//The direction is the other player's
						try {
							dir[1-SELF] = (Direction) fromServer.readObject();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case SEND_LOSS: 
						//Game ends when someone loses
						endGame(fromServer.readInt());
						//						lblStatus.setText("Game over");
						break;
					case SEND_START:
						//Sent at the beginning to start the game
						newGame();
						break;
					case SEND_ARR:
						//The game grid is being sent
//						grid = null;
						
						for (int i = 0; i < WIDTH; i++) {
							byte[] temp = new byte[HEIGHT];
							fromServer.readFully(temp);
							//Copy the game grid
							grid[i] = temp;
						}
						break;
					case SEND_LOC:
						//The location of a player is being sent
						int player = fromServer.readInt();
						locX[player] = fromServer.readInt();
						locY[player] = fromServer.readInt();
						break;
						
					default: 
						//Something unreadable was sent
						System.out.println("Invalid from server (" + cmd + ")");
					}

				} catch(IOException e){
					//					e.printStackTrace();
					lblStatus.setText("Disconnected");
					timer.stop();
					break;

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}


	class TimerListener implements ActionListener {
		//The clients still have a timer in case the server's slow
		//(Would this cause sync issues?)
		//(It's being overridden every 'tick' by the server)
		//(but it shows movement during disconnect)
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < NUM_PLAYERS; i++) {
				//Advance each player
				switch (dir[i]){
				case NORTH: locY[i]--;
				break;
				case EAST: locX[i]++;
				break;
				case SOUTH: locY[i]++;
				break;
				case WEST: locX[i]--;
				break;
				}

				//Current block is now dead
				grid[locX[i]][locY[i]] = TRUE;
				
				//Redraw the grid
				repaint();
			}

		}

	}

	class KeyboardPanel extends JPanel {
		//This gets key presses from player for direction changes
		public KeyboardPanel() {
			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					
					//Change direction according to the button pressed
					switch (e.getKeyCode()){
					case KeyEvent.VK_UP: 
						if (dir[SELF] != Direction.SOUTH)
						dir[SELF] = Direction.NORTH;
						break;
					case KeyEvent.VK_DOWN: 
						if (dir[SELF] != Direction.NORTH)
						dir[SELF] = Direction.SOUTH;
						break;
					case KeyEvent.VK_LEFT: 
						if (dir[SELF] != Direction.EAST)
						dir[SELF] = Direction.WEST;
						break;
					case KeyEvent.VK_RIGHT:
						if (dir[SELF] != Direction.WEST) 
						dir[SELF] = Direction.EAST;
						break;
					default:
						System.out.println("Invalid key");
						return;
					}
					
					try {
						//Tell server about direction change
						toServer.writeObject(Code.SEND_DIR);
						toServer.writeObject(dir[SELF]);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		protected void paintComponent(Graphics g){
			//For drawing the graphics
			
			//Start on a blank canvas
			g.clearRect(0, 0, HEIGHT*PPI, WIDTH*PPI);
			
			try {
				g.setColor(color[NUM_PLAYERS]);		//The color for dead blocks
				
				//Color the dead blocks (black trails left behind)
				for (int i = 0; i < grid.length; i++) {
					for (int j = 0; j < grid[i].length; j++) {
						if (grid[i][j] == TRUE)
							g.fillRect(i*PPI, j*PPI, PPI, PPI);							
					}
				}
				
				//Color the players current positions
				for (int i = 0; i < NUM_PLAYERS; i++) {
					g.setColor(color[i]);		//The players have different colors
					g.fillRect(locX[i]*PPI, locY[i]*PPI, PPI, PPI);
				}
				
			} catch (NullPointerException ex){
				System.out.println("Null grid");
				
			}
		}

	}

	class ButtonListener implements ActionListener {
		//Detects when the 'New Game' button is pressed
		@Override
		public void actionPerformed(ActionEvent e) {

			//Tell server you are ready to start
			try {
				toServer.writeObject(Code.SEND_START);
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

		}

	}
}
