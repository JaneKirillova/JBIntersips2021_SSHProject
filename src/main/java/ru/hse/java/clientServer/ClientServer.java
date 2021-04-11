package ru.hse.java.clientServer;

import java.net.*;
import java.io.*;

public class ClientServer {
    public static class Client {
        private static Socket clientSocket = null;
        private static BufferedReader in = null;
        private static BufferedWriter out = null;

        public Client(String serverName, int serverPort) {
            try {
                try {
                    clientSocket = new Socket(serverName, serverPort);
                    while(true) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        System.out.println("Введите число N:");
                        String input = reader.readLine();
                        if (input.equals(""))
                            break;
                        out.write(input + "\n");
                        out.flush();
                        String serverAnswer = in.readLine();
                        System.out.println(serverAnswer);
                    }
                } finally {
                    System.out.println("Клиент был завершен");
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }


    public static class Server implements Runnable {
        private ServerSocket server = null;
        private Thread thread = null;

        public Server(int port) {
            try {
                server = new ServerSocket(port);
                start();
            } catch(IOException ioe) {
                System.out.println(ioe);
            }
        }
        public void run() {
            System.out.println("Ожидание клиента ...");
            while (thread != null) {
                try {
                    addThread(server.accept());
                } catch(IOException ie) {
                    System.out.println("Acceptance Error: " + ie);
                }
            }
        }
        public void addThread(Socket socket) {
            System.out.println("Клиент принят: " + socket);
            ClientHandler client = new ClientHandler(socket);
            client.start();
        }
        public void start() {
            if (thread == null) {
                thread = new Thread(this);
                thread.start();
            }
        }
        public void stop() {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
    }


    private static class ClientHandler extends Thread {
        private static int countFibNumber(int n) {
            if (n == 0 || n == 1) {
                return n;
            }
            int ans = 1, prevNum = 0;
            for (int i = 2; i <= n; i++) {
                ans = prevNum + ans;
                prevNum = ans - prevNum;
            }
            return ans;
        }

        private final Socket socket;
        private static BufferedReader in = null;
        private static BufferedWriter out = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                try {
                    boolean done = false;
                    while (!done) {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        try {
                            String word = in.readLine();
                            if (word == null)
                                break;
                            int ans = countFibNumber(Integer.parseInt(word));
                            System.out.println(ans);
                            out.write(ans + "\n");
                            out.flush();
                        } catch (IOException e) {
                            done = true;
                        }
                    }
                } finally {
                    socket.close();
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            } catch(IOException e) {
                System.err.println(e);
            }
        }
    }

    static void runApplication(String type, String serverName, int serverPort) {
        if (type.equals("Client")) {
            new Client(serverName, serverPort);
        } else {
            new Server(serverPort);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3 || !args[0].equals("Server") && !args[0].equals("Client")) {
            System.out.println("Неправильный формат ввода");
            return;
        }
        runApplication(args[0], args[1], Integer.parseInt(args[2]));
    }
}
