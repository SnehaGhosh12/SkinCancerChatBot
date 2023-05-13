package com.example.detecto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatRVAdapter extends RecyclerView.Adapter{

    private ArrayList<ChatsModel> chatsModelArrayList;
     private String currentTime;
    private Context context;

    public ChatRVAdapter(ArrayList<ChatsModel> chatsModelArrayList, Context context) {
        this.chatsModelArrayList = chatsModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_msg_rv,parent,false);

                return new UserViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_msg_rv,parent,false);
                return new BotViewHolder(view);

            case 2:
                view=LayoutInflater.from(context).inflate(R.layout.image_layout,parent,false);
                return  new ImageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsModel chatsModel = chatsModelArrayList.get(position);
        switch (chatsModel.getSender()){
            case "user":
                ((UserViewHolder)holder).userMsg.setText(chatsModel.getMessage());
                currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                ((UserViewHolder)holder).userTime.setText(currentTime);
                break;
            case "bot":
                ((BotViewHolder)holder).botMsg.setText(chatsModel.getMessage());
                currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                ((BotViewHolder)holder).botTime.setText(currentTime);
                break;
            case "img":
                Picasso.get().load(chatsModel.getImageUrl()).into(((ImageViewHolder)holder).img);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatsModelArrayList.get(position).getSender()){
            case "user":
                return 0;
            case "bot":
                return 1;
            case "img":
                return 2;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return chatsModelArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView userMsg,userTime;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMsg= itemView.findViewById(R.id.userMsg);
            userTime=itemView.findViewById(R.id.userTime);
        }
    }

    public static class ImageViewHolder extends  RecyclerView.ViewHolder{
        ImageView img;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.ImagePreview);
        }
    }
    public static class BotViewHolder extends RecyclerView.ViewHolder{
        TextView botMsg,botTime;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMsg = itemView.findViewById(R.id.botMsg);
            botTime=itemView.findViewById(R.id.botTime);
        }
    }
}
