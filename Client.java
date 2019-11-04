import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

	public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException {

		Scanner kb = new Scanner(System.in);

		Socket clientSocket = new Socket("localhost", 5000); // Server port is 5000
		System.out.println("Conected to server");

		ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
		ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

		System.out.println("Object streams instantiated");

		Message msg = (Message) inFromServer.readObject();
		System.out.println("Msg type: " + msg.getMsgType());

		String x1, x2;

		System.out.println("Static is: " + Message.MSG_REQUEST_INIT);
		if (msg.getMsgType() == Message.MSG_REQUEST_INIT) {
			// Player inputs grid blocks for fleet
			System.out.println("\nBeginning piece input...\n");

			BattleShipTable fBoard = new BattleShipTable();
			BattleShipTable pBoard = new BattleShipTable();
			System.out.println("Initial Table Created");

			// 2 Aircraft Carriers
			System.out.println("Placing 2 carriers (Takes 5 spaces each)");

			int i = 0;

			System.out.println(fBoard.toString());
			System.out.println("Please enter two adjacent coordinates");
			while (i < 2) {
				do {
					System.out.println("Enter first coordinate [A-J][0-9]");
					x1 = kb.nextLine().toUpperCase();
					System.out.println("You entered " + x1);
				} while (x1.length() != 2);
				do {
					System.out.println("Enter second coordinate [A-J][0-9]");
					x2 = kb.nextLine().toUpperCase();
					System.out.println("You entered " + x2);
				} while (x2.length() != 2);
				if (fBoard.insertAirCarrier(x1, x2)) {
					i++;
					System.out.println("Piece placed");
				} else
					System.out.println("Invalid placement, try again");
				System.out.println(fBoard.toString());

			}
			// 2 Destroyers
			System.out.println("Placing 2 destroyers (Takes 3 spaces each)");
			i = 0;

			System.out.println(fBoard.toString());
			while (i < 2) {
				do {
					System.out.println("Enter first coordinate [A-J][0-9]");
					x1 = kb.nextLine().toUpperCase();
					System.out.println("You entered " + x1);
				} while (x1.length() != 2);
				do {
					System.out.println("Enter second coordinate [A-J][0-9]");
					x2 = kb.nextLine().toUpperCase();
					System.out.println("You entered " + x2);
				} while (x2.length() != 2);
				if (fBoard.insertDestroyer(x1, x2)) {
					i++;
					System.out.println("Piece placed");
				} else
					System.out.println("Invalid placement, try again");
				System.out.println(fBoard.toString());
			}
			// 2 Subs
			System.out.println("Placing 2 submarines (Takes only 1 space each)");
			i = 0;

			while (i < 2) {
				do {
					System.out.println("Enter coordinate [A-J][0-9]");
					x1 = kb.nextLine().toUpperCase();
					System.out.println("You entered " + x1);
				} while (x1.length() != 2);
				if (fBoard.insertSubmarine(x1)) {
					i++;
					System.out.println("Piece placed");
				} else
					System.out.println("Invalid placement, try again");
				System.out.println(fBoard.toString());
			}
			System.out.println(fBoard);

			System.out.println("Sending table to server");

			// Send table to server
			msg = new Message();
			msg.Ftable = fBoard;
			msg.setMsgType(Message.MSG_RESPONSE_INIT);

			outToServer.writeObject(msg);

			// Loop gameplay
			boolean game = true;
			while (fBoard != null && game) {
				// Recieve messasge
				msg = (Message) inFromServer.readObject();

				if (msg.getMsgType() == Message.MSG_REQUEST_PLAY) {

					// Update boards
					fBoard = msg.Ftable;
					pBoard = msg.Ptable;

					// Display boards
					System.out.println("Your turn:");
					System.out.println("Your board:\n" + fBoard.toString());
					System.out.println("Enemy board:\n" + pBoard);

					// Player selects grid to bomb
					do {
						System.out.println("Select a grid position to bomb [A-J][0-9]");
						x1 = kb.nextLine().toUpperCase();
					} while (x1.length() != 2);

					// Send response to server
					// Put targeted grid in Message.msg field
					msg.setMsg(x1);
					msg.setMsgType(Message.MSG_RESPONSE_PLAY);
					outToServer.writeObject(msg);
					System.out.println("Attack launched. Waiting for server to update and opponent to take turn\n");

					// Wait for his or miss update
					msg = (Message) inFromServer.readObject();
					System.out.println(msg.getMsg());
				} else if (msg.getMsgType() == Message.MSG_REQUEST_GAME_OVER) {
					System.out.println(msg.getMsg());
					System.out.println("Your table:");
					System.out.println(msg.Ftable);
					break;
				}

			}

		}
		kb.close();
		clientSocket.close();
		System.out.println("Game over");

	}
}