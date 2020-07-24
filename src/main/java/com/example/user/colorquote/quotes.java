package com.example.user.colorquote;

/**
 * Created by USER on 10/29/2017.
 */

public class quotes {

    String quote,saidby,postedby,likecount;


    public quotes(){



    }


    public quotes(String quote, String saidby, String postedby,String likecount) {
        this.quote = quote;
        this.saidby = saidby;
        this.postedby = postedby;
        this.likecount = likecount;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getSaidby() {
        return saidby;
    }

    public void setSaidby(String saidby) {
        this.saidby = saidby;
    }

    public String getPostedby() {
        return postedby;
    }

    public void setPostedby(String postedby) {
        this.postedby = postedby;
    }

    public String getLikecount() {
        return likecount;
    }

    public void setLikecount(String likecount) {
        this.likecount = likecount;
    }

}