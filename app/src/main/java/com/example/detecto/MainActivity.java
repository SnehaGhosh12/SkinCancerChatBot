package com.example.detecto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.example.detecto.ml.Model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRV;
    FloatingActionButton camera, attach;
    public EditText editmsg;
    private FloatingActionButton send;
    private ImageView imgPreview;
    private final String BOT_KEY = "bot";
    public String uri;
    private final String USER_KEY = "user";
    private final String img_KEY = "img";
    private final String cam_img = "camera";

    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRVAdapter chatRVAdapter;
    private Bitmap imgBitmap;
    public static final int GALLERY_REQ_CODE = 100;
    public static final int CAMERA_REQ_CODE = 101;
    String[] classes = {"akiec", "bcc", "bkl", "df", "mel", "nv", "vas"};


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        chatsRV = findViewById(R.id.RVchats);
        editmsg = findViewById(R.id.EditMsg);
        camera = findViewById(R.id.camera);
        attach = findViewById(R.id.attach);
        send = findViewById(R.id.Send);
        imgPreview = findViewById(R.id.skin_image);
        chatsModelArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatsModelArrayList, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatRVAdapter);
        getResponse("Hii");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editmsg.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show();
                    return;
                }
                chatsModelArrayList.add(new ChatsModel(editmsg.getText().toString(), USER_KEY));
                chatRVAdapter.notifyDataSetChanged();
                chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
                getResponse(editmsg.getText().toString());
                editmsg.setText("");
            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallary = new Intent(Intent.ACTION_PICK);
                iGallary.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallary, GALLERY_REQ_CODE);


            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iCamera, CAMERA_REQ_CODE);
            }
        });
    }

    private void model() {
        imgBitmap = Bitmap.createScaledBitmap(imgBitmap, 32, 32, true);
//        imgBitmap= imgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(imgBitmap);
            ByteBuffer byteBuffer = tensorImage.getBuffer();


            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//            String s=String.valueOf(outputFeature0.getFloatArray()[0])+"\n "+String.valueOf(outputFeature0.getFloatArray()[1])
//                    +"\n "+String.valueOf(outputFeature0.getFloatArray()[2])+"\n "+String.valueOf(outputFeature0.getFloatArray()[3])
//                    +"\n "+String.valueOf(outputFeature0.getFloatArray()[4])+"\n "+String.valueOf(outputFeature0.getFloatArray()[5])
//                    +"\n "+String.valueOf(outputFeature0.getFloatArray()[6]);
            int max = Integer.MIN_VALUE;
            int res = -1;
            for (int i = 0; i < 7; i++) {
                if (outputFeature0.getFloatArray()[i] > max) {
                    max = (int) outputFeature0.getFloatArray()[i];
                    res = i;
                }
            }
            chatsModelArrayList.add(new ChatsModel(classes[res], BOT_KEY));
            chatRVAdapter.notifyDataSetChanged();
            chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
//        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
//                for gallery
                Uri uri = data.getData();
                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
// for gallery
                    chatsModelArrayList.add(new ChatsModel(imgBitmap, cam_img));
                    chatRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
                    model();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
            if (requestCode == CAMERA_REQ_CODE) {
//                for camera
                if ((data != null ? data.getExtras() : null) != null) {
                    imgBitmap = (Bitmap) data.getExtras().get("data");
                    chatsModelArrayList.add(new ChatsModel(imgBitmap, cam_img));
                    chatRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
                }

            }
//            model();
        }
    }


    public void getResponse(String message) {
        String url = "http://api.brainshop.ai/get?bid=175248&key=MwQGePFXLShwVDGN&uid=[uid]&msg=" + message;
        String BASE_URL = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getCnt(), BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);

                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                chatsModelArrayList.add(new ChatsModel("Please revert your Question" + t.getMessage(), BOT_KEY));
//                chatsRV.scrollToPosition(chatsModelArrayList.size()-1);
                chatRVAdapter.notifyDataSetChanged();
                chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);

            }
        });
    }
}