package ch09;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch09.ai.SimpleAiPlayerHandler;
import ch09.console.ConsolePlayerHandler;
import ch09.gui.SwingPlayerHandler;
import ch09.logic.ChessGame;
import ch09.logic.IPlayerHandler;
import ch09.logic.Piece;
import ch09.net.XmlRpcPlayerHandler;


/**
 * Main class of the application. This class starts the game logic and sets
 * the IPlayerHandler instances for the black and white player. If you want
 * to switch from playing "Swing user interface" against "Network player"
 * to "Network player" against "artificial intelligence player", this is the
 * class you need to change.
 */
public class Main {

	private static final int PLAYER_OPTION_SWING = 0;
	private static final int PLAYER_OPTION_CONSOLE = 1;
	private static final int PLAYER_OPTION_AI = 2;
	private static final int PLAYER_OPTION_NETWORK = 3;

	public static void main(String[] args) {

		// ask for player handlers to be used for this game
		//
		String[] iPlayerHandlerOptions = new String[] {
			"Swing GUI", "Console GUI", "AI Player", "Network Player"
		};
		int whitePlayerOption = ask("What should be the white (starting) player?"
				, iPlayerHandlerOptions);
		int blackPlayerOption = ask("What should be the black player?"
				, iPlayerHandlerOptions);

		// in case of network players, ask for details
		//
		String gameIdOnServer = null;
		String gamePassword = null;
		if(whitePlayerOption == PLAYER_OPTION_NETWORK) {
			gameIdOnServer = ask("Game ID on server:");
			gamePassword = ask("Password for game:");
		}
		
		if(blackPlayerOption == PLAYER_OPTION_NETWORK) {
			gamePassword = ask("Password for new game:");
		}

		// create the game logic
		ChessGame chessGame = new ChessGame();

		// assign white and black player
		IPlayerHandler playerWhite = getPlayerHandler(whitePlayerOption, chessGame, gameIdOnServer, gamePassword);
		IPlayerHandler playerBlack = null;
		// if white and black are the same (e.g. both Swing or both Console)
		// then do not create a new player instance, but reuse the already
		// created ones. Otherwise we would create two Swing frames or
		// mess up the console interface, etc.
		if (whitePlayerOption == blackPlayerOption) {
			playerBlack = playerWhite;
		} else {
			playerBlack = getPlayerHandler(blackPlayerOption, chessGame, gameIdOnServer, gamePassword);			
		}

		// then we attach the clients/players to the game
		chessGame.setPlayer(Piece.COLOR_WHITE, playerWhite);
		chessGame.setPlayer(Piece.COLOR_BLACK, playerBlack);

		// in the end we start the game
		new Thread(chessGame).start();
	}
	
	/**
	 * Get the IPlayerHandler implementation for the specified playerHandlerOption
	 * @param playerHandlerOption - one of PLAYER_OPTION_..
	 * @param chessGame - a ChessGame instance
	 * @param gameIdOnServer - only applicable for PLAYER_OPTION_NETWORK.
	 *                         parameter should be empty for creating a new game
	 *                         and filled for joining an existing network game
	 * @param gamePassword - only applicable for PLAYER_OPTION_NETWORK.
	 *                       password for the network game
	 * @return IPlayerHandler implementation
	 */
	private static IPlayerHandler getPlayerHandler(int playerHandlerOption,
			ChessGame chessGame, String gameIdOnServer, String gamePassword) {
		switch (playerHandlerOption) {
			case PLAYER_OPTION_AI: return new SimpleAiPlayerHandler(chessGame);
			case PLAYER_OPTION_CONSOLE: return new ConsolePlayerHandler(chessGame);
			case PLAYER_OPTION_NETWORK: return new XmlRpcPlayerHandler(gameIdOnServer,gamePassword);
			case PLAYER_OPTION_SWING: return new SwingPlayerHandler(chessGame);
			default: throw new IllegalArgumentException("Invalid player option:" + playerHandlerOption);
		}
	}

	/**
	 * Ask user to choose one of the displayed options.
	 * @param question - question to ask the user
	 * @param options - options for the user to choose from
	 * @return index of the option (starting with 0)
	 */
	private static int ask(String question, String[] options) {
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.println();
			try {
				System.out.println(question);
				for (int i = 0; i < options.length; i++) {
					System.out.println(i + ": " +options[i]);
				}
				System.out.print("Your choice: ");
				String returnValue =  inputReader.readLine();
				
				// check validity
				int choice = Integer.parseInt(returnValue);
				if( choice >= 0 && choice < options.length) {
					return choice;
				} else {
					System.out.println("Your selection is out of range. Please try again.");
				}
			} catch (Exception e) {
				System.out.println("Your choice has been invalid, please try again:" + e);
			}
		}
	}

	/**
	 * ask the user a question and return the reply.
	 * In case of an empty string reply, null is returned
	 * @param question - question to ask to the user
	 * @return user reply
	 */
	public static String ask(String question) {
		System.out.println();
		//read user input
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print(question);
			String returnValue =  inputReader.readLine();
			
			// return null instead of empty string
			if (returnValue != null && returnValue.trim().length() == 0) {
				returnValue = null;
			}
			return returnValue;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}