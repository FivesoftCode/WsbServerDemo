package com.wsb.demoserver;

import java.io.Serializable;

class Kot implements Serializable {
    private String imie;
    private int wiek;

    public Kot(String imie, int wiek) {
        this.imie = imie;
        this.wiek = wiek;
    }

    @Override
    public String toString() {
        return "Kot(imie=" + imie + ", wiek=" + wiek + ")";
    }
}

class Pies implements Serializable {
    private String imie;
    private String rasa;

    public Pies(String imie, String rasa) {
        this.imie = imie;
        this.rasa = rasa;
    }

    @Override
    public String toString() {
        return "Pies(imie=" + imie + ", rasa=" + rasa + ")";
    }
}
class Papuga implements Serializable {
    private String imie;
    private String kolor;

    public Papuga(String imie, String kolor) {
        this.imie = imie;
        this.kolor = kolor;
    }

    @Override
    public String toString() {
        return "Papuga(imie=" + imie + ", kolor=" + kolor + ")";
    }
}
