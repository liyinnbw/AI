package test;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.List;
import java.util.Stack;

import chess.GameState;
import chess.Move;
import org.junit.Before;
import org.junit.Test;

import chess.GameTree;

public class TestGameState {
	public static final int ROWS = 10;
	public static final int COLS = 9;
	public GameState g;
	@Before
	public void init() {
		g = new GameState(ROWS,COLS,GameState.MIN_PLAYER);
	}
	@Test
	public void testNextPossibleMoves(){
		/*int[] state = {
		4,	14,	2,	1,	0,	1,	2,	3,	4,	14,	14,	14,	14,	14,	14,	14,	
		14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
		3,	5,	14,	14,	14,	14,	14,	5,	14,	14,	14,	14,	14,	14,	14,	14,	
		6,	14,	6,	14,	6,	14,	6,	14,	6,	14,	14,	14,	14,	14,	14,	14,	
		14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
		14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
		13,	14,	13,	14,	13,	14,	13,	14,	13,	14,	14,	14,	14,	14,	14,	14,	
		14,	12,	14,	14,	12,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
		14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
		11,	10,	9,	8,	7,	8,	9,	10,	11,	14,	14,	14,	14,	14,	14,	14
		};
		g.setState(state);*/
		Move m1 = new Move(7, 7, 7, 4, 14);
		g.makeMove(m1);
		Move m2 = new Move(0, 1, 2, 0, 14);
		g.makeMove(m2);
		g.setCurrSide(GameState.MIN_PLAYER);
		List<Move> nexts = g.nextPossibleMoves();
		boolean findMove = false;
		for(Move m: nexts){
			if(m.fromC == 4 && m.fromR == 7 && m.toC == 4 && m.toR == 3 && m.rmPiec == GameState.Z){
				findMove = true;
				break;
			}
		}
		assertTrue(findMove);
		
		int[] state2 = {
			4,	14,	2,	1,	0,	1,	2,	3,	4,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			3,	5,	14,	14,	14,	14,	14,	5,	14,	14,	14,	14,	14,	14,	14,	14,	
			6,	14,	6,	14,	6,	14,	6,	14,	6,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			13,	14,	13,	14,	13,	14,	13,	14,	13,	14,	14,	14,	14,	14,	14,	14,	
			14,	12,	14,	14,	12,	14,	10,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			11,	10,	9,	8,	7,	8,	9,	11,	14,	14,	14,	14,	14,	14,	14,	14
		};
		g.setState(state2);
		g.setCurrSide(GameState.MIN_PLAYER);
		nexts = g.nextPossibleMoves();
		findMove = false;
		for(Move m: nexts){
			if(m.fromC == 7 && m.fromR == 9 && m.toC == 7 && m.toR == 2 && m.rmPiec == GameState.P){
				findMove = true;
				break;
			}
		}
		assertTrue(findMove);
		
		int[] state3 = {
				14,	14,	2,	1,	0,	1,	2,	14,	4,	14,	14,	14,	14,	14,	14,	14,	
				14,	14,	14,	4,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
				14,	14,	3,	14,	14,	11,	3,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
				6,	14,	6,	14,	6,	14,	6,	14,	6,	14,	14,	14,	14,	14,	14,	14,	
				14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
				14,	14,	14,	14,	14,	14,	13,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
				13,	14,	13,	14,	13,	14,	14,	5,	13,	14,	14,	14,	14,	14,	14,	14,	
				14,	12,	14,	14,	9,	14,	10,	12,	14,	14,	14,	14,	14,	14,	14,	14,	
				14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
				14,	11,	14,	8,	7,	8,	9,	14,	14,	14,	14,	14,	14,	14,	14,	14
		};
		g.setState(state3);
		g.setCurrSide(GameState.MIN_PLAYER);
		nexts = g.nextPossibleMoves();
		findMove = false;
		for(Move m: nexts){
			if(m.fromC == 5 && m.fromR == 2 && m.toC == 6 && m.toR == 2 && m.rmPiec == GameState.M){
				findMove = true;
				break;
			}
		}
		assertTrue(findMove);
	}
	
	@Test
	public void testMirrorState(){
		int[] state = {
			4,	14,	2,	1,	0,	1,	2,	3,	4,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			3,	5,	14,	14,	14,	14,	14,	5,	14,	14,	14,	14,	14,	14,	14,	14,	
			6,	14,	6,	14,	6,	14,	6,	14,	6,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			13,	14,	13,	14,	13,	14,	13,	14,	13,	14,	14,	14,	14,	14,	14,	14,	
			14,	12,	14,	14,	12,	14,	10,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	14,	
			11,	10,	9,	8,	7,	8,	9,	11,	14,	14,	14,	14,	14,	14,	14,	14
		};
		g.setState(state);
		System.out.println(g);
		g.mirrorState();
		System.out.println(g);
		
	}
	@Test
	public void testMirrorMove(){
		Move m = new Move(1,2,5,6,10);
		System.out.println(m);
		System.out.println(GameState.getMirrorMove(m));
	}
}
