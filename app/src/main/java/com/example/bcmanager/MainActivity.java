package com.example.bcmanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, AsyncResponse {
    private String TAG_ = "MainAcitivity";
    /**
     * url
     */
    public static String IMAGE_URL = "http://pmy0420.cafe24.com/dbimages/";
    public static String INSERT_IMAGE_URL = "http://pmy0420.cafe24.com/insert_image.php";
    public static String GET_IMAGE_URL = "http://pmy0420.cafe24.com/get_image.php";
    public static String SIGNUP_URL = "http://pmy0420.cafe24.com/signup.php";
    public static String GET_CARDS_INFO = "http://pmy0420.cafe24.com/get_cards.php";
    public static String GET_CARD_INFO = "http://pmy0420.cafe24.com/get_card.php";
    public static String GET_UNREGISTERD_CARD_INFO = "http://pmy0420.cafe24.com/get_unregisterd_card.php";
    public static String GET_UNREGISTERD_CARD_COUNT = "http://pmy0420.cafe24.com/get_count_cards0.php";
    public static String CARD_INPUT = "http://pmy0420.cafe24.com/card_input.php";
    public static String GET_USER_NUMBER = "http://pmy0420.cafe24.com/get_user_number.php";
    public static String DELETE_ITEM = "http://pmy0420.cafe24.com/delete_item.php";
    public static String REGISTER_CARD = "http://pmy0420.cafe24.com/moveToCardTB.php";
    public static String UPDATE_CARD_INFOS = "http://pmy0420.cafe24.com/update_infos.php";
    public static String INSERT_CARD_INFOS = "http://pmy0420.cafe24.com/insert_card_info.php";
    public static String UPDATE_TCARD_RESULT = "http://pmy0420.cafe24.com/update_tcard_result.php";
    public static String DELETE_CARD = "http://pmy0420.cafe24.com/delete_item_cardtb.php";
    public static String INSERT_CARD_INFOS_FROM_CAMERA = "http://pmy0420.cafe24.com/insert_card_from_camera.php";
    public static String INSERT_CARD_INFOS_CARDTB = "http://pmy0420.cafe24.com/register_card.php";
    public static String  SEARCH_CARD_GET_INFO= "http://pmy0420.cafe24.com/get_searchcard.php";
    /**
     * end
     */

    private ImageButton btn_click;
    private ImageButton btn_clickToCamera;
    private ImageButton btn_clickToSearch;
    private RecyclerView recyclerView;
    private CardRecyclerViewAdapter adapter = null;
    private TextView cnt_text;
    private TextView cnt_count_text;
    private TextView noCard_text;
    private TextView card_text;
    private TextView welcome;
    private LinearLayout linearLayout;
    private LinearLayout linearGoToCardList;
    private ImageView btn_goToCardList;
    private ImageView actionbar_btn;
    private ArrayList<CardInfoItem.cardInfo> cardsList;
    private ArrayList<CardInfoItem.cardInfo> unRegedcardsList;
    private SwipeRefreshLayout refreshLayout = null;
    private int card_cound = 0;
    private boolean forThread = false;
    CardOCR cardocr;
    ArrayList<String> tmpCards = new ArrayList<String>(); // done == 0 인 카드 이미지들

    private static final String TAG = "opencv";
    public static String userid = ""; //ocr
    private static final int REQUEST_CODE_GALLERY = 200;
    private static final int REQUEST_CODE = 300;
    private static final int REQUEST_CODE_RESTART = 400;
    private static final int REQUEST_CODE_FOR_OCR = 700;
    private static final int REQUEST_CODE_FOR_LOGIN = 800;

    public static HttpConnection httpConn;
    public static Context mContext;
    public static int kakaoLinkNum =0;


    Handler mHandler = null;
    static int cnt = 0;

    public static int device_width = 0, device_height = 0;

    //user
//    FirebaseUser user;
    private BCMApplication myApp;

    public static String done;//ocr인식 확인

    public native void RecognitionCard(long inputImage, long outputImage);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    static {
        System.loadLibrary("native-lib");

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is not loaded");
        } else {
            Log.d(TAG, "OpenCV is loaded successfully!");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate");
        mContext = this;
        myApp = (BCMApplication) getApplication();

        //actionbar title 가운데
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); // 커스텀 사용
        getSupportActionBar().setCustomView(R.layout.actionbar_title); // 커스텀 사용할 파일 위치
        getSupportActionBar().setTitle("BCManager");


        btn_click = findViewById(R.id.clickToGallery);
        btn_clickToCamera = findViewById(R.id.clickToCamera);
        btn_clickToSearch = findViewById(R.id.clickToSearch);
        recyclerView = findViewById(R.id.recyclerview);
        linearLayout = findViewById(R.id.layout_for_thread);
        cnt_text = findViewById(R.id.cnt_card);
        noCard_text = findViewById(R.id.textview_noCard);
        card_text = findViewById(R.id.textview_registerdCard);
        actionbar_btn = findViewById(R.id.actionbar_btn);
        welcome = findViewById(R.id.welcome_text);
        cnt_count_text = findViewById(R.id.cnt);
        linearGoToCardList = findViewById(R.id.linear_goToCardList);
        btn_goToCardList = findViewById(R.id.btn_goToCardList);
        refreshLayout = findViewById(R.id.refresh_layout);




        //체크해야 할 것: 로그인 여부, 등록된 카드가져오기, 인식됐지만 등록안된 카드 들고오기
        checkCurrentUser();
        getAppKeyHash();

//        if (myApp.unregisterdCards.size() > 0) {
//            linearGoToCardList.setVisibility(View.VISIBLE);
//        } else linearGoToCardList.setVisibility(View.GONE);

        //handler
        mHandler = new Handler();

        //recyclerview
        cardsList = new ArrayList<>();
        unRegedcardsList = new ArrayList<>();


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setNestedScrollingEnabled(false);

        //Display
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        device_width = size.x;
        device_height = size.y;
        Log.d("디바이스 가로 = ", String.valueOf(device_width));
        Log.d("디바이스 세로 = ", String.valueOf(device_height));

        if (myApp.isLogined) {
            Log.d("ID", myApp.userID);
            Log.d("ID", myApp.loginType);
            Log.d("ID", myApp.userEmail);
            Log.d("ID", myApp.userName);

            getCardCount();
//            getUserNumber();
//            getCardInfo();
//            getUnregisterdCardsInfo();

        } else {

        }


        if (cardsList.isEmpty()) {
            card_text.setVisibility(View.GONE);
            noCard_text.setVisibility(View.VISIBLE);
        }

        //btn event
        btn_click.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ConfirmCapture.class);
                startActivityForResult(intent1, REQUEST_CODE);
            }
        });

        btn_clickToCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        actionbar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });

        btn_clickToSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent search = new Intent(getApplicationContext(), SearchingActivity.class);
                startActivity(search);



            }
        });
        btn_goToCardList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //명함리스트로;
                startActivityForResult(new Intent(getApplicationContext(), CardListActivity.class), REQUEST_CODE_RESTART);
            }
        });

        if (cnt > 0) {
            linearLayout.setVisibility(View.VISIBLE);
            cnt_count_text.setText(String.valueOf(cnt));
        }

        //Receive a value from KakaoLink
        try {
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri != null) {
                    kakaoLinkNum = Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("CARD_NUMBER")));
                    Log.d("카카오카드넘버", "In Main Acitivty" + String.valueOf(kakaoLinkNum));
                }
            }
        } catch (NumberFormatException e) {
            Log.d("카카오톡 ", "NumberFormatException " + e.getMessage());
        } catch (RuntimeException e) {
            Log.d("카카오톡 ", "RuntimeException " + e.getMessage());
        }

        if(myApp.isLogined && kakaoLinkNum != 0){
            Log.d("카카오톡공유", "로그인 OK, 바로 Detail페이지로!");
            Intent goToCardInfo = new Intent(getApplicationContext(), DetailInfoActivity.class);
            goToCardInfo.putExtra("cardNumber", kakaoLinkNum);
            goToCardInfo.putExtra("flag", 503);
            startActivity(goToCardInfo);
        }else if(!myApp.isLogined && kakaoLinkNum != 0){
            Log.d("카카오톡공유", "로그인 NO, 바로 로그인 페이지로!");
            Intent goToLogin = new Intent(getApplicationContext(), LoginActivity.class);
//            goToLogin.putExtra("flag", "FROMMAIN");
            startActivity(goToLogin);
        }

        //End of kakaolink


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (myApp.isLogined) {
//                    cardsList.clear();
                    tmpCards.clear();
//                    getCardCount();
                    getCardInfo();
                    refreshLayout.setRefreshing(false);
                } else {
                    refreshLayout.setRefreshing(false);
                }
            }
        });
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.my_green));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG_, "onActivityResult");
        if (requestCode == REQUEST_CODE_RESTART) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG_, "onActivityResult_1");
