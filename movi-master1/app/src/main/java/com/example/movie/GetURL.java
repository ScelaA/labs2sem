package com.example.movie;

public class GetURL {
    String component;
    String PLACEHOLDER_API_KEY=BuildConfig.API_KEY;

    public String fetch(String component) {
        this.component = component;
        String newurl="";
        switch (component)
        {
            case "Search":{
                newurl="https://api.themoviedb.org/3/search/movie?query=+insertquery+&api_key="+PLACEHOLDER_API_KEY;
                break;
            }
        }

        return newurl;
    }
}
