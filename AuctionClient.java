import java.io.*;
import java.net.*;

public class AuctionClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Welcome to the Auction System!");
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            out.println(username); // Send username to server

            String input;
            while (true) {
                showMenu();
                input = userInput.readLine();
                handleUserInput(input, out, userInput);
                while (in.ready()) {
                    System.out.println(in.readLine()); // Print messages from server
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showMenu() {
        System.out.println("--- Auction Menu ---");
        System.out.println("1. List a product (Seller)");
        System.out.println("2. Place a bid (Buyer)");
        System.out.println("3. View available products");
        System.out.println("4. Logout");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void handleUserInput(String input, PrintWriter out, BufferedReader userInput) throws IOException {
        switch (input) {
            case "1":
                listProduct(out, userInput);
                break;
            case "2":
                placeBid(out, userInput);
                break;
            case "3":
                out.println("VIEW");
                break;
            case "4":
                out.println("LOGOUT");
                System.exit(0);
                break;
            case "5":
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void listProduct(PrintWriter out, BufferedReader userInput) throws IOException {
        System.out.print("Enter product name: ");
        String name = userInput.readLine();
        System.out.print("Enter product description: ");
        String description = userInput.readLine();
        System.out.print("Enter starting price: ");
        double startingPrice = Double.parseDouble(userInput.readLine());

        out.println("LIST," + name + "," + description + "," + startingPrice);
    }

    private static void placeBid(PrintWriter out, BufferedReader userInput) throws IOException {
        System.out.print("Enter product name to bid on: ");
        String productName = userInput.readLine();
        System.out.print("Enter your bid amount: ");
        double bidAmount = Double.parseDouble(userInput.readLine());

        out.println("BID," + productName + "," + bidAmount);
    }
}
