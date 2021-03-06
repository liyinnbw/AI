package wuziqi;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.*;


public class MainUI extends JPanel{

   public int XO;
   public int YO;
   public int UNIT;
   public int GRID_ROWS;	//vertices, not squares
   public int GRID_COLS;	//vertices, not squares
   public int X_OFFSET;
   public int Y_OFFSET;
   public int SEARCH_TIME;	//in miliseconds
   public GameState gameState;

   public MainUI(){
	   X_OFFSET=8;
	   Y_OFFSET=30;
	   XO=50;
	   YO=60;
	   UNIT = 60;
	   GRID_ROWS = 15;	//vertices, not squares
	   GRID_COLS = 15;	//vertices, not squares
	   SEARCH_TIME = 3000; //miliseconds
	   newGame();
   }
   private void newGame(){
	   gameState = new GameState(GRID_ROWS,GRID_COLS,GameState.MAX_PLAYER);
   }
   private Point posOnGrid(Point p){
	   Point pos = new Point((int)((p.getX()-XO+UNIT/2.0)/UNIT),(int)((p.getY()-YO+UNIT/2.0)/UNIT));
	   return pos;
   }
   private Point posAbsolute(Point p){
	   Point pos = new Point((int)(p.getX()*UNIT+XO),(int)(p.getY()*UNIT+YO));
	   return pos;
   }

   @Override
   public void paint (Graphics g) {
	   super.paintComponent(g);			//this will clear the current frame
	   Graphics2D g2d = (Graphics2D) g;
	   g2d.setColor(Color.BLACK);
	   for(int i=0; i<GRID_ROWS-1; i++){
			for(int j=0; j<GRID_COLS-1; j++){
				int x = XO+j*UNIT-X_OFFSET;
				int y = YO+i*UNIT-Y_OFFSET;
				g2d.drawRect(x,y,UNIT,UNIT);
			}
	   }
	   
	   //System.out.println(gameState);

	   int[][] states = gameState.getGameState();   
	   int[] bitMaps = new int[GRID_COLS];
	   for(int i=0; i<bitMaps.length; i++){
		   bitMaps[i] = 1<<GRID_COLS-1-i;
	   }
	   for(int k=0; k<states.length; k++){
		   int state[] = states[k];
		   for(int i=0; i<state.length; i++){
			   int row = state[i];
			   for(int j=0; j<GRID_COLS; j++){
				   int bit = row & bitMaps[j];
				   if(bit!=0){
					   Point p = new Point(j,i);
					   Point ap = posAbsolute(p);
					   if(k==0){
						   g2d.setColor(Color.BLACK);
					   }else{
						   g2d.setColor(Color.WHITE);
					   }
					   int d = (int) (UNIT*0.8);
					   g2d.fillOval((int) ap.getX()-X_OFFSET-d/2, (int) ap.getY()-Y_OFFSET-d/2, d, d);
				   }
			   }
			}
		}
	  
	   if(!gameState.getMoves().empty()){
		   g2d.setColor(Color.RED);
		   int d = (int) (UNIT*0.2);
		   Point lastMove = gameState.getMoves().peek();
		   Point ap = posAbsolute(lastMove);
		   g2d.fillOval((int) ap.getX()-X_OFFSET-d/2, (int) ap.getY()-Y_OFFSET-d/2, d, d);
	   }
	   
   }
   
