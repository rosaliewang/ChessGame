package logic;

/**
 * Created by Yuchen Wang on 8/21/15.
 */
public interface PlayerHandler {
    public Move getMove();

    public void moveSuccessfullyExecuted(Move move);
}
