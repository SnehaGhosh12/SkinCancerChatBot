package com.example.detecto;

import android.graphics.Bitmap;
import android.net.Uri;

public class ChatsModel {
    private String message;
    private String sender;
    private Uri imgUrl;
    private  Bitmap imgBitmap;

    public ChatsModel(Uri uri,String sender) {
        this.imgUrl=uri;
        this.sender=sender;
    }
    public ChatsModel(Bitmap imgBitmap, String sender) {
        this.imgBitmap=imgBitmap;
        this.sender=sender;
    }
    public Bitmap getImageBitmap(){
        return  imgBitmap;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ChatsModel(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public void setImageUrl(Uri imgUrl) {

        this.imgUrl=imgUrl;
    }
    public Uri getImageUrl(){
        return  imgUrl;
    }

}
