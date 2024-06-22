package com.wsb.demoserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    //Maksymalną liczbę klientów należy
    //ograniczyć w postaci stałej, np. MAX_CLIENTS
    public static final int MAX_CLIENTS = 3;

    private final Map<String, Serializable> objects = new HashMap<>();

    //Serwer ma obsługiwać wielu klientów równocześnie
    private final ExecutorService serverExecutor = new ThreadPoolExecutor(MAX_CLIENTS,
            MAX_CLIENTS, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());


    //Sprawdź powyższą funkcjonalność:
    public static void main(String[] args) throws IOException {
        //Uruchom serwer.
        new Server().startServer();
    }

    /**
     * Start server and accept client connections.<br>
     * Server will accept connections until interrupted.<br>
     * Server will accept connections on port 2137.
     * @throws IOException if an I/O error occurs when creating the server socket.
     */
    public void startServer() throws IOException {
        //Serwer przy starcie tworzy po cztery obiekty każdej klasy, inicjalizując je innymi danymi i
        //umieszcza te obiekty w mapie, gdzie kluczem będzie nazwa klasy oraz numer porządkowy, np.
        //dla klasy Kot i dwóch instancji mamy: kot_1, kot_2
        for (int i = 0; i < 4; i++) {
            objects.put("Kot_" + i, new Kot("Kot_" + i, (i * 46) % 10));
            objects.put("Pies_" + i, new Pies("Pies_" + i, "owczarek"));
            objects.put("Papuga_" + i, new Papuga("Papuga_" + i, "kolorowy"));
        }
        //Start server
        try(ServerSocket ss = new ServerSocket(2137)){
            System.out.println("Server started at port 2137.");
            ss.setSoTimeout(1000);
            ss.setReuseAddress(true);
            while(!Thread.interrupted()){
                try {
                    onConnectionRequest(ss.accept());
                } catch (SocketTimeoutException e) {
                    //Ignore timeout, continue accepting connections
                }
            }
        }
    }

    private void onConnectionRequest(Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        try {
            serverExecutor.submit(() -> {
                try (socket) {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    //Move on and accept connection
                    onConnectionAccepted(in, out);
                } catch (IOException | InterruptedException e) {
                    //Error writing response or thread interrupted
                    throw new RuntimeException(e);
                }
            });
        } catch (RejectedExecutionException e) {
            //nadmiarowi klienci powinni otrzymać informację o odmowie obsługi
            writeMessage(out, "REFUSED");
        }
    }

    private void onConnectionAccepted(DataInputStream in,
                                      DataOutputStream out) throws IOException, InterruptedException {
        //Receive client ID
        byte clientID = IOUtil.readMessage(in)[0];
        //Respond with OK status
        writeMessage(out, "OK");
        //Print connection notification
        System.out.println("Client " + clientID + " connected.");
        //Wait for client request(s)
        while (!Thread.interrupted()) {
            byte[] request = IOUtil.readMessage(in);
            onResourceRequest(request, clientID, out);
        }
    }

    private void onResourceRequest(byte[] request, byte clientID,
                                   DataOutputStream out) throws IOException, InterruptedException {
        String resStr = new String(request, StandardCharsets.UTF_8);
        System.out.println("Server received request: " + resStr);
        //zadbaj o losowe opóźnienia przy obsłudze klientów
        Thread.sleep((long) (5_000 * Math.random()));
        if(resStr.startsWith("GET ")){
            //Klient prosi o przesłanie kolekcji obiektów
            // konkretnej klasy, np. przesyłając komunikat get_Koty,
            // robimy w formacie GET <pełna_nazwa_klasy>
            //GET resource requested
            resStr = resStr.substring(4);
            try {
                //Read class of requested resource
                Class<?> resClass = Class.forName(resStr);
                //Send serialized resources
                byte[] serializedResources = getSerializedResources(resClass, clientID);
                System.out.println("Server sends serialized resources to client " + clientID);
                IOUtil.writeMessage(out, serializedResources);
            } catch (ClassNotFoundException e) {
                //Unknown resource requested
                writeMessage(out, "CLASS NOT FOUND");
            } catch (IOException e) {
                //Error sending resources
                System.err.println("Error sending resources: " + e);
            }
        } else {
            //Send status changed notification
            writeMessage(out, "DISCONNECTED");
            //Disconnection requested
            Thread.currentThread().interrupt();
        }
    }

    private Object getResources(Class<?> clazz, byte clientId){
        //serwer pobiera obiekty z mapy, tworzy kolekcję (np. listę)
        List<Serializable> res = new ArrayList<>();
        for(Serializable obj : objects.values()){
            if(clazz.isInstance(obj)){
                res.add(obj);
                //oraz wypisuje na konsoli co (jakie obiekty) i komu (id) przesłał
                System.out.println("Server sends resource to client " + clientId + ": " + obj);
            }
        }
        //gdy obiektów nie ma w mapie, serwer odsyła dowolny obiekt
        return res.isEmpty() ? new Pies("Piotrek", "buldog") : res;
    }

    private byte[] getSerializedResources(Class<?> clazz, byte clientId) throws IOException {
        // odsyła go do klienta w postaci zserializowanej
        Object res = getResources(clazz, clientId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ObjectOutputStream oos = new ObjectOutputStream(baos)){
            oos.writeObject(res);
        }
        baos.flush();
        return baos.toByteArray();
    }

    private static void writeMessage(
            DataOutputStream stream, String message) throws IOException {
        IOUtil.writeMessage(stream, message.getBytes(StandardCharsets.UTF_8));
        System.out.println("Server responds with: " + message);
    }

}