//                startActivity(getIntent());
                linearGoToCardList.setVisibility(View.GONE);
            }
        }

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Intent datas = data;

                    final Thread thread_ = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Looper.prepare();
                            MessageQueue messageQueue = Looper.myQueue();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //ui 작업 수행o
                                    cnt++;
                                    if (cnt > 0) {
                                        linearLayout.setVisibility(View.VISIBLE);
                                        cnt_count_text.setText(String.valueOf(cnt));
                                    } else {
                                        linearLayout.setVisibility(View.GONE);
                                    }
                                }
                            });

                            byte[] bytes = datas.getByteArrayExtra("image");
                            Bitmap result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            result.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            cnt--;

                            Intent intent = new Intent(getApplicationContext(), ImageOCRActivity.class);
                            intent.putExtra("image", byteArray);
//                            intent.putExtra("filename", byteArray);
                            startActivityForResult(intent, REQUEST_CODE_FOR_OCR);

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //ui 작업 수행o

                                    if (cnt > 0) {
                                        linearLayout.setVisibility(View.VISIBLE);
                                        cnt_count_text.setText(String.valueOf(cnt));
                                    } else {
                                        linearLayout.setVisibility(View.GONE);
                                    }

                                }
                            });
                            Looper.loop();
                        }
                    });
                    thread_.start();
                }
            }
        }
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //        val intent = intent
                if (data != null) {
                    byte[] bytes = data.getByteArrayExtra("image");
                    final String filename = data.getStringExtra("fileName");

                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    detection(bitmap, filename);

                }
            }
        }
        if (requestCode == REQUEST_CODE_FOR_OCR) {
            if (resultCode == RESULT_OK) {
                linearGoToCardList.setVisibility(View.VISIBLE);
            }
        }


    }

    public void detection(final Bitmap bitmap, final String filename) {
        forThread = true;
        final Thread thread = new Thread((new Runnable() {
            @Override
            public void run() {
                //ui 작업 수행x
                Looper.prepare();
                MessageQueue messageQueue = Looper.myQueue();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //ui 작업 수행o
                        cnt++;
                        if (cnt > 0) {
                            linearLayout.setVisibility(View.VISIBLE);
                            cnt_count_text.setText(String.valueOf(cnt));
                        } else {
                            linearLayout.setVisibility(View.GONE);
                        }
                    }
                });

                Log.d("image사이즈", String.valueOf(bitmap.getWidth()) + " " + String.valueOf(bitmap.getHeight()));

                double image_height = bitmap.getHeight();
                double image_width = bitmap.getWidth();
                double tmps = image_width / device_width;
                int dst_height = (int) (image_height / (image_width / device_width));
                Log.d("tmps", String.valueOf(tmps));
                Log.d("image_height", String.valueOf(image_height));
                Log.d("image_width", String.valueOf(image_width));
                Log.d("dst_height", String.valueOf(dst_height));
                Bitmap image = Bitmap.createScaledBitmap(bitmap, device_width, dst_height, true);

                Mat mat_img = new Mat();
                Utils.bitmapToMat(image, mat_img);

                Mat output = new Mat();

                RecognitionCard(mat_img.getNativeObjAddr(), output.getNativeObjAddr());

                if (!output.empty()) {

                    final Bitmap bitmapOutput = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(output, bitmapOutput);


                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
//                                cardocr = new CardOCR(getApplicationContext(), bitmapOutput, userid, filename,filePath);

                    userid = myApp.userID;

                    cardocr = new CardOCR(getApplicationContext(), bitmapOutput, userid, filename, myApp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("여기야 여기", "       ");
                            CardOCR.LableDetectionTask task = null;
                            try {
                                task = new CardOCR.LableDetectionTask(getApplicationContext(), cardocr.prepareAnnotationRequest(bitmapOutput), MainActivity.this);
                                task.execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                } else {
                    final Bitmap failedBitmap = Bitmap.createBitmap(mat_img.cols(), mat_img.rows(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(mat_img, failedBitmap);
                    updateTcardResult(filename);
                    Log.d("파일네임", filename);
                    linearGoToCardList.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //ui 작업 수행o
                        cnt--;
                        if (cnt > 0) {
                            linearLayout.setVisibility(View.VISIBLE);
                            cnt_count_text.setText(String.valueOf(cnt));
                        } else {
                            forThread = false;
                            linearLayout.setVisibility(View.GONE);
                        }

                    }
                });
                Looper.loop();


            }
        }));
        thread.start();
        if (!forThread) thread.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("메인액티비티", "onDestroy");
