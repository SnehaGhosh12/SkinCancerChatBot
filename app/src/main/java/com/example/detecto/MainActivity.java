package com.example.detecto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
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
import java.util.Objects;

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
    private long pressedTime;
    public static final int GALLERY_REQ_CODE = 100;
    public static final int CAMERA_REQ_CODE = 101;
    String[] classe = {"melanocytic nevi", "melanoma", "benign keratosis-like lesions", "basal cell carcinoma", "pyogenic granulomas and hemorrhage",
            "Actinic keratoses and intraepithelial carcinomae", "dermatofibroma"};

    String[] classes={"Actinic keratoses","basal cell carcinoma","benign keratosis-like lesions","dermatofibroma","melanocytic nevi","Vascular lesion","melanoma"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        chatsRV = findViewById(R.id.RVchats);
        editmsg = findViewById(R.id.EditMsg);
//        camera = findViewById(R.id.camera);
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


//        camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(iCamera, CAMERA_REQ_CODE);
//            }
//        });
    }

    private void model() {

        imgBitmap = Bitmap.createScaledBitmap(imgBitmap, 28, 28, false);
//        imgBitmap= imgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(imgBitmap);
        ByteBuffer byteBuffer = tensorImage.getBuffer();
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28, 28, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float max = Float.MIN_VALUE;
            int res = -1;
            for (int i = 0; i < 7; i++) {
                Log.d("i",""+outputFeature0.getFloatArray()[i]);
                if (outputFeature0.getFloatArray()[i] > max) {
                    max = outputFeature0.getFloatArray()[i];
                    res = i;
                }
            }
            Log.d("i",""+res);


            // Releases model resources if no longer used.
            model.close();
            Log.d("res:",classes[res]);
            if(!Objects.equals(classes[res],"benign keratosis-like lesions")) {
                chatsModelArrayList.add(new ChatsModel("There is a possibility of " + String.valueOf(max * 100) + "% that this maybe " + classes[res], BOT_KEY));

            }else{
                chatsModelArrayList.add(new ChatsModel("It seems that this is not any kind of Skin Cancer with accuracy "+ String.valueOf(max * 100)+"%",BOT_KEY));
            }
            chatRVAdapter.notifyDataSetChanged();
            chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
//
//            chatsModelArrayList.add(new ChatsModel("Accuracy: "+String.valueOf(max*100)+"%", BOT_KEY));
//            chatRVAdapter.notifyDataSetChanged();
//
//            chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
        } catch (IOException e) {
            // TODO Handle the exception
        }

//        try {
//            Model model = Model.newInstance(getApplicationContext());
//
//            // Creates inputs for reference.
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
//            inputFeature0.loadBuffer(byteBuffer);
//
//            // Runs model inference and gets result.
//            Model.Outputs outputs = model.process(inputFeature0);
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//            int max = Integer.MIN_VALUE;
//            int res = -1;
//            for (int i = 0; i < 7; i++) {
//                if (outputFeature0.getFloatArray()[i] > max) {
//                    max = (int) outputFeature0.getFloatArray()[i];
//                    res = i;
//                }
//            }
//            chatsModelArrayList.add(new ChatsModel(classes[res], BOT_KEY));
//            chatRVAdapter.notifyDataSetChanged();
//            chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
//            // Releases model resources if no longer used.
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//        }

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
//                    chatsModelArrayList.add(new ChatsModel(getFileName(uri), BOT_KEY));
//                    chatRVAdapter.notifyDataSetChanged();
//                    chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
            if (requestCode == CAMERA_REQ_CODE) {
//                for camera
                if ((data != null ? data.getExtras() : null) != null) {
                    imgBitmap = (Bitmap) data.getExtras().get("data");
                    int dimension= Math.min(imgBitmap.getHeight(),imgBitmap.getWidth());
                    imgBitmap= ThumbnailUtils.extractThumbnail(imgBitmap,dimension,dimension);
                    chatsModelArrayList.add(new ChatsModel(imgBitmap, cam_img));
                    chatRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
//                    model();
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
    public String getFileName(Uri uri) {
        try {
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            return returnCursor.getString(nameIndex);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "unknown";
    }
    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
}