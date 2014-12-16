package tron;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GameMenu extends JFrame {

	private final Color darkgreen = new Color(34,139,34);
	
	public GameMenu() {
		//Create the window
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Geese Battle");
		this.setResizable(false);
		this.setBackground(Color.CYAN);

		JPanel pMenu = new JPanel(new GridLayout(0,1));		//A panel on top of the window
		
		JLabel title = new JLabel("GEESE BATTLE", JLabel.CENTER);
		title.setFont(new Font("Serif",Font.BOLD, 16));
		title.setBackground(Color.CYAN);
		title.setOpaque(true);
		pMenu.add(title);
		
		//Button to lauch single player game
		JButton btnSingle = new JButton("One Player");
		btnSingle.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new SinglePlayer();
			}
		});
		//btnSingle.setBackground(darkgreen);
		pMenu.add(btnSingle);
		
		//Button to launch two player client
		JButton btnClient = new JButton("Multiplayer");
		btnClient.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new GeeseBattle();
			}
		});
		pMenu.add(btnClient);
		
		//Button to launch multiplayer server
		JButton btnHost = new JButton("Host");
		btnHost.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Server();
			}
		});
		pMenu.add(btnHost);

		this.add(pMenu);
		
		//Window
		this.setVisible(true);		
		this.setSize(300,300);
	}
	
	public static void main(String[] args) {
		new GameMenu();
	}

	
	
}
