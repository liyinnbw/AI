package chess;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameTree {
	
	public static class MaxComparator implements Comparator<TreeNode>{
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			if(o1.v<o2.v) return 1;
			if(o1.v==o2.v) return 0;
			else return -1;
		}
	}
	public static class MinComparator implements Comparator<TreeNode>{
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			if(o1.v<o2.v) return -1;
			if(o1.v==o2.v) return 0;
			else return 1;
		}
	}
	private class TreeNode{
		public GameState.Move nextMove;
		//public int moveCount;
		//public int[] livePieces;
		public int v;
		public int searchDepth;
	}
	private final static MaxComparator maxComparator = new MaxComparator();
	private final static MinComparator minComparator = new MinComparator();
	private GameState currState;	//the current game state
	private int depthLim;			//number of moves to look ahead
	private long timeLim;
	public HashMap<Integer, TreeNode> hm;
	private HashMap<Integer, Point> moveLib;	//key: currstate hash value, value: next state
	private ZobristHash hasher;
	private static boolean ENABLE_HASH = true;
	private static boolean ENABLE_MOVELIB = false;
	private static final int NULLMOVE_R = 2;	//must be multiples of 2
	private long startTime;
	private boolean endSearch;
	
	//stats for print
	public int hmQuerySuccessfulCount;
	public int maxDepthReached;
	public int nodesVisitedCount;
	public int branchesCount;
	public long totalSortingTime;
	//debug flag
	boolean debug;

	public long getTimeLim() {
		return timeLim;
	}

	public void setTimeLim(int t) {
		timeLim = (long)t;
	}

	public GameState getCurrState() {
		return currState;
	}

	public void setCurrState(GameState curr) {
		this.currState = curr;
	}
	
	public void setStateLibrary(){
		moveLib = new HashMap<Integer, Point>();
		String filename = "stateLib.txt";
		BufferedReader br = null;
		try {
			System.out.println("===== reading state library =====");
			br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				//TODO: store moves to library
			}
			System.out.println(moveLib.size());
			for (Map.Entry<Integer, Point> entry : moveLib.entrySet()){
				System.out.println(entry.getValue());
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.out.println("===== finished reading lib =====");
		}
	}
	
	public GameTree(GameState curr, int tLim){
		System.out.println("agent created");
		setCurrState(curr);
		setTimeLim(tLim);
		hasher = new ZobristHash(curr.ROWS, curr.HEX_COLS, curr.SQUARE_STATES);
		hm = new HashMap<Integer, TreeNode>();
		if(ENABLE_MOVELIB){
			setStateLibrary();
		}
		debug = false;
	}
	
	public GameState.Move nextMove(){
		/*
		//clean outdated hash
		int outdatedCount = 0;
		
		int livePieces[] = currState.getLivePieces();
		//int currTotalMoveCount = currState.getMoves().size();
		
		Iterator<Map.Entry<Integer, TreeNode>> it = hm.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Integer, TreeNode> entry = it.next();
			int[] entryLivePieces = entry.getValue().livePieces;
			for(int i=0; i<entryLivePieces.length-1; i++){
				
				if(entryLivePieces[i]>livePieces[i]){
					it.remove();
					outdatedCount++;
					break;
				}
			}
			//if(entry.getValue().moveCount<currTotalMoveCount){
			//	it.remove();
			//	outdatedCount++;
			//}
		}
		System.out.println("cleared outdated hash = "+outdatedCount+" current hash size = "+hm.size());
		*/
		hm = new HashMap<Integer, TreeNode>();

		hmQuerySuccessfulCount = 0;
		maxDepthReached = 0;
		nodesVisitedCount = 0;
		branchesCount = 0;
		totalSortingTime = 0;
		endSearch = false;
		
		TreeNode bestNext = null;
		startTime = System.currentTimeMillis();
		for(int depth = 1; ; depth++){
			depthLim = depth;
			bestNext = minMaxAlphaBeta(currState,depth-1,0-Integer.MAX_VALUE, Integer.MAX_VALUE, true);
			maxDepthReached = depth;
			long currTime = System.currentTimeMillis();
			if((currTime - startTime > timeLim) || bestNext.v==GameState.MAX_STATE_VALUE || bestNext.v==GameState.MIN_STATE_VALUE || endSearch) break;
		}
		
		System.out.println("Reuse saved nodes = "+hmQuerySuccessfulCount+"/"+hm.size()+" max depth reached = "+maxDepthReached+" average branching = "+branchesCount/nodesVisitedCount);
		System.out.println("sorting time = "+totalSortingTime/1000.0+" s");
		hm = null;	//this would immediately trigger garbage collection to release memory
		return bestNext.nextMove;
	}
	public List<GameState.Move> sortMoves(GameState curr, List<GameState.Move> nextPossibleMoves, int side){
		List<TreeNode> sortable = new ArrayList<TreeNode>();
		
		//evaluate all moves
		for(GameState.Move m : nextPossibleMoves){
			curr.makeMove(m);
			TreeNode t = new TreeNode();
			t.nextMove = m;
			t.v = curr.evaluate();
			curr.revertOneMove();
			sortable.add(t);
		}
		
		//sort moves by value
		if(side == GameState.MAX_PLAYER){
			Collections.sort(sortable,maxComparator);
		}else{
			Collections.sort(sortable,minComparator);
		}
		List<GameState.Move> sortedMoves = new ArrayList<GameState.Move>();
		for(int i=0; i<sortable.size(); i++){
			sortedMoves.add(sortable.get(i).nextMove);
		}
		return sortedMoves;
	}

	public TreeNode minMaxAlphaBeta(GameState curr, int depth, int alpha, int beta, boolean useNullMove){
		useNullMove = false;
		nodesVisitedCount++;
		
		GameState.Move previousBest = null;
		//query hash table
		if(ENABLE_HASH){	
			TreeNode queryRoot = hm.get(hasher.hash(curr));
			if(queryRoot!=null){
				if(queryRoot.searchDepth>=depth && !(depthLim-depth == 1 && queryRoot.nextMove==null)) {
					hmQuerySuccessfulCount++;
					return queryRoot;
				}else{
					previousBest = queryRoot.nextMove;
				}
			}
		}

		List<GameState.Move> nextPossibleMoves = curr.nextPossibleMoves();
		
		long start = System.currentTimeMillis();
		//order search by resultant state value
		nextPossibleMoves = sortMoves(curr, nextPossibleMoves, curr.getCurrSide());
		long end = System.currentTimeMillis();
		totalSortingTime += end-start;
		
		//if previous less deeper search result available, prioritize it as first search since it is likely still be the best
		if(previousBest!=null){
			if(nextPossibleMoves.remove(previousBest)){
				nextPossibleMoves.add(0, previousBest);
			}
		}
		
		TreeNode root = new TreeNode();	//the node to return
		//root.livePieces = new int[curr.getLivePieces().length];
		GameState.Move selectedMove = null;		//the state selected
		int selectedSearchDepth = 0;
		if(nextPossibleMoves.size()==0){
			root.nextMove = null;
			//System.arraycopy(curr.getLivePieces(), 0, root.livePieces, 0, root.livePieces.length);
			curr.getLivePieces().clone();
			root.v=curr.evaluate();
			root.searchDepth = Integer.MAX_VALUE;
		}
		else if(nextPossibleMoves.size()==1 && (depthLim-depth)==1){
			root.nextMove = nextPossibleMoves.get(0);
			endSearch = true;
			return root;
		}
		else if(curr.getCurrSide() == GameState.MAX_PLAYER){
			int max = 0;
			boolean maxNotInit = true;
			
			if(depth > 0 && (depthLim - depth>=3) && useNullMove){
				//make null move
				curr.makeNullMove();
				TreeNode nullMoveBestNext = minMaxAlphaBeta(curr, depth-1-NULLMOVE_R, alpha, beta, !useNullMove);
				curr.revertNullMove();
				
				if(nullMoveBestNext.v>=beta || nullMoveBestNext.v==GameState.MAX_STATE_VALUE){
					//System.out.println("null move pruning successful at depth = "+depth);
					root.nextMove = null;
					//System.arraycopy(curr.getLivePieces(), 0, root.livePieces, 0, root.livePieces.length);
					root.v = nullMoveBestNext.v;
					root.searchDepth = Math.max(depth, nullMoveBestNext.searchDepth);
					if(ENABLE_HASH){
						hm.put(hasher.hash(curr), root);
					}
					branchesCount++;
					return root;
				}
			}
			
			
			
			for(GameState.Move m: nextPossibleMoves){
				branchesCount++;
				
				curr.makeMove(m);
				int value = 0;
				int searchDepth = depth;
				if(depth<=0){
					value = curr.evaluate();		
				}else{
					TreeNode bestNext = minMaxAlphaBeta(curr, depth-1, alpha, beta, !useNullMove);
					value = bestNext.v;
					searchDepth = Math.max(searchDepth, bestNext.searchDepth);
				}
				curr.revertOneMove();
				
				if(maxNotInit){
					max = value;
					selectedMove = m; 
					selectedSearchDepth = searchDepth;
					maxNotInit = false;
				}else if(value>max){
					max = value;
					selectedMove = m; 
					selectedSearchDepth = searchDepth;
				}
				
				if(max>=beta || max==GameState.MAX_STATE_VALUE){
					break;
				}else{
					if(max>alpha){
						alpha = max;
					}
				}
				
				long currTime = System.currentTimeMillis();
				if(currTime - startTime > timeLim) break;
			}
			root.nextMove = selectedMove;
			//System.arraycopy(curr.getLivePieces(), 0, root.livePieces, 0, root.livePieces.length);
			root.v = max;
			root.searchDepth = selectedSearchDepth;
			
		}
		else{
			int min = 0;
			boolean minNotInit = true;
			
			
			if(depth>0 && (depthLim - depth>=3) && useNullMove){
				//make null move
				curr.makeNullMove();
				TreeNode nullMoveBestNext = minMaxAlphaBeta(curr, depth-1-NULLMOVE_R, alpha, beta, !useNullMove);
				curr.revertNullMove();
				
				if(nullMoveBestNext.v<=alpha || nullMoveBestNext.v==GameState.MIN_STATE_VALUE){
					//System.out.println("null move pruning successful at depth = "+depth);
					root.nextMove = null;
					//System.arraycopy(curr.getLivePieces(), 0, root.livePieces, 0, root.livePieces.length);
					root.v = nullMoveBestNext.v;
					root.searchDepth = Math.max(depth, nullMoveBestNext.searchDepth);
					if(ENABLE_HASH){
						hm.put(hasher.hash(curr), root);
					}
					branchesCount++;
					return root;
				}
			}
			
			
			for(GameState.Move m: nextPossibleMoves){
				branchesCount++;
				
				curr.makeMove(m);
				int value = 0;
				int searchDepth = depth;
				if(depth<=0){
					value = curr.evaluate();
				}else{
					TreeNode bestNext = minMaxAlphaBeta(curr, depth-1, alpha, beta, !useNullMove);
					value = bestNext.v;
					searchDepth = Math.max(searchDepth, bestNext.searchDepth);
				}
				curr.revertOneMove();
				
				if(minNotInit){
					min = value;
					selectedMove = m; 
					selectedSearchDepth = searchDepth;
					minNotInit = false;
				}else if(value<min){
					min = value;
					selectedMove = m; 
					selectedSearchDepth = searchDepth;
				}
				
				if(min<=alpha || min==GameState.MIN_STATE_VALUE){
					break;
				}else{
					if(min<beta){
						beta = min;
					}
				}
				long currTime = System.currentTimeMillis();
				if(currTime - startTime > timeLim) break;
			}
			root.nextMove = selectedMove;
			//System.arraycopy(curr.getLivePieces(), 0, root.livePieces, 0, root.livePieces.length);
			root.v = min;
			root.searchDepth = selectedSearchDepth;
			
		}
		if(ENABLE_HASH){
			hm.put(hasher.hash(curr), root);
		}
		return root;
	}
}
