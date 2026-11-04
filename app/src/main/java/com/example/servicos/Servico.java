package com.example.servicos;

import java.text.NumberFormat;
import java.util.Locale;

public class Servico {
    private long id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String clientName;
    private String address;
    private double price;

    public Servico() {
    }

    public Servico(long id, String title, String description, String date, String time, String clientName, String address, double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.clientName = clientName;
        this.address = address;
        this.price = price;
    }

    public Servico(String title, String description, String date, String time, String clientName, String address, double price) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.clientName = clientName;
        this.address = address;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getFormattedPrice() {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return format.format(price);
    }
}
