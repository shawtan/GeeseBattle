package tron;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/*
 * This is the game client
 * It is what the players actually use to play the game
 * It has graphics
 * 
 * by Shaw Tan
 * Dec. 26 2014
 */


public class Client extends JFrame implements GC {
	
	

	private int SELF;		//Which player this is
	
	//Direction and location of the players
	private int dir[] = new int[2];
	private int locX[] = new int[2];
	private int locY[] = new int[2];
	
	//Game grid
	private byte[][] grid;
	
	private Image[] goose;
	private Image bg;
	
	private Timer timer;		//A timer for when the server's slow
	
	private KeyboardPanel kp;	//Player interacts with keyboard

	private JButton btnNew;		//Button used to start the game
	private JLabel lblStatus;	//Label to display winners for status messages

	//I/O with server
	private DataInputStream fromServer;
	private DataOutputStream toServer;

	public static void main (String args[]){
		new Client();
	}

	public Client(){

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
		this.setSize(PPI*GC.GAME_WIDTH,PPI*GC.GAME_HEIGHT+this.getInsets().top+pMenu.getHeight());

		timer = new Timer(TICK, new TimerListener());
		
		try {
			//Attempt to connect to server
			
			//Find out where to connect
			Socket socket = new Socket(JOptionPane.showInputDialog("Enter the server name:"), PORT);
//			Socket socket = new Socket("localhost",PORT);
			
			//I/O with server
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());

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

		loadImage();
		

		new Thread(new HandleInput()).start();
		
	}

	private void newGame(){
		//Initialize game
		//(should be the same as what the server has)
		locX[0] = 0+2;
		locX[1] = GC.GAME_WIDTH-3;
		locY[0] = GC.GAME_HEIGHT /2;
		locY[1] = GC.GAME_HEIGHT /2;
		dir[0] = EAST;
		dir[1] = WEST;
		grid = new byte[GC.GAME_WIDTH][GC.GAME_HEIGHT];
		repaint();
		timer.start();
		lblStatus.setText("Game started");
		System.out.println("Starting game");
	}
	
	private void loadImage(){
		
		bg = Toolkit.getDefaultToolkit().createImage("rsc/grass.jpg");
		
		goose = new Image[6];
		goose[0] = Toolkit.getDefaultToolkit().createImage("rsc/goose-upL.gif");
		goose[1] = Toolkit.getDefaultToolkit().createImage("rsc/goose-right.gif");
		goose[2] = Toolkit.getDefaultToolkit().createImage("rsc/goose-downL.gif");
		goose[3] = Toolkit.getDefaultToolkit().createImage("rsc/goose-left.gif");
		goose[4] = Toolkit.getDefaultToolkit().createImage("rsc/goose-upR.gif");
		goose[5] = Toolkit.getDefaultToolkit().createImage("rsc/goose-downR.gif");
		

	}
	
	void endGame(int player){
		//Someone lost
		timer.stop();
		
		if (player<NUM_PLAYERS)
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
					int cmd = fromServer.readInt();

					switch (cmd) {
					case SEND_DIR:
						//The direction is the other player's
						dir[1-SELF] = fromServer.readInt();
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
						
						for (int i = 0; i < GC.GAME_WIDTH; i++) {
							byte[] temp = new byte[GC.GAME_HEIGHT];
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
						if (dir[SELF] != SOUTH)
						dir[SELF] = NORTH;
						break;
					case KeyEvent.VK_DOWN: 
						if (dir[SELF] != NORTH)
						dir[SELF] = SOUTH;
						break;
					case KeyEvent.VK_LEFT: 
						if (dir[SELF] != EAST)
						dir[SELF] = WEST;
						break;
					case KeyEvent.VK_RIGHT:
						if (dir[SELF] != WEST) 
						dir[SELF] = EAST;
						break;
					default:
						System.out.println("Invalid key");
						return;
					}
					
					try {
						//Tell server about direction change
						toServer.writeInt(SEND_DIR);
						toServer.writeInt(dir[SELF]);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		protected void paintComponent(Graphics g){
			//For drawing the graphics

			if (!timer.isRunning()){
				return;
			}
			
			//Start on a blank canvas
			g.drawImage(bg, 0, 0, GAME_WIDTH*PPI, GAME_HEIGHT*PPI, null);
			
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
					int d = 0;
					switch (dir[i]){
					case NORTH: d = 0;
						break;
					case EAST: d = 1;
						break;
					case SOUTH: d = 2;
						break;
					case WEST: d = 3;
						break;
					}
					
					g.drawImage(goose[d], locX[i]*PPI-5*PPI, locY[i]*PPI-5*PPI, 10*PPI, 10*PPI, null);
					
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
				toServer.writeInt(SEND_START);
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

		}

	}
}
