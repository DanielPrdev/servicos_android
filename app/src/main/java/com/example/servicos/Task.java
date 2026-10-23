package com.example.servicos;

public class Task {
    private long id;
    private String title;
    private String description;
    private String date;
    private String userEmail;

    public Task() {
    }

    public Task(long id, String title, String description, String date, String userEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.userEmail = userEmail;
    }

    public Task(String title, String description, String date, String userEmail) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.userEmail = userEmail;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
