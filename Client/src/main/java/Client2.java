import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client2 {
    public static void main(String[] args) {
        try {
            new Client2().start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void start() throws IOException {
        final Socket client = new Socket("localhost", MServer.PORT);
        // чтение
        new Thread(() -> {
            try (Scanner input = new Scanner(client.getInputStream())) {
                while (true) {
                    System.out.println(input.nextLine());
                }
            } catch (Exception e) {
                System.out.println("Чат закрыт");
                throw new RuntimeException(e);
            }
        }).start();

        // запись
        new Thread(() -> {
            try (PrintWriter output = new PrintWriter(client.getOutputStream(), true)) {
                Scanner consoleScanner = new Scanner(System.in);
                while (true) {
                    String consoleInput = consoleScanner.nextLine();
                    output.println(consoleInput);
                    if (consoleInput.substring(0, 1).equals("@")) {
                        if (consoleInput.substring(0, 2).equals("@q")) {
                            client.close();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}
