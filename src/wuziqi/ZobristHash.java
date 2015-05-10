package wuziqi;

import java.util.Random;

public class ZobristHash {
	private int [][] hashtable; //[][0] = maxPlayer, [][1] = min player, [][2] = empty
	public ZobristHash(int gameboardSize, int gamesquareStates){
		initTable(gameboardSize,gamesquareStates);
	}
	public void initTable(int gameboardSize, int gamesquareStates){
		hashtable = new int[gameboardSize][gamesquareStates];
		Random rand = new Random();
		for(int i=0; i<gameboardSize; i++){
			for(int j=0; j<gamesquareStates; j++){
				hashtable[i][j] = rand.nextInt();
			}
		}
	}
	
	//currently this hashfunction is hardcoded
	public int hash(GameState s){
		int hashValue = 0;
		int[][] state = s.getGameState();
		int[] maxState = state[s.MAX_PLAYER];
		int[] minState = state[s.MIN_PLAYER];
		
		for(int i=0; i<hashtable.length; i++){
			int r = i/s.getCols();
			int c = i%s.getCols();
			int maxPlayerBit = s.getBit(maxState, r, c);
			int minPlayerBit = s.getBit(minState, r, c);
			if(maxPlayerBit==0 && minPlayerBit==0){
				hashValue ^= hashtable[i][2];
			}else if (maxPlayerBit!=0){
				hashValue ^= hashtable[i][0];
			}else{
				hashValue ^= hashtable[i][1];
			}
		}
		return hashValue;
	}
}
