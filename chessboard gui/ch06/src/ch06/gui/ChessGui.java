package ch06.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch06.logic.ChessGame;
import ch06.logic.Move;
import ch06.logic.MoveValidator;
import ch06.logic.Piece;

/**
 * all x and y coordinates point to the upper left position of a component all
 * lists are treated as 0 being the bottom and size-1 being the top piece
 * 
 */
public class ChessGui extends JPanel {
	
	private static final long serialVersionUID = -8207574964820892354L;
	
	private static final int BOARD_START_X = 299;
	private static final int BOARD_START_Y = 49;

	private static final int SQUARE_WIDTH = 50;
	private static final int SQUARE_HEIGHT = 50;

	private static final int PIECE_WIDTH = 48;
	private static final int PIECE_HEIGHT = 48;
	
	private static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH/2.0 - PIECE_WIDTH/2.0);
	private static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT/2.0 - PIECE_HEIGHT/2.0);
	
	private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
	private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

	private Image imgBackground; // background image
	private JLabel lblGameState; // label displaying the current game state
	
	private ChessGame chessGame; 
	
	// all gui game pieces of the running game
	// the gui game pieces wrap the actual game pieces of the chess game
	private List<GuiPiece> guiPieces = new ArrayList<GuiPiece>(); 

	private GuiPiece dragPiece; // currently dragged game piece

	private Move lastMove; // the last executed move (used for highlighting)

	public ChessGui() {
		this.setLayout(null);

		// background
		URL urlBackgroundImg = getClass().getResource("/ch06/gui/img/bo.png");
		this.imgBackground = new ImageIcon(urlBackgroundImg).getImage();
		
		// create chess game
		this.chessGame = new ChessGame();
		
		//wrap game pieces into their graphical representation
		for (Piece piece : this.chessGame.getPieces()) {
			createAndAddGuiPiece(piece);
		}
		

		// add listeners to enable drag and drop
		//
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this.guiPieces,
				this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);

		// label to display game state
		String labelText = this.getGameStateAsText();
		this.lblGameState = new JLabel(labelText);
		lblGameState.setBounds(0, 30, 80, 30);
		lblGameState.setForeground(Color.WHITE);
		this.add(lblGameState);

		// create application frame and set visible
		//
		JFrame f = new JFrame();
		f.setSize(80, 80);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setSize(imgBackground.getWidth(null), imgBackground.getHeight(null));
	}

	/**
	 * @return textual description of current game state
	 */
	private String getGameStateAsText() {
		String state = "unknown";
		switch (this.chessGame.getGameState()) {
			case ChessGame.GAME_STATE_BLACK: state = "black";break;
			case ChessGame.GAME_STATE_END: state = "end";break;
			case ChessGame.GAME_STATE_WHITE: state = "white";break;
		}
		return state;
	}

	/**
	 * create a game piece
	 * 
	 * @param color color constant
	 * @param type type constant
	 * @param x x position of upper left corner
	 * @param y y position of upper left corner
	 */
	private void createAndAddGuiPiece(Piece piece) {
		Image img = this.getImageForPiece(piece.getColor(), piece.getType());
		GuiPiece guiPiece = new GuiPiece(img, piece);
		this.guiPieces.add(guiPiece);
	}

	/**
	 * load image for given color and type. This method translates the color and
	 * type information into a filename and loads that particular file.
	 * 
	 * @param color color constant
	 * @param type type constant
	 * @return image
	 */
	private Image getImageForPiece(int color, int type) {

		String filename = "";

		filename += (color == Piece.COLOR_WHITE ? "w" : "b");
		switch (type) {
			case Piece.TYPE_BISHOP:
				filename += "b";
				break;
			case Piece.TYPE_KING:
				filename += "k";
				break;
			case Piece.TYPE_KNIGHT:
				filename += "n";
				break;
			case Piece.TYPE_PAWN:
				filename += "p";
				break;
			case Piece.TYPE_QUEEN:
				filename += "q";
				break;
			case Piece.TYPE_ROOK:
				filename += "r";
				break;
		}
		filename += ".png";

		URL urlPieceImg = getClass().getResource("/ch06/gui/img/" + filename);
		return new ImageIcon(urlPieceImg).getImage();
	}

	@Override
	protected void paintComponent(Graphics g) {

		// draw background
		g.drawImage(this.imgBackground, 0, 0, null);

		// draw pieces
		for (GuiPiece guiPiece : this.guiPieces) {
			if( !guiPiece.isCaptured()){
				g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
			}
		}
		
		// draw last move, if user is not dragging game piece
		if( !isUserDraggingPiece() && this.lastMove != null ){
			int highlightSourceX = convertColumnToX(this.lastMove.sourceColumn);
			int highlightSourceY = convertRowToY(this.lastMove.sourceRow);
			int highlightTargetX = convertColumnToX(this.lastMove.targetColumn);
			int highlightTargetY = convertRowToY(this.lastMove.targetRow);
			
			g.setColor(Color.YELLOW);
			g.drawRoundRect( highlightSourceX+4, highlightSourceY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
			g.drawRoundRect( highlightTargetX+4, highlightTargetY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
			//g.drawLine(highlightSourceX+SQUARE_WIDTH/2, highlightSourceY+SQUARE_HEIGHT/2
			//		, highlightTargetX+SQUARE_WIDTH/2, highlightTargetY+SQUARE_HEIGHT/2);
		}
		
		// draw valid target locations, if user is dragging a game piece
		if( isUserDraggingPiece() ){
			
			MoveValidator moveValidator = this.chessGame.getMoveValidator();
			
			// iterate the complete board to check if target locations are valid
			for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
				for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
					int sourceRow = this.dragPiece.getPiece().getRow();
					int sourceColumn = this.dragPiece.getPiece().getColumn();
					
					// check if target location is valid
					if( moveValidator.isMoveValid( new Move(sourceRow, sourceColumn, row, column)) ){
						
						int highlightX = convertColumnToX(column);
						int highlightY = convertRowToY(row);
						
						// draw a black drop shadow by drawing a black rectangle with an offset of 1 pixel
						g.setColor(Color.BLACK);
						g.drawRoundRect( highlightX+5, highlightY+5, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
						// draw the highlight
						g.setColor(Color.GREEN);
						g.drawRoundRect( highlightX+4, highlightY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
					}
				}
			}
		}
		
		
		// draw game state label
		this.lblGameState.setText(this.getGameStateAsText());
	}

	/** 
	 * @return true if user is currently dragging a game piece
	 */
	private boolean isUserDraggingPiece() {
		return this.dragPiece != null;
	}

	public static void main(String[] args) {
		new ChessGui();
	}

	/**
	 * @return current game state
	 */
	public int getGameState() {
		return this.chessGame.getGameState();
	}
	
	/**
	 * convert logical column into x coordinate
	 * @param column
	 * @return x coordinate for column
	 */
	public static int convertColumnToX(int column){
		return PIECES_START_X + SQUARE_WIDTH * column;
	}
	
	/**
	 * convert logical row into y coordinate
	 * @param row
	 * @return y coordinate for row
	 */
	public static int convertRowToY(int row){
		return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
	}
	
	/**
	 * convert x coordinate into logical column
	 * @param x
	 * @return logical column for x coordinate
	 */
	public static int convertXToColumn(int x){
		return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
	}
	
	/**
	 * convert y coordinate into logical row
	 * @param y
	 * @return logical row for y coordinate
	 */
	public static int convertYToRow(int y){
		return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y)/SQUARE_HEIGHT;
	}

	/**
	 * change location of given piece, if the location is valid.
	 * If the location is not valid, move the piece back to its original
	 * position.
	 * @param dragPiece
	 * @param x
	 * @param y
	 */
	public void setNewPieceLocation(GuiPiece dragPiece, int x, int y) {
		int targetRow = ChessGui.convertYToRow(y);
		int targetColumn = ChessGui.convertXToColumn(x);
		
		if( targetRow < Piece.ROW_1 || targetRow > Piece.ROW_8
				|| targetColumn < Piece.COLUMN_A || targetColumn > Piece.COLUMN_H){
			// reset piece position if move is not valid
			dragPiece.resetToUnderlyingPiecePosition();
		
		}else{
			//change model and update gui piece afterwards
			System.out.println("moving piece to "+targetRow+"/"+targetColumn);
			Move move = new Move( dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn()
					, targetRow, targetColumn);
			boolean wasMoveSuccessfull = this.chessGame.movePiece( move );
			
			// if the last move was successfully executed, we remember it for
			// highlighting it in the user interface
			if( wasMoveSuccessfull ){
				this.lastMove = move;
			}
			
			dragPiece.resetToUnderlyingPiecePosition();
		}
	}

	/**
	 * @param guiPiece - set the gui piece that the user is current dragging
	 */
	public void setDragPiece(GuiPiece guiPiece) {
		this.dragPiece = guiPiece;
	}
	
	/**
	 * @return the gui piece that the user is currently dragging
	 */
	public GuiPiece getDragPiece(){
		return this.dragPiece;
	}

}
