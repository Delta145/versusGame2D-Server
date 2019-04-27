package Server;


import Entities.Human;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    private static volatile AtomicInteger id = new AtomicInteger(0);
    private String userName;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket client;
    private boolean isAuth = false;
    private boolean isTokenValid = true;
    private HashMap<String, Human> persons = new HashMap<>();
    private Server server;
    private ClientCommandHandler cmdHandler = null;

    private Human human;
    private String key;

    Client(Socket socket, Server server) {
        userName = id.getAndIncrement()+"";
        client = socket;
        this.server = server;
        try {
            InputStream inputClientStream = client.getInputStream();
            OutputStream outClientStream = client.getOutputStream();
            reader = new ObjectInputStream(inputClientStream);
            writer = new ObjectOutputStream(outClientStream);
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода!");
        }
    }

    void setIsAuth(boolean a) {
        isAuth = a;
    }

    void setIsTokenValid(boolean a) {
        isTokenValid = a;
    }

    boolean isTokenValid() {
        return isTokenValid;
    }

    boolean getIsAuth() {
        return isAuth;
    }

    void closeConnection() {
        try {
            sendMessage(cActions.SEND, "Сервер закрывает соединение...\n");
            writer.close();
            client.close();
        } catch (IOException e) {
            System.out.println("Соединение с клиентом оборвалось");
        }
    }

    void startService() {
        Thread thread = new Thread(this::servClient);
        thread.start();
    }

    void setKey(String key) {
        this.key = key;
    }

    String getKey() {
        return key;
    }

    Server getServer() {
        return server;
    }

    private void servClient() {
        try {

            cmdHandler = new ClientCommandHandler(this , server);
            Command cmd = readCMD();

                while (!cmd.getName().equals("exit")) {
                    System.out.println("Клиент " + userName + ": " + cmd.getName());
                    cmdHandler.executeCommand(cmd);
                    cmd = readCMD();
                }

            System.out.println("Клиент " + userName + " отключился.");

        } catch (Exception e) {
            System.err.println(String.format("Потеряно соединение с клиентом %s.", userName));
            System.err.println(e.getMessage());
        }
        finally {
            if (getKey() != null) server.remPlayer(getKey());
            server.remClient(this);
        }
    }

    public void sendMessage(cActions action, String str) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Ошибка при отправке сообщения клиенту");
        }
    }

    void sendMessage(cActions action, String str, Object human) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
            sendObject(human);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке объекта клиента");
        }
    }

    private void sendObject(Object obj) throws IOException {
        writer.writeObject(obj);
        writer.flush();
    }

    ClientCommandHandler getCmdHandler() {
        return cmdHandler;
    }

    String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    Command readCMD() {
        try {
            return (Command) reader.readObject();
        } catch (Exception e) {
            System.err.println("Ошибка при чтении команды");
            e.printStackTrace();
        }
        return null;
    }

    Human readHuman() {
        try {
            return (Human) reader.readObject();
        } catch (Exception e) {
            System.err.println("Ошибка при приёме персонажа");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public Human getHuman() {
        return human;
    }

    void showHumans() {
        persons.values().stream().map(Human::getName).forEach(c -> sendMessage(cActions.USRPRSN, c+"\n"));
        sendMessage(cActions.USRPRSN, "$EOF$");
    }

    void addHuman(Human human) {
        persons.put(human.getName(), human);
    }
    void removeHuman(String key) {
        persons.remove(key);
    }

    HashMap<String, Human> getPersons() {
        return persons;
    }

}
