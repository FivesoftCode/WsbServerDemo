package com.wsb.demoserver;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {

    static final Class<?>[] CLASSES = new Class<?>[]{
            com.wsb.demoserver.Kot.class,
            com.wsb.demoserver.Pies.class,
            com.wsb.demoserver.Papuga.class
    };

    private final byte id;

    public Client(byte id) {
        this.id = id;
    }

    public static void main(String[] args) {

    }

    public void startClient() throws IOException {
        try (Socket socket = new Socket("localhost", 2137);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            //Klient łączy się z serwerem, wysyła swoje id (liczba)
            IOUtil.writeMessage(out, new byte[]{id});
            // i otrzymuje status połączenia: OK lub
            //REFUSED (w przypadku gdy została przekroczona maksymalna liczba obsługiwanych klientów)
            byte[] response = IOUtil.readMessage(in);
            String status = new String(response);
            if (!"OK".equals(status)) {
                //W przypadku odpowiedzi REFUSED klient kończy działanie.
                System.out.println("Connection ended with status: " + status);
                return;
            }
            //W przypadku odpowiedzi OK:
            System.out.println("Connection established.");
            // klient powtarza powyższe punkty dwa/trzy razy dla różnych obiektów
            int repeats = (int) Math.round(Math.random() * 1) + 2;
            for (int i = 0; i < repeats; i++) {
                // klient prosi o przesłanie kolekcji obiektów konkretnej klasy, np. przesyłając komunikat
                //get_Koty, robimy w formacie GET <pełna_nazwa_klasy>
                int classIndex = (int) Math.round(Math.random() * (CLASSES.length - 1));
                String request = "GET " + CLASSES[classIndex].getName();
                IOUtil.writeMessage(out, request.getBytes());
                // klient odbiera kolekcję i wypisuje jej obiekty na konsoli wraz ze swoim id
                response = IOUtil.readMessage(in);
                Object result = deserializeObject(response);
                if(result instanceof List<?> list){
                    for (Object obj : list) {
                        System.out.println("Client " + id + " received: " + obj);
                    }
                } else {
                    // gdy nie potrafi zrzutować, wypisuje o tym informacje na konsoli,
                    System.out.println("Client " + id + " cannot deserialize results from server: " + result);
                }
            }
            //i kończy działanie.
            IOUtil.writeMessage(out, "END".getBytes());
        }
    }

    private static Object deserializeObject(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
