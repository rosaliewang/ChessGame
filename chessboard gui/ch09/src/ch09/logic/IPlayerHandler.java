package ch09.logic;

/**
 * This interface has to be implemented by all classes that want to
 * act as a player. Instances of this interface will be registered
 * in the logic class "ChessGame" using the method:
 * setPlayer(int pieceColor, IPlayerHandler playerHandler)
 *
 */
public interface IPlayerHandler {

	/**
	 * The logic class "ChessGame" calls this method on the currently
	 * active IPlayerHandler instance to receive the next move. If the
	 * new move is not available yet (e.g. the AI did not finish
	 * calculating the next move), the method
	 * can either block the call or return null. If null is returned, the
	 * game logic will call this method again after waiting a litte.
	 * 
	 * @return the next move or null, if the next move is not yet available
	 */
	public Move getMove();

	/**
	 * The logic class "ChessGame" calls this method on all registered
	 * IPlayerHandlers to inform them about the successful execution of
	 * the provided move.
	 * @param move - the last successfully executed move
	 */
	public void moveSuccessfullyExecuted(Move move);

}
