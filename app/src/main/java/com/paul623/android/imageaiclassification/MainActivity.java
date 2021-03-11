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
                    src=src+"图片预处理成功\n";
                    textView.setText(src);
                    break;
                case 2:
                    src=src+curStatus+"\n";
                    src=src+"正在处理图片识别结果\n";
                    textView.setText(src);
                    scanningListAdapter=new ScanningListAdapter(dealWithResult(),MainActivity.this);
                    listView.setAdapter(scanningListAdapter);
                    src=src+"处理完整,耗时: "+timeCounter.stop();
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
    protected void Request () {             //获取读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//版本判断
            if (!checkAllPre())
            {
                final android.app.AlertDialog.Builder normalDialog =
                        new android.app.AlertDialog.Builder(this);
                normalDialog.setIcon(R.mipmap.ic_launcher_default);
                normalDialog.setTitle("权限说明");
                normalDialog.setMessage("为了本软件的正常运行，需要申请以下权限"
                        +"\n"+"存储卡的读写权限"
                        +"\n"+"相机权限"
                        +"\n"+"日历读写日历权限"
                        +"\n"+"程序在使用过程中遇到的问题，都可以通过“联系我们”的方式向我反馈" );
                normalDialog.setPositiveButton("朕准了",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.ACCESS_WIFI_STATE}, 1);
                            }
                        });
                normalDialog.setNegativeButton("不允许",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getContext(),"吓死我了···",Toast.LENGTH_SHORT).show();
                                //Toasty.error(getContext(), "吓死我了···", Toast.LENGTH_SHORT, true).show();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        });
                normalDialog.setNeutralButton("隐私协议", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String shareURL="https://www.yuque.com/docs/share/0eefd667-7171-44a1-ac6d-0461a1d97a63?#";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);//为Intent设置动作
                        intent.setData(Uri.parse(shareURL));//为Intent设置数据
                        startActivity(intent);//将Intent传递给Activity
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
                Log.d("检测权限",item);
                break;
            }
        }
        return flag;
    }
    public void getAllPhotos(){
        //if语句 没有读写SD卡的权限，就申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            //获取所有图片存入list集合 getGalleryPhotos方法
            allPath = new ArrayList<>();
            //allPath = getGalleryPhotos(getContentResolver());
            //allPath= FileManager.getFilePathList(ScanningActivity.this);
            allPath= FileManager.getImageFileList(MainActivity.this);
            src=src+"线程数："+5+" Float Working Mode：CPU"+"\n";
            src=src+"图片总数:"+allPath.size()+"\n";
            src=src+"目前，图片识别筛选被设置为 "+correct_point+"\n";
            if(allPath.size()>=maxNum){
                src=src+"选取"+maxNum+"张图片进行识别操作\n";
            }
            textView.setText(src);
            Message message=new Message();
            src=src+"正在识别，请耐心等待\n";
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
            curStatus="目前正在处理第"+(i+1)+"张,共计:"+num+"张,地址："+allPath.get(i);
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
                //将筛选出来的照片存储到数据库中
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
            kindsBeans.get(count).save();//保存数据库
            count++;
        }
        return kindsBeans;
    }
    private void showEditTextDialog(){
        final EditText inputServer = new EditText(MainActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("输入筛选").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setMessage("筛选概率区间为(0.6-1.0),（可选）指定扫描个数（不填默认300）")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
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
                            Toast.makeText(MainActivity.this,"本地数据已被清空",Toast.LENGTH_SHORT).show();
                            LitePal.deleteAll(KindsBean.class);
                            LitePal.deleteAll(ImageBean.class);
                            if(scanningListAdapter!=null){
                                scanningListAdapter.clear();
                            }
                            dialog.dismiss();
                            getAllPhotos();
                        }else {
                            Toast.makeText(MainActivity.this,"数值非法！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.show();

    }
}