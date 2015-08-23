package ch09.net;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import ch09.Main;
import ch09.logic.IPlayerHandler;
import ch09.logic.Move;
import ch09.util.MoveUtils;

/**
 * This class represents a player that received and sends moves over a network.
 */
public class XmlRpcPlayerHandler implements IPlayerHandler{

       /** equivalent to channelId. */
	private String gameIdOnServer;
	/** password for the channel on the server. */
	private String gamePassword = null;
	/** last received message string from server */
	private String lastMoveStrReceivedFromNetwork = "###";
	/** last message sent to server */
	private String lastMoveStrSentToNetwork = "###";
	/** connection to server */
	private XmlRpcClient xmlRpcClient;
	/** URL of the XML-RPC services */
	private static final String XML_RPC_HOST_URL = "http://gmmsgs.appspot.com/xml";
	//private static final String XML_RPC_HOST_URL = "http://localhost:8080/xml";

	/**
	 * create new network game or join existing one.
	 * @param aGameIdOnServer - game id to join. If null then a new game is created
	 * @param aGamePassword - password for joining the online game or password for
	 *                        the new game to be created
	 */
	public XmlRpcPlayerHandler(String aGameIdOnServer, String aGamePassword) {
		// set up connection to server
		//
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(XML_RPC_HOST_URL));
			xmlRpcClient = new XmlRpcClient();
			xmlRpcClient.setTransportFactory(new XmlRpcCommonsTransportFactory(xmlRpcClient));
			xmlRpcClient.setConfig(config);
		} catch(MalformedURLException mue) {
			throw new IllegalStateException("Invalid XML-RPC-Server URL:" + XML_RPC_HOST_URL);
		}

		// do we need to create a new game or join existing?
		//
		if (aGameIdOnServer == null) {
			// create new game
			System.out.println("Creating new game");
			this.gameIdOnServer = createGame(aGamePassword);
			this.gamePassword = aGamePassword;
			Main.ask("New game id on server is:" + gameIdOnServer
					+ " (hit enter to continue)");
		} else {
			System.out.println("Joining game:" + aGameIdOnServer);
			// verify received parameters
			if (isGameValid(aGameIdOnServer, aGamePassword)) {
				this.gameIdOnServer = aGameIdOnServer;
				this.gamePassword = aGamePassword;
			} else {
				throw new IllegalStateException("GameId: " + aGameIdOnServer
						+ " and/or password: >" + aGamePassword+"< are invalid");
			}
		}
	}

	@Override
	public Move getMove() {
		Move receivedMove = null;
		String lastMoveFromServerStr = null;

		// loop until we receive a new move.
		//
		// We could also just return null when we receive no move
		// or receive the one we sent, but as the game logic
		// would ask again in 100 ms, I decided to block
		// the call and implement a waiting time of 3000 ms.
		// This greatly reduces network traffic, while still
		// not slowing down the game too much.
		while(receivedMove == null) {
			// ask server if there are new messages
			lastMoveFromServerStr = getLastMove(this.gameIdOnServer, this.gamePassword);
	
			// if no messages returned, return null
			if (lastMoveFromServerStr == null || lastMoveFromServerStr.trim().length() == 0) {
				System.out.println("No moves received");
			}
			// if we receive the move that we have just sent, we do not want
			// to return it to the game logic.
			//
			else if (lastMoveStrSentToNetwork != null
					&& lastMoveStrSentToNetwork.equals(lastMoveFromServerStr)) {
				System.out.println("Received move is the one we sent");
			}
			else {
				receivedMove = MoveUtils.convertStringToMove(lastMoveFromServerStr);
			}
			try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
		}

		// set last received move
		this.lastMoveStrReceivedFromNetwork = lastMoveFromServerStr;
		return receivedMove;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		System.out.println("moveSuccessfullyExecuted");
		String moveStr = MoveUtils.convertMoveToString(move);
		if (!moveStr.equals(lastMoveStrReceivedFromNetwork)) {
			// send our move to server
			sendMove(this.gameIdOnServer, this.gamePassword, moveStr);
			lastMoveStrSentToNetwork = moveStr;
		} else {
			// the executed move is the one we have received from
			// the network, so no need to send it again to the server
		}
	}

	/**
	 * get the last move that is available on the server
	 *
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - password for online game
	 * @return the last move as string
	 */
	private String getLastMove(String aGameIdOnServer, String aGamePassword) {
		System.out.println("get last move from server");
	    Object[] params = new Object[]{aGameIdOnServer};
	    String message = null;
	    try {
	    	message = (String) xmlRpcClient.execute("getLastMessage", params);
	    	// instead of returning an empty string we return null
	    	if (message != null && message.trim().length() == 0) {
	    		message = null;
	    	}
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return message;
	}

	/**
	 * Send the move message to the server.
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - password for online game
	 * @param message - the move as a string to be sent
	 * @return id that the server assigned to the new move message
	 */
	private String sendMove(String aGameIdOnServer, String aGamePassword, String message) {
		System.out.println("sending move:" + message);
	    Object[] params = new Object[]{aGameIdOnServer, aGamePassword, message};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("sendMessage", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	/**
	 * checks the validity of the provided game id and password
	 * @param aGameIdOnServer - server game id
	 * @param aGamePassword - game password
	 * @return true if parameters are valid
	 */
	private boolean isGameValid(String aGameIdOnServer, String aGamePassword) {
		System.out.println("sending validation request for game and password");
	    Object[] params = new Object[]{aGameIdOnServer, aGamePassword};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("isValid", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return Boolean.parseBoolean(result);
	}

	/**
	 * Create a new game with the specified password
	 * @param aGamePassword - password for the new game
	 * @return the game id that the game server assigned to the new game
	 */
	private String createGame(String aGamePassword) {
		System.out.println("sending createChannel request");
	    Object[] params = new Object[]{aGamePassword};
	    String result = null;
	    try {
			result = (String) xmlRpcClient.execute("createChannel", params);
		} catch (XmlRpcException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	/**
	 * getter
	 * @return gameId on server
	 */
	public String getGameIdOnServer(){
		return gameIdOnServer;
	}
	/*
	public static void main(String[] args) {
		String password = "test";
		XmlRpcPlayerHandler handler = new XmlRpcPlayerHandler(null, password);
		System.out.println("new id: " + handler.gameIdOnServer);
		System.out.println("is valid: " + handler.isGameValid(handler.gameIdOnServer, password));
		for (int i = 0; i < 15; i++) {
			System.out.println("message id: " + handler.sendMove(handler.gameIdOnServer, password, "msg" + i));
			System.out.println("messages: " + handler.getLastMove(handler.gameIdOnServer, password));
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	*/
}
