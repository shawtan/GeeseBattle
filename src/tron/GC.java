package tron;
import java.awt.Color;

/*
 * Just some constants used throughout the game
 * 
 * by Shaw Tan
 * 12/09/2012
 */

public interface GC {

	final int FALSE = 0;
	final int TRUE = 1;
	
	//Direction codes
	final int NORTH = 3;
	final int EAST = 4;
	final int SOUTH = 5;
	final int WEST = 6;
	
	final int NUM_PLAYERS = 2;		//Number of players
	
	//Game dimensions
	final int WIDTH = 100;
	final int HEIGHT = 100;
	
	final int PPI = 4; 		//Pixel density
	final int TICK = 100;	//Timer speed
	
	final int PORT = 8002;			//The network port the game uses
	
	//Colors for each player, and dead blocks
	final Color[] color = {Color.BLUE, Color.RED, Color.BLACK};
	
	//Communications announcement codes
	final int SEND_START = 10;	//Game starting
	final int SEND_DIR = 11;	//Send new direction
	final int SEND_ARR = 12;	//Send board
	final int SEND_LOSS = 13;	//A player lost
	final int SEND_LOC = 14; 	//Send location
	
}