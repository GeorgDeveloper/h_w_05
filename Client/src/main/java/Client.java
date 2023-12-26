package ru.develop.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;




@AllArgsConstructor
@Getter
@Setter
public class Client {

    private boolean isAdmin = false;

    public static void main(String[] args) {
        try {
            new Client(true).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void start() throws IOException {
        final Socket client;
        client = new Socket("localhost", MServer.PORT);
        // чтение
        new Thread(() -> {
            try (Scanner input = new Scanner(client.getInputStream())) {
                while (true) {
                    System.out.println(input.nextLine());
                }
            } catch (Exception e) {
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
                System.out.println("Чат закрыт");
            }
        }).start();

    }
}
