package ch06.logic;

import java.util.ArrayList;
import java.util.List;

public class ChessGame {

	private int gameState = GAME_STATE_WHITE;
	public static final int GAME_STATE_WHITE = 0;
	public static final int GAME_STATE_BLACK = 1;
	public static final int GAME_STATE_END = 2;

	// 0 = bottom, size = top
	private List<Piece> pieces = new ArrayList<Piece>();

	private MoveValidator moveValidator;

	/**
	 * initialize game
	 */
	public ChessGame() {

		this.moveValidator = new MoveValidator(this);

		// create and place pieces
		// rook, knight, bishop, queen, king, bishop, knight, and rook
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1,
				Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1,
				Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_1,
				Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1,
				Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1,
				Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);

		// pawns
		int currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2,
					currentColumn);
			currentColumn++;
		}

		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8,
				Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8,
				Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, Piece.ROW_8,
				Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8,
				Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8,
				Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);

		// pawns
		currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7,
					currentColumn);
			currentColumn++;
		}
	}

	/**
	 * create piece instance and add it to the internal list of pieces
	 * 
	 * @param color on of Pieces.COLOR_..
	 * @param type on of Pieces.TYPE_..
	 * @param row on of Pieces.ROW_..
	 * @param column on of Pieces.COLUMN_..
	 */
	private void createAndAddPiece(int color, int type, int row, int column) {
		Piece piece = new Piece(color, type, row, column);
		this.pieces.add(piece);
	}

	/**
	 * Move piece to the specified location. If the target location is occupied
	 * by an opponent piece, that piece is marked as 'captured'. If the move
	 * could not be executed successfully, 'false' is returned and the game
	 * state does not change.
	 * 
	 * @param move to execute
	 * @return true, if piece was moved successfully
	 */
	public boolean movePiece(Move move) {

		if (!this.moveValidator.isMoveValid(move) ) {
			System.out.println("move invalid");
			return false;
		}

		Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);

		// check if the move is capturing an opponent piece
		int opponentColor = (piece.getColor() == Piece.COLOR_BLACK ? Piece.COLOR_WHITE
				: Piece.COLOR_BLACK);
		if (isNonCapturedPieceAtLocation(opponentColor, move.targetRow, move.targetColumn)) {
			Piece opponentPiece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
			opponentPiece.isCaptured(true);
		}

		piece.setRow(move.targetRow);
		piece.setColumn(move.targetColumn);

		if (isGameEndConditionReached()) {
			this.gameState = GAME_STATE_END;
		} else {
			this.changeGameState();
		}
		return true;
	}

	/**
	 * check if the games end condition is met: One color has a captured king
	 * 
	 * @return true if the game end condition is met
	 */
	private boolean isGameEndConditionReached() {
		for (Piece piece : this.pieces) {
			if (piece.getType() == Piece.TYPE_KING && piece.isCaptured()) {
				return true;
			} else {
				// continue iterating
			}
		}

		return false;
	}

	/**
	 * returns the first piece at the specified location that is not marked as
	 * 'captured'.
	 * 
	 * @param row one of Piece.ROW_..
	 * @param column one of Piece.COLUMN_..
	 * @return the first not captured piece at the specified location
	 */
	public Piece getNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column
					&& piece.isCaptured() == false) {
				return piece;
			}
		}
		return null;
	}

	/**
	 * Checks whether there is a piece at the specified location that is not
	 * marked as 'captured' and has the specified color.
	 * 
	 * @param color one of Piece.COLOR_..
	 * @param row one of Piece.ROW_..
	 * @param column on of Piece.COLUMN_..
	 * @return true, if the location contains a not-captured piece of the
	 *         specified color
	 */
	private boolean isNonCapturedPieceAtLocation(int color, int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column
					&& piece.isCaptured() == false && piece.getColor() == color) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether there is a non-captured piece at the specified location
	 * 
	 * @param row one of Piece.ROW_..
	 * @param column on of Piece.COLUMN_..
	 * @return true, if the location contains a piece
	 */
	public boolean isNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column
					&& piece.isCaptured() == false) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return current game state (one of ChessGame.GAME_STATE_..)
	 */
	public int getGameState() {
		return this.gameState;
	}

	/**
	 * @return the internal list of pieces
	 */
	public List<Piece> getPieces() {
		return this.pieces;
	}

	/**
	 * switches the game state depending on the current board situation.
	 */
	public void changeGameState() {

		// check if game end condition has been reached
		//
		if (this.isGameEndConditionReached()) {

			if (this.gameState == ChessGame.GAME_STATE_BLACK) {
				System.out.println("Game over! Black won!");
			} else {
				System.out.println("Game over! White won!");
			}

			this.gameState = ChessGame.GAME_STATE_END;
			return;
		}

		switch (this.gameState) {
			case GAME_STATE_BLACK:
				this.gameState = GAME_STATE_WHITE;
				break;
			case GAME_STATE_WHITE:
				this.gameState = GAME_STATE_BLACK;
				break;
			case GAME_STATE_END:
				// don't change anymore
				break;
			default:
				throw new IllegalStateException("unknown game state:" + this.gameState);
		}
	}
	
	public MoveValidator getMoveValidator(){
		return this.moveValidator;
	}

}
