package tron;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

/*
 * This is the one player version
 * It's not as fun, or polished
 * 
 * by Shaw Tan
 * 12/09/2012
 */

public class SinglePlayer extends JFrame{

	public static enum Direction{
		NORTH, EAST, SOUTH, WEST
	}
	
	final int gameWidth = 80;
	final int gameHeight = 60;

	final int PPI = 8; 		//Pixel density
	final int TICK = 100;	//Timer speed
	
	final static int PORT = 8002;			//The network port the game uses

	final int NUM_PLAYERS = 2;		//Number of players
	
	//Colors for each player, and dead blocks
	final Color[] color = {Color.BLUE, Color.RED, new Color(0xa95b26)};
	
	//Communications announcement codes
	public static enum Code{
		SEND_START, SEND_DIR, SEND_ARR, SEND_LOSS, SEND_LOC
	}
	
	private int SELF;		//Which player this is
	
//	private byte[][] grid;
	
	private Timer timer;		//A timer for when the server's slow
	
	private KeyboardPanel kp;	//Player interacts with keyboard

	private JButton btnNew;		//Button used to start the game
	private JLabel lblStatus;	//Label to display winners for status messages
	
	//**************************************
	//Direction and location of the players
	private Direction dir;
	private int locX,locY;
	
	//Game grid
	private boolean[][] grid;

	private Image[] goose;
	private Image bg;
	
	public static void main (String args[]){
		
		new SinglePlayer();

	}

	public SinglePlayer(){


		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Honk Honk");
//		this.setResizable(false);

		
		JPanel pMenu = new JPanel();
		btnNew = new JButton("New Game");
		btnNew.addActionListener(new ButtonListener());
		btnNew.setFocusable(false);
		pMenu.setBackground(Color.GRAY);
		pMenu.add(btnNew);
		this.add(pMenu,BorderLayout.NORTH);

		kp = new KeyboardPanel();
		kp.setFocusable(true);
		this.add(kp,BorderLayout.CENTER);
		
		
		timer = new Timer(50, new TimerListener());
//		timer.start();
//		
		locX = gameWidth;
//		locY = HEIGHT /2;
//		dir = WEST;
//		grid = new boolean[WIDTH][HEIGHT];


		this.setVisible(true);
		this.setSize(PPI*gameWidth,PPI*gameHeight+this.getInsets().top+pMenu.getHeight());


		grid = new boolean[gameWidth][gameHeight];
		loadImage();
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
	
	
	private void newGame(){
		locX = gameWidth-3;
		locY = gameHeight /2;
		dir = Direction.WEST;
		grid = new boolean[gameWidth][gameHeight];
		timer.start();
		repaint();
	}
	
	class TimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			switch (dir){
			case NORTH: locY--;
				break;
			case EAST: locX++;
				break;
			case SOUTH: locY++;
				break;
			case WEST: locX--;
				break;
			}
			try{
			if (grid[locX][locY]){
				timer.stop();
				System.out.println("Dead (Hit self)");
				JOptionPane.showMessageDialog(null, "You died!");
			} else {
				grid[locX][locY]=true;
				kp.callPaint();
			}
			} catch (ArrayIndexOutOfBoundsException ex) {
				//Dead
				timer.stop();
				System.out.println("Dead (Out of bounds)");
				JOptionPane.showMessageDialog(null, "You died!");
				
			}
			
		}
		
	}

	class KeyboardPanel extends JPanel {
		public KeyboardPanel() {
			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					switch (e.getKeyCode()){
					case KeyEvent.VK_UP: 
						dir = Direction.NORTH;
						System.out.println("Up key");
						break;
					case KeyEvent.VK_DOWN: 
						dir = Direction.SOUTH;
						break;
					case KeyEvent.VK_LEFT: 
						dir = Direction.WEST;
						break;
					case KeyEvent.VK_RIGHT: 
						dir = Direction.EAST;
						break;
					case KeyEvent.VK_SPACE:
						if (!timer.isRunning()){
							newGame();
						}
					default:
						System.out.println("Invalid key");
						return;
					}
				}
			});
		}
		
		protected void paintComponent(Graphics g){
			
			if (!timer.isRunning()){
				return;
			}
			
			g.drawImage(bg, 0, 0, gameWidth*PPI, gameHeight*PPI, null);
			
			g.setColor(color[2]);
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					if (grid[i][j]){
						g.fillRect(i*PPI, j*PPI, PPI, PPI);
					}
				}
			}
			
			g.setColor(color[0]);
			g.fillRect(locX*PPI, locY*PPI, PPI, PPI);
			
			int d = 0;
			switch (dir){
			case NORTH: d = 0;
				break;
			case EAST: d = 1;
				break;
			case SOUTH: d = 2;
				break;
			case WEST: d = 3;
				break;
			}
			
			g.drawImage(goose[d], locX*PPI-5*PPI, locY*PPI-5*PPI, 10*PPI, 10*PPI, null);
			
		}
		
		public void callPaint(){
			repaint();
		}
	}
	
	class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			newGame();
			
		}
		
	}
}
