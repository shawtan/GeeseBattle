package tron;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/*
 * This is the server that the game players connect with
 * It is where the game goes on
 * The server then sends the game state to the clients	
 * 
 * by Shaw Tan
 * 12/09/2012
 */

public class Server extends JFrame implements GC{

	
	//The location and direction of both players
	private int dir[] = new int[2];
	private int locX[] = new int[2];
	private int locY[] = new int[2];
	
	private byte[][] grid;		//The grid saves which paths are occupied already
	private Timer timer;		//Timer to move the cars
	private JTextArea jta;		//Text area displays status messages

	//I/O with the clients
	private DataInputStream[] input = new DataInputStream[NUM_PLAYERS];
	private DataOutputStream[] output = new DataOutputStream[NUM_PLAYERS];

	public static void main (String args[]){
		new Server();
	}

	public Server() {

		//Create the window
		this.setSize(500, 300);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//Add the text area
		jta = new JTextArea();
		this.add(new JScrollPane(jta));

		//Initiate the timer
		timer = new Timer(TICK,new TimerListener());
		this.setVisible(true);

		ServerSocket s;
		try {
			//Listen at port for connection
			s = new ServerSocket(PORT);
			println("Server Started");

			// Blocks until a connection occurs:
			for (int i = 0; i < NUM_PLAYERS; i++) {
				//Waits for players until full

				println("Waiting for players. (" + i + "/" + NUM_PLAYERS + ")");
				Socket socket = s.accept();
				println("Player " + (i+1) + " connected.");
				
				//I/O for each client
				output[i] = new DataOutputStream(socket.getOutputStream());
				input[i] = new DataInputStream(socket.getInputStream());

				//Tell client which player they are
				output[i].writeInt(i);
				output[i].flush();

				//A seperate thread for each player
				new Thread(new HandleInput(i)).start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void println(String str){
		//Printing to the text area
		jta.append(str + "\n");
	}
	
	private void newGame(){
		//Set initial game conditions
		
		//Start a bit away from side of screen
		locX[0] = 0+2;
		locX[1] = GC.WIDTH-3;	
		//Start in middle of window
		locY[0] = GC.HEIGHT /2;
		locY[1] = GC.HEIGHT /2;
		//Players go in opposite directions
		dir[0] = EAST;
		dir[1] = WEST;
		//Game size
		grid = new byte[GC.WIDTH][GC.HEIGHT];
		
		//Start timing, begin moving players
		timer.start();
		
		println("New game started");
	}

	void endGame(int player){
		//When a player crashes and loses
		
		//Tell each player who lost
		for (int i = 0; i < NUM_PLAYERS; i++) {
			try {
				output[i].writeInt(SEND_LOSS);
				output[i].writeInt(player);
				output[i].flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		timer.stop();		//Stop the game (since it's only 2p)
		
		if (player < NUM_PLAYERS)
			println("Player " + (player+1) + " lost.");
		else
			println("Tie game");		//When both crash at the same time
	}


	class HandleInput implements Runnable {
		//Handles the input from clients
		
		private int player;		//Input is from this player

		public HandleInput(int player) {
			this.player = player;
		}

		@Override
		public void run() {

			while (true){
				try {

					//Get the command from the player
					int cmd = input[player].readInt();

					//Interpret the command
					switch (cmd){
					case SEND_DIR:
						//The player changed directions
						int in = input[player].readInt();	//Get the new direction
						dir[player] = in;
						for (int i = 0; i < NUM_PLAYERS; i++) {
							//Tell the other players about direction change
							if (i != player){
								output[i].writeInt(SEND_DIR);
								output[i].writeInt(in);
								output[i].flush();
							}
						}
						break;
					case SEND_START:
						//Player is ready to start the game
						if (!timer.isRunning()){	//Check if going is already on-going
							for (int i = 0; i < NUM_PLAYERS; i++) {
								//Tell the players to start
								output[i].writeInt(SEND_START);
								output[i].flush();
							}
							newGame();	//Start the game
							break;
						}
					default:
						//Otherwise its an unknown command
						println("Invalid input from client " + (player + 1));
					}


				} catch (IOException e) {
					//Connection error
					println("Player " + (player+1) + " disconnected.");
					break;
				}
			}
		}
	}

	class TimerListener implements ActionListener {
		//Moves players every tick
		
//		int count = 0;
		boolean[] lost = new boolean[NUM_PLAYERS];		//Keeps track of if a player has lost

		@Override
		public void actionPerformed(ActionEvent e) {
			//On tick:
			
			for (int i = 0; i < NUM_PLAYERS; i++) {
				//Move each player 1 in the direction they're facing

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
				
				try{
					//See if they're on a dead block
					if (grid[locX[i]][locY[i]] == TRUE){
						//That means they crashed and lost
						lost[i] = true;
					} else {
						//Otherwise, make the block they're on dead
						grid[locX[i]][locY[i]] = TRUE;
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					//Hit the border means Dead
					lost[i] = true;
				}

			}

			if (lost[0] && lost[1])
				//Both players crashed at once
				endGame(NUM_PLAYERS);	//Tie game
			else if (lost[0])
				endGame(0);		//Player 1 lost
			else if (lost[1])
				endGame(1);		//Player 2 lost

			for (int i = 0; i < lost.length; i++) {
				//Reset the count
				lost[i] = false;
			}


			for (int i = 0; i < NUM_PLAYERS; i++) {
				//Tell players the game state
				try {
					output[i].writeInt(SEND_ARR);	//Announce sending game board array
					//Send the game board
					for (int j = 0; j < GC.HEIGHT; j++) {
						output[i].write(grid[j]);
					}

					for (int j = 0; j < NUM_PLAYERS; j++) {
						//Also send the location and direction of the players
						output[i].writeInt(SEND_LOC);
						output[i].writeInt(j);
						output[i].writeInt(locX[j]);
						output[i].writeInt(locY[j]);

					}

					output[i].flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}

	}

}