//        Thread.interrupted();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("메인액티비티", "onPause");
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        if (myApp.isLogined) inflater.inflate(R.menu.menu_login, popup.getMenu());
        else inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }

    public void updateTcardResult(String tCardImage) {
        try {
            HttpConnection httpConnection = new HttpConnection(new URL(UPDATE_TCARD_RESULT));
            httpConnection.requestUpdateTcardResult(tCardImage, new OnRequestCompleteListener() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable String data) {
                    if (data != null && !data.isEmpty()) {
                        if (data.equals("1")) Log.d("MainActivity", "update TcardTb result 성공");
                    }
                }

                @Override
                public void onError() {
                    Log.d("MainActivity", "update TcardTb result 실패");
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.menu_logout:
                if (myApp.loginType.equals("g")) {
                    Log.d("FirebaseAuth Login", "Logout");
                    FirebaseAuth.getInstance().signOut();
                    startActivity(getIntent());

                } else {

                    Log.d("Kakao Login", "Logout");
                    Log.d("Kakao Login_type  ", myApp.loginType);
                    UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                        @Override
                        public void onCompleteLogout() {
                            initUserData();
                            startActivity(getIntent());
                        }
                    });
                }
                finish();
                initUserData();
                startActivity(getIntent());
                return true;
            case R.id.menu_userinfo:
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initUserData() {
        myApp.isLogined = false;
        myApp.userNum = null;
        myApp.userImage = null;
    }

    public void checkCurrentUser() {
        // [START check_current_user]
        if (myApp.isLogined) {

            welcome.setText(myApp.userName + " 님 인식된 \n명함을 확인하세요!");
        } else {
            linearGoToCardList.setVisibility(View.GONE);
            // No user is signed in
            Log.d(TAG_, "null");
        }


        // [END check_current_user]
    }

    void getCardCount() {
        try {

            Log.d("겟카운트", "실행");
            HttpConnection httpConn = new HttpConnection(new URL(GET_UNREGISTERD_CARD_COUNT));
            httpConn.requestGetCards(myApp.userID, new OnRequestCompleteListener() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable String data) {

                    if (data != null && !data.isEmpty()) {
                        tmpCards.clear();
                        Log.d("카운트", data);
                        Gson gson = new GsonBuilder()
                                .create();
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(data);
                        JsonObject jsonArray = (JsonObject) jsonObject.get("cardInfo");
                        JsonArray jObject1 = (JsonArray) jsonArray.get("count");
                        JsonArray jObject2 = (JsonArray) jsonArray.get("image");

                        myApp.count = jObject1.get(0).getAsJsonObject().get("count").getAsInt();

                        for (int i = 0; i < jObject2.size(); i++) {
                            JsonObject j = jObject2.get(i).getAsJsonObject();
                            tmpCards.add(j.get("TCARD_IMAGE").toString().replace("\"", ""));
                        }

                        for (int i = 0; i < tmpCards.size(); i++) {
                            final String filename = tmpCards.get(i);
                            Glide.with(MainActivity.this)
                                    .asBitmap().load(IMAGE_URL + filename)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .listener(new RequestListener<Bitmap>() {
                                                  @Override
                                                  public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                                      return false;
                                                  }

                                                  @Override
                                                  public boolean onResourceReady(final Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                                      runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              detection(bitmap, filename);
                                                          }
                                                      });
                                                      return false;
                                                  }
                                              }
                                    ).submit();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getCardInfo();
                                if (myApp.count > 0)
                                    linearGoToCardList.setVisibility(View.VISIBLE);
                                else linearGoToCardList.setVisibility(View.GONE);


                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    void getCardInfo() {
        try {
            HttpConnection httpConn = new HttpConnection(new URL(GET_CARDS_INFO));
            httpConn.requestGetCards(myApp.userID, new OnRequestCompleteListener() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable String data) {

                    if (data != null && !data.isEmpty()) {
                        cardsList.clear();
                        Log.d("성공 ", data);
                        Gson gson = new GsonBuilder().create();
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(data);
                        JsonArray jsonArray = (JsonArray) jsonObject.get("cardInfo");

                        CardInfoItem.cardInfo result;
                        for (int i = 0; i < jsonArray.size(); i++) {
                            final JsonObject j = jsonArray.get(i).getAsJsonObject();
                            result = gson.fromJson(j, CardInfoItem.cardInfo.class);
                            cardsList.add(result);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_, String.valueOf(cardsList.size()));
                                card_text.setVisibility(View.VISIBLE);
                                noCard_text.setVisibility(View.GONE);
                                adapter = new CardRecyclerViewAdapter(MainActivity.this, cardsList);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                card_text.setVisibility(View.GONE);
                                noCard_text.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume실행");
        if (myApp.isLogined) getCardInfo();

//        if (myApp.isLogined) getCardCount();
        if (myApp.count == 0) {
            linearGoToCardList.setVisibility(View.GONE);
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }

    @Override
    public void processFinish(String output) {
        Log.d("processFinish", "성공");

        String tmp = output;

        output.replaceAll(" ", "");

        if (output.equals("true")) {
            Log.d("processFinish", "내부");
        }

        myApp.count++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearGoToCardList.setVisibility(View.VISIBLE);
            }
        });
    }

}

interface AsyncResponse {
    void processFinish(String output);
}