   public static void main(String[] args){
	  final JFrame mainFrame = new JFrame("Five In A Row");
	  final MainUI gameboard = new MainUI();
	  final JPanel statusPanel = new JPanel();
	//   statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	  mainFrame.add(statusPanel, BorderLayout.SOUTH);
	  statusPanel.setPreferredSize(new Dimension(mainFrame.getWidth(), 32));
	  statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
	  JLabel statusLabel = new JLabel("<html><div style='text-align: center;'>Your turn. Press [Backspace] to revert a step.</div></html>");
	  statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
	  statusPanel.add(statusLabel);

	  mainFrame.setSize(gameboard.XO*2+gameboard.GRID_COLS*gameboard.UNIT-gameboard.UNIT,
			  			gameboard.YO*2+gameboard.GRID_ROWS*gameboard.UNIT-gameboard.UNIT);
	  mainFrame.addWindowListener(new WindowAdapter() {
	     public void windowClosing(WindowEvent windowEvent){
	        System.exit(0);
	     }        
	  });
	  mainFrame.addMouseListener(new MouseAdapter() {
	     public void mousePressed(MouseEvent e) {
	    	if(gameboard.gameState.getCurrSide()!=gameboard.gameState.MAX_PLAYER){
	    		return;
	    	}else{
		        Point p = gameboard.posOnGrid(e.getPoint());
		        if(gameboard.gameState.addPiece((int)p.getX(), (int)p.getY())){
					System.out.println("mouse click "+p);
					
					//paint immediately so that user input is reflected
					// gameboard.paintImmediately(gameboard.getBounds());
					
					
			        int over = gameboard.gameState.isGameOver();
			        if(over!=-1){
			        	String message;
			        	if(over==gameboard.gameState.MAX_PLAYER) message = "Black Win";
			        	else if(over==gameboard.gameState.MIN_PLAYER) message = "White Win";
			        	else message = "Tie";
			        	int input = JOptionPane.showOptionDialog(mainFrame, message, "Message",JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,null,null,null);
			        	if(input == JOptionPane.OK_OPTION)
			        	{
			        		gameboard.newGame();
						}
						
			        }else{
						statusLabel.setText("<html><div style='text-align: center;'>AI Thinking...</div></html>");
						new SwingWorker<Void, Void>() {
							@Override
							protected void done() {
								// update UI
								int over = gameboard.gameState.isGameOver();
								if(over!=-1){
									String message;
									if(over==gameboard.gameState.MAX_PLAYER) message = "Black Win";
									else if(over==gameboard.gameState.MIN_PLAYER) message = "White Win";
									else message = "Tie";
									int input = JOptionPane.showOptionDialog(mainFrame, message, "Message",JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,null,null,null);
									if(input == JOptionPane.OK_OPTION)
									{
										gameboard.newGame();
									}
								}
								statusLabel.setText("<html><div style='text-align: center;'>Your turn. Press [Backspace] to revert a step.</div></html>");
								gameboard.repaint();
							}

							@Override
							protected Void doInBackground() throws Exception {
								//copy state so that it does not affect drawing
								GameState gameState = new GameState(gameboard.GRID_ROWS,gameboard.GRID_COLS,GameState.MAX_PLAYER);
								gameState.setGameState(gameboard.gameState.getGameState());
	  							GameTree agent = new GameTree(gameState, gameboard.SEARCH_TIME);
								
								//do work
								long start = System.currentTimeMillis();
								Point nextBestMove = agent.nextMove();
								long end = System.currentTimeMillis();
								System.out.println("ai move = "+nextBestMove+" move calculation time = "+(end-start)/1000.0+" s");

								//update current game state
								gameboard.gameState.addPiece((int)nextBestMove.getX(), (int)nextBestMove.getY());
								return null;
							}
						}.execute();
					}
			        gameboard.repaint();
		        } 
	    	}
	     }
	  });
	  
	  mainFrame.addKeyListener(new KeyAdapter() {
		  public void keyPressed(KeyEvent e) {
			  if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
				  System.out.println("revert move");
				  gameboard.gameState.revertOneMove();
				  if(gameboard.gameState.getCurrSide()==GameState.MIN_PLAYER){
					  gameboard.gameState.revertOneMove();
				  }
				  gameboard.repaint();
			  }
		  }
	  });
	  
	  mainFrame.add(gameboard);
	  mainFrame.setVisible(true); 
   }
   
}