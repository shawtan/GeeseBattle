package tron;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * This is the one player version
 * It's not as fun, or polished
 * 
 * by Shaw Tan
 * 12/09/2012
 */

public class SinglePlayer extends JFrame{

	final int NORTH = 1;
	final int EAST = 2;
	final int SOUTH = 3;
	final int WEST = 4;
	
	final int WIDTH = 100;
	final int HEIGHT = 100;
	final int PPI = 4; 		//Pixel density
	
	final Color color = Color.BLUE;

	private int dir;
	private int locX,locY;
	private boolean[][] grid;
	private Timer timer;
	private KeyboardPanel kp;
	private JButton btnNew;

	public static void main (String args[]){
		new SinglePlayer();

	}

	public SinglePlayer(){


		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Tron");
		this.setResizable(false);

		
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
		locX = WIDTH;
//		locY = HEIGHT /2;
//		dir = WEST;
//		grid = new boolean[WIDTH][HEIGHT];


		this.setVisible(true);		
		System.out.println(this.getInsets());
		this.setSize(PPI*WIDTH,PPI*HEIGHT+this.getInsets().top+pMenu.getHeight());

	}
	
	private void newGame(){
		locX = WIDTH-3;
		locY = HEIGHT /2;
		dir = WEST;
		grid = new boolean[WIDTH][HEIGHT];
		repaint();
		timer.start();
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
			} else {
				grid[locX][locY]=true;
				kp.callPaint();
			}
			} catch (ArrayIndexOutOfBoundsException ex) {
				//Dead
				timer.stop();
				System.out.println("Dead (Out of bounds)");
				
			}
			
		}
		
	}

	class KeyboardPanel extends JPanel {
		public KeyboardPanel() {
			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					switch (e.getKeyCode()){
					case KeyEvent.VK_UP: 
						dir = NORTH;
						System.out.println("Up key");
						break;
					case KeyEvent.VK_DOWN: 
						dir = SOUTH;
						break;
					case KeyEvent.VK_LEFT: 
						dir = WEST;
						break;
					case KeyEvent.VK_RIGHT: 
						dir = EAST;
						break;
					default:
						System.out.println("Invalid key");
						return;
					}
				}
			});
		}
		
		protected void paintComponent(Graphics g){
			g.setColor(color);
			g.fillRect(locX*PPI, locY*PPI, PPI, PPI);
			
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
