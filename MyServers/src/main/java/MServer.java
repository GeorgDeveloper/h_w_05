import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MServer {

    // message broker (kafka, redis, rabbitmq, ...)
    // client sent letter to broker

    // server sent to SMTP-server

    public static final int PORT = 8181;

    private static long clientIdCounter = 1L;
    private static Map<Long, SocketWrapper> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            new MServer().start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                final Socket client = server.accept();
                final long clientId = clientIdCounter++;

                SocketWrapper wrapper = new SocketWrapper(clientId, client);

                System.out.println("Подключился новый клиент[" + wrapper + "]");
                clients.put(clientId, wrapper);

                new Thread(() -> {
                    try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
                        output.println("Подключение успешно. Список всех клиентов: " + clients);

                        while (true) {
                            String clientInput = input.nextLine();
                            if (clientInput.substring(0, 1).equals("@")) {
                                if (clientInput.substring(0, 2).equals("@q")) {
                                    // todo разослать это сообщение всем остальным клиентам
                                    clients.remove(clientId);
                                    clients.values().forEach(it -> it.getOutput().println("Клиент[" + clientId + "] отключился"));
                                    break;
                                } else  if (clientInput.substring(0, 2).equals("@d")) {
                                    try {
                                        long destinationId = Long.parseLong(clientInput.substring(2, 3));
                                        clients.get(destinationId).getOutput().println("Вы заблокированы");
                                        clients.get(destinationId).close();
                                        clients.remove(destinationId);
                                        clients.values().forEach(it -> it.getOutput().println("Клиент[" + destinationId + "] блокирован"));
                                    } catch (NullPointerException | NumberFormatException e) {
                                        output.println("Нет такого пользователя или команды");
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    // формат сообщения: "цифра сообщение"
                                    try {
                                        long destinationId = Long.parseLong(clientInput.substring(1, 2));
                                        SocketWrapper destination = clients.get(destinationId);
                                        destination.getOutput().println(clientInput);
                                    } catch (NullPointerException | NumberFormatException e) {
                                        output.println("Нет такого пользователя или команды");
                                    }
                                }

                            } else {
                                clients.values().forEach(it -> it.getOutput().println(clientInput));
                            }
                        }


                    }

                }).start();
            }
        }
    }

}
