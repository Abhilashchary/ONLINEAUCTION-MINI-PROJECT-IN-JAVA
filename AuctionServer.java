import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionServer {
    private static final int PORT = 8080;
    private static final List<Product> products = new ArrayList<>();
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Auction Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Product {
        String name;
        String description;
        double startingPrice;
        double highestBid;
        String highestBidder;
        boolean isAuctionActive;

        Product(String name, String description, double startingPrice) {
            this.name = name;
            this.description = description;
            this.startingPrice = startingPrice;
            this.highestBid = startingPrice;
            this.highestBidder = "None";
            this.isAuctionActive = true;
        }

        void endAuction() {
            this.isAuctionActive = false;
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private String username;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                username = in.readLine();
                broadcast(username + " has joined the auction.");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    handleUserInput(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleUserInput(String input) {
            String[] tokens = input.split(",");
            String command = tokens[0];

            switch (command) {
                case "LIST":
                    String name = tokens[1];
                    String description = tokens[2];
                    double startingPrice = Double.parseDouble(tokens[3]);
                    Product product = new Product(name, description, startingPrice);
                    products.add(product);
                    broadcast("Auction started for: " + name + " - " + description + " with starting price $" + startingPrice);
                    startAuction(product);
                    break;
                case "BID":
                    String productName = tokens[1];
                    double bidAmount = Double.parseDouble(tokens[2]);
                    placeBid(productName, bidAmount);
                    break;
                case "VIEW":
                    viewProducts();
                    break;
                default:
                    out.println("Invalid command.");
            }
        }

        private void startAuction(Product product) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    product.endAuction();
                    broadcast("Auction for " + product.name + " has ended. Winner: " + product.highestBidder + " with a bid of $" + product.highestBid);
                }
            }, 60000); // Auction lasts for 1 minute
        }

        private void placeBid(String productName, double bidAmount) {
            for (Product product : products) {
                if (product.name.equalsIgnoreCase(productName) && product.isAuctionActive) {
                    if (bidAmount > product.highestBid) {
                        product.highestBid = bidAmount;
                        product.highestBidder = username;
                        broadcast(username + " placed a bid of $" + bidAmount + " on " + productName);
                        return;
                    } else {
                        out.println("Bid must be higher than the current highest bid of $" + product.highestBid);
                    }
                }
            }
            out.println("Product not found or auction is not active.");
        }

        private void viewProducts() {
            if (products.isEmpty()) {
                out.println("No products available for auction.");
                return;
            }
            StringBuilder sb = new StringBuilder("Available Products:\n");
            for (Product product : products) {
                sb.append(product.name)
                        .append(" - ")
                        .append(product.description)
                        .append(" (Starting Price: $")
                        .append(product.startingPrice)
                        .append(", Highest Bid: $")
                        .append(product.highestBid)
                        .append(", Winner: ")
                        .append(product.isAuctionActive ? "Bidding Open" : product.highestBidder)
                        .append(")\n");
            }
            out.println(sb.toString());
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
