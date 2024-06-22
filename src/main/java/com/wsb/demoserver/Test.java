package com.wsb.demoserver;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Test {


    public static void main(String[] args) throws InterruptedException {
        Executor e = Executors.newFixedThreadPool(8);
        Server server = new Server();
        e.execute(() -> {
            try {
                server.startServer();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        for (int i = 0; i < 7; i++) {
            byte finalI = (byte) i;
            e.execute(() -> {
                try {
                    Client client = new Client(finalI);
                    client.startClient();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }



}
