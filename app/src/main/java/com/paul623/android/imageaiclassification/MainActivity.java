package com.paul623.android.imageaiclassification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paul623.android.imageaiclassification.ml.Model;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TimeCounter timeCounter;
    private TextView textView;
    String src="";
    public List<String> allPath;
    public ListView listView;
    public double correct_point=0.85;
    private String curStatus="";
    private int curNum=0;
    private ProgressBar progressBar;
    public ArrayList<String> others_image=new ArrayList<>();
    public ArrayList<String> power_imagePaths=new ArrayList<>();
    public ArrayList<String> writing_imagePaths=new ArrayList<>();
    public List<KindsBean> kindsBeans=new ArrayList<>();
    Model model ;
    ScanningListAdapter scanningListAdapter;
    public Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case 1:
                    Thread thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                model=Model.newInstance(MainActivity.this);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            timeCounter.start();
                            processPhoto();
                        }
                    });
                    thread.start();
                    src=src+"?????????????????????\n";
                    textView.setText(src);
                    break;
                case 2:
                    src=src+curStatus+"\n";
                    src=src+"??????????????????????????????\n";
                    textView.setText(src);
                    scanningListAdapter=new ScanningListAdapter(dealWithResult(),MainActivity.this);
                    listView.setAdapter(scanningListAdapter);
                    src=src+"????????????,??????: "+timeCounter.stop();
                    textView.setText(src);
                    break;
                case 3:
                    textView.setText(src+curStatus);
                    progressBar.setProgress(curNum+1);
                    break;
            }
            return false;
        }
    });
    int maxNum=300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Request();
        initAll();
    }
    public void initAll(){
        listView=findViewById(R.id.lv_album_lessonselect);
        textView=findViewById(R.id.tv_content);
        progressBar=findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        timeCounter=new TimeCounter();
        LitePal.initialize(MainActivity.this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        Album.gallery(MainActivity.this)
                                .checkedList(others_image)
                                .checkable(false)
                                .start();
                        break;
                    case 1:
                        Album.gallery(MainActivity.this)
                                .checkedList(power_imagePaths)
                                .checkable(false)
                                .start();
                        break;
                    case 2:
                        Album.gallery(MainActivity.this)
                                .checkedList(writing_imagePaths)
                                .checkable(false)
                                .start();
                        break;
                }
            }
        });
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new ImageLoader())
                .build());
        if(LitePal.count(KindsBean.class)!=0){
            int count=0;
            List<String> lists[]= new ArrayList[]{others_image, power_imagePaths, writing_imagePaths};
            kindsBeans=LitePal.findAll(KindsBean.class);
            for(KindsBean kindsBean:kindsBeans){
                List<ImageBean> imageBeans=LitePal.where("kindName = ?",kindsBean.getKindName()).find(ImageBean.class);
                for(ImageBean imageBean:imageBeans){
                    lists[count].add(imageBean.path);
                }
                count++;
            }
            scanningListAdapter=new ScanningListAdapter(kindsBeans,MainActivity.this);
            listView.setAdapter(scanningListAdapter);
        }
    }
    protected void Request () {             //??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//????????????
            if (!checkAllPre())
            {
                final android.app.AlertDialog.Builder normalDialog =
                        new android.app.AlertDialog.Builder(this);
                normalDialog.setIcon(R.mipmap.ic_launcher_default);
                normalDialog.setTitle("????????????");
                normalDialog.setMessage("?????????????????????????????????????????????????????????"
                        +"\n"+"????????????????????????"
                        +"\n"+"????????????"
                        +"\n"+"????????????????????????"
                        +"\n"+"????????????????????????????????????????????????????????????????????????????????????????????????" );
                normalDialog.setPositiveButton("?????????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.ACCESS_WIFI_STATE}, 1);
                            }
                        });
                normalDialog.setNegativeButton("?????????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_SHORT).show();
                                //Toasty.error(getContext(), "??????????????????", Toast.LENGTH_SHORT, true).show();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        });
                normalDialog.setNeutralButton("????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String shareURL="https://www.yuque.com/docs/share/0eefd667-7171-44a1-ac6d-0461a1d97a63?#";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);//???Intent????????????
                        intent.setData(Uri.parse(shareURL));//???Intent????????????
                        startActivity(intent);//???Intent?????????Activity
                    }
                });
                normalDialog.show();

            }
        }

    }
    public Boolean checkAllPre() {
        Boolean flag=true;
        String premissons[]={Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        for(String item:premissons)
        {
            if(this.checkSelfPermission(item) != PackageManager.PERMISSION_GRANTED)
            {
                flag=false;
                Log.d("????????????",item);
                break;
            }
        }
        return flag;
    }
    public void getAllPhotos(){
        //if?????? ????????????SD??????????????????????????????
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            //????????????????????????list?????? getGalleryPhotos??????
            allPath = new ArrayList<>();
            //allPath = getGalleryPhotos(getContentResolver());
            //allPath= FileManager.getFilePathList(ScanningActivity.this);
            allPath= FileManager.getImageFileList(MainActivity.this);
            src=src+"????????????"+5+" Float Working Mode???CPU"+"\n";
            src=src+"????????????:"+allPath.size()+"\n";
            src=src+"??????????????????????????????????????? "+correct_point+"\n";
            if(allPath.size()>=maxNum){
                src=src+"??????"+maxNum+"???????????????????????????\n";
            }
            textView.setText(src);
            Message message=new Message();
            src=src+"??????????????????????????????\n";
            textView.setText(src);
            message.what=1;
            handler.sendMessage(message);
        }
    }
    public void processPhoto(){
        int num=allPath.size();
        if(num>=maxNum){
            num=maxNum;
        }
        progressBar.setMax(num);
        for(int i=0;i<num;i++){
            curNum=i;
            curStatus="?????????????????????"+(i+1)+"???,??????:"+num+"???,?????????"+allPath.get(i);
            Message message=new Message();
            message.what=3;
            handler.sendMessage(message);
            recognize(allPath.get(i),i);
        }
        model.close();
        Message message=new Message();
        message.what=2;
        handler.sendMessage(message);

    }
    private void recognize(String path,int i) {
        Bitmap bitmap= ImageHelper.SuperSuitWay(this,path);
        TensorImage image = TensorImage.fromBitmap(bitmap);
        // Runs model inference and gets result.
        Model.Outputs outputs = model.process(image);
        List<Category> probability = outputs.getProbabilityAsCategoryList();
        if(i==0){
            for(Category category:probability){
                kindsBeans.add(new KindsBean(category.getLabel()));
            }
        }
        for(int j=0;j<probability.size();j++){
            if(probability.get(j).getScore()>correct_point){
                switch (j){
                    case 0:
                        others_image.add(path);
                        break;
                    case 1:
                        power_imagePaths.add(path);
                        break;
                    default:
                        writing_imagePaths.add(path);
                        break;
                }
                //?????????????????????????????????????????????
                ImageBean imageBean=new ImageBean();
                imageBean.setKindName(probability.get(j).getLabel());
                imageBean.setPath(path);
                imageBean.save();
            }
        }
        bitmap.recycle();
    }
    public void startAI(View view) {
        showEditTextDialog();
    }
    private List<KindsBean> dealWithResult() {
        List<String> lists[]= new ArrayList[]{others_image, power_imagePaths, writing_imagePaths};
        int count=0;
        for(List<String> i:lists){
            kindsBeans.get(count).setKindNumber(i.size());
            if(kindsBeans.get(count).getKindNumber()==0){
                kindsBeans.get(count).setFirstPhotoPath("");
            }else {
                kindsBeans.get(count).setFirstPhotoPath(i.get(0));
            }
            kindsBeans.get(count).save();//???????????????
            count++;
        }
        return kindsBeans;
    }
    private void showEditTextDialog(){
        final EditText inputServer = new EditText(MainActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("????????????").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setMessage("?????????????????????(0.6-1.0),?????????????????????????????????????????????300???")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //double words = Double.parseDouble(inputServer.getText().toString());
                        String words=inputServer.getText().toString();
                        String[] values =words.split(" ");
                        Double correct_point_user;
                        if(values.length==2){
                            maxNum=Integer.parseInt(values[1]);
                        }
                        correct_point_user=Double.parseDouble(values[0]);
                        if(correct_point_user>0.6&&correct_point_user<1.0){
                            correct_point=correct_point_user;
                            Toast.makeText(MainActivity.this,"????????????????????????",Toast.LENGTH_SHORT).show();
                            LitePal.deleteAll(KindsBean.class);
                            LitePal.deleteAll(ImageBean.class);
                            if(scanningListAdapter!=null){
                                scanningListAdapter.clear();
                            }
                            dialog.dismiss();
                            getAllPhotos();
                        }else {
                            Toast.makeText(MainActivity.this,"???????????????",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.show();

    }
}