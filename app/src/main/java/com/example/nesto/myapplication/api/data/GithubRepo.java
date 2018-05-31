package com.example.nesto.myapplication.api.data;

public class GithubRepo {
    String name;
    String owner;
    String url;

    @Override
    public String toString() {
        return(name + " " +  url);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
