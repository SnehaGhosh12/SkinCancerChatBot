package com.example.detecto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRV;
    FloatingActionButton camera,attach;
    public EditText editmsg;
    private FloatingActionButton send;
    private ImageView imgPreview;
    private final String BOT_KEY = "bot";
     public String uri;
    private final String USER_KEY = "user";
    private final String img_KEY = "img";

    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRVAdapter chatRVAdapter;
    public static final int PICK_IMAGE = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        chatsRV = findViewById(R.id.RVchats);
        editmsg = findViewById(R.id.EditMsg);
        camera=findViewById(R.id.camera);
        attach=findViewById(R.id.attach);
        send = findViewById(R.id.Send);
        imgPreview=findViewById(R.id.ImagePreview);
        chatsModelArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatsModelArrayList,this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatRVAdapter);
        getResponse("Hii");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editmsg.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show();
                    return;
                }
                chatsModelArrayList.add(new ChatsModel(editmsg.getText().toString(),USER_KEY));
                chatRVAdapter.notifyDataSetChanged();
                chatsRV.scrollToPosition(chatsModelArrayList.size()-1);
                getResponse(editmsg.getText().toString());
                editmsg.setText("");
            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MediaStore.ACTION_PICK_IMAGES);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 1);

            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        try {
//            imgPreview.setImageBitmap(BitmapFactory.decodeStream(getContentResolver()
//                    .openInputStream(data.getData()), null, new BitmapFactory.Options()));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//           if(!data.getData().toString().isEmpty()){
//               Toast.makeText(this, "Hiii", Toast.LENGTH_SHORT).show();
//               uri=data.getData().toString();
//               Picasso.get().load(uri).resize(1000, 800).into(imgPreview);
//               chatsModelArrayList.add(new ChatsModel(uri,img_KEY));
//
//               chatRVAdapter.notifyDataSetChanged();
//               chatsRV.scrollToPosition(chatsModelArrayList.size()-1);
//               getResponse(editmsg.getText().toString());
//               editmsg.setText("");
//           }

    }

    public void getResponse(String message){
        String url= "http://api.brainshop.ai/get?bid=175248&key=MwQGePFXLShwVDGN&uid=[uid]&msg="+message;
        String BASE_URL= "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call= retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if(response.isSuccessful()){
                    MsgModel model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getCnt(),BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(chatsModelArrayList.size()-1);

                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                chatsModelArrayList.add(new ChatsModel("Please revert your Question" + t.getMessage(),BOT_KEY));
//                chatsRV.scrollToPosition(chatsModelArrayList.size()-1);
                chatRVAdapter.notifyDataSetChanged();
                chatsRV.scrollToPosition(chatsModelArrayList.size()-1);

            }
        });
    }
}