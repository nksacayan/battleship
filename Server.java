import java.io.*;
import java.net.*;

public class Server implements Runnable {

	// F-Board shows player's fleet and damage
	// P-Board shows hits and misses on enemy fleet

	Socket playerOneSocket = null;
	ObjectOutputStream outToClient1 = null;
	ObjectInputStream inFromClient1 = null;
	BattleShipTable fBoard1 = null;
	BattleShipTable pBoard1 = null;

	Socket playerTwoSocket = null;
	ObjectOutputStream outToClient2 = null;
	ObjectInputStream inFromClient2 = null;
	BattleShipTable fBoard2 = null;
	BattleShipTable pBoard2 = null;

	public Server(Socket playerOneSocket) throws IOException {
		this.playerOneSocket = playerOneSocket;
		this.outToClient1 = new ObjectOutputStream(this.playerOneSocket.getOutputStream());
		this.inFromClient1 = new ObjectInputStream(this.playerOneSocket.getInputStream());
		System.out.println("Player one connected");

	}

	public static void main(String args[]) throws IOException, InterruptedException {

		ServerSocket welcomeSocket = new ServerSocket(5000);

		System.out.println("Server on, listening");
		Server server = new Server(welcomeSocket.accept());
		Thread gameThread = new Thread(server);

		System.out.println("Starting thread...");
		gameThread.start();
		System.out.println("Thread started");

		server.playerTwoSocket = welcomeSocket.accept();
		server.outToClient2 = new ObjectOutputStream(server.playerTwoSocket.getOutputStream());
		server.inFromClient2 = new ObjectInputStream(server.playerTwoSocket.getInputStream());
		System.out.println("Player two connected");

		// Lets game thread know that p2 is connected

		// Wait for game to end
		System.out.println("Waiting for game to end");
		gameThread.join();
		System.out.println("Server shutting down");
		welcomeSocket.close();
	}

	@Override
	public void run() {
		Message msg = new Message();
		msg.setMsgType(Message.MSG_REQUEST_INIT);

		try {
			outToClient1.writeObject(msg);
			outToClient1.flush();
			outToClient1.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			msg = (Message) inFromClient1.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Recieved board");
		System.out.println("Msg type: " + msg.getMsgType());
		System.out.println("Ftable: \n" + msg.Ftable.toString());
		System.out.println("Updating server boards for player 1");
		this.fBoard1 = msg.Ftable;
		this.pBoard1 = new BattleShipTable();

		System.out.println("Player 1 table:\n" + this.fBoard1);

		msg.setMsgType(Message.MSG_REQUEST_INIT);
		// Wait for player 2

		while (this.playerTwoSocket == null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			outToClient2.writeObject(msg);
			outToClient2.flush();
			outToClient2.reset();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Wait for response from player 2
		try {
			msg = (Message) inFromClient2.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("Recieved board");
		System.out.println("Msg type: " + msg.getMsgType());
		System.out.println("Ftable: \n" + msg.Ftable.toString());
		System.out.println("Updating server board for player 2");
		fBoard2 = msg.Ftable;
		pBoard2 = new BattleShipTable();

		System.out.println("Player 2 table:\n" + this.fBoard2);

		// Loop game
		String attack = null;

		while (true) {

			// Check game over
			if (fBoard1.checkLoss()) {
				msg.setMsgType(Message.MSG_REQUEST_GAME_OVER);
				msg.setMsg("Player 2 wins!");
				msg.Ftable = fBoard1;
				try {
					outToClient1.writeObject(msg);
					outToClient1.flush();
					outToClient1.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.Ftable = fBoard2;
				try {
					outToClient2.writeObject(msg);
					outToClient2.flush();
					outToClient2.reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;

			}
			if (fBoard2.checkLoss()) {
				msg.setMsgType(Message.MSG_REQUEST_GAME_OVER);
				msg.setMsg("Player 1 wins!");
				msg.Ftable = fBoard1;
				try {
					outToClient1.writeObject(msg);
					outToClient1.flush();
					outToClient1.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}

				msg.Ftable = fBoard2;
				try {
					outToClient2.writeObject(msg);
					outToClient2.flush();
					outToClient2.reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			}

			// Send request to p1
			msg.setMsgType(Message.MSG_REQUEST_PLAY);
			msg.Ftable = fBoard1;
			msg.Ptable = pBoard2;
			try {
				outToClient1.writeObject(msg);
				outToClient1.flush();
				outToClient1.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Receive msg from p1
			try {
				msg = (Message) inFromClient1.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

			// Apply attack to p2 board && update p2 Fboard
			// Attack is stored in Message.msg field
			// X hit O miss
			attack = msg.getMsg();

			if (fBoard2.bomb(attack)) {
				pBoard2.insertHit(attack, "X");
				msg.setMsg("Hit!");

			} else {
				pBoard2.insertHit(attack, "O");
				msg.setMsg("Miss!");
			}
			try {
				outToClient1.writeObject(msg);
				outToClient1.flush();
				outToClient1.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Check game over
			if (fBoard1.checkLoss()) {
				msg.setMsgType(Message.MSG_REQUEST_GAME_OVER);
				msg.setMsg("Player 2 wins!");
				msg.Ftable = fBoard1;
				try {
					outToClient1.writeObject(msg);
					outToClient1.flush();
					outToClient1.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.Ftable = fBoard2;
				try {
					outToClient2.writeObject(msg);
					outToClient2.flush();
					outToClient2.reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;

			}
			if (fBoard2.checkLoss()) {
				msg.setMsgType(Message.MSG_REQUEST_GAME_OVER);
				msg.setMsg("Player 1 wins!");
				msg.Ftable = fBoard1;
				try {
					outToClient1.writeObject(msg);
					outToClient1.flush();
					outToClient1.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}

				msg.Ftable = fBoard2;
				try {
					outToClient2.writeObject(msg);
					outToClient2.flush();
					outToClient2.reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			}

			// P2 turn
			msg.setMsgType(Message.MSG_REQUEST_PLAY);
			msg.Ftable = fBoard2;
			msg.Ptable = pBoard1;
			try {
				outToClient2.writeObject(msg);
				outToClient2.flush();
				outToClient2.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Receive msg from p2
			try {
				msg = (Message) inFromClient2.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

			// Apply attack to p1 board && update p1 Fboard
			// Attack is stored in Message.msg field
			// X hit O miss
			attack = msg.getMsg();
			if (fBoard1.bomb(attack)) {
				pBoard1.insertHit(attack, "X");
				msg.setMsg("Hit!");
			} else {
				pBoard1.insertHit(attack, "O");
				msg.setMsg("Miss!");
			}
			try {
				outToClient2.writeObject(msg);
				outToClient2.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
