package com.example.dell1.cameraalbumtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private ImageView picture;
    private Uri imageUri;

    //新内容
    private List<String> names = new ArrayList<>();
    private List<String> descs = new ArrayList<>();
    private List<String> fileNames = new ArrayList<>();
    private RecyclerView show;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        picture = (ImageView) findViewById(R.id.picture);

        //新增55-70

        Button addBn = findViewById(R.id.add);
        Button viewBn = findViewById(R.id.view);
        show = findViewById(R.id.show);
        // 为RecyclerView设置布局管理器
        show.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        // 为viewBn按钮的单击事件绑定监听器
        viewBn.setOnClickListener(view ->
                // 请求读取外部存储器的权限
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x123));

        // 为addBn按钮的单击事件绑定监听器
        addBn.setOnClickListener(view ->
                // 请求写入外部存储器的权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x456));



        takePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //存放图片
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24){
                    //第二个参数可以是任意的字符串
                    imageUri = FileProvider.getUriForFile(MainActivity.this,
                           "com.example.dell1.cameraalbumtest.fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);//系统版本低于7.0
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);//指定图片输出地址
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        chooseFromAlbum.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
    }





    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }


        if (grantResults[0] == 0) {
            if (requestCode == 0x123) {
                // 清空names、descs、fileNames集合里原有的数据
                names.clear();
                descs.clear();
                fileNames.clear();
                // 通过ContentResolver查询所有图片信息
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
                while (cursor.moveToNext())
                {
                    // 获取图片的显示名
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    // 获取图片的详细描述
                    String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
                    // 获取图片的保存位置的数据
                    byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    // 将图片名添加到names集合中
                    names.add(name);
                    // 将图片描述添加到descs集合中
                    descs.add(desc);
                    // 将图片保存路径添加到fileNames集合中
                    fileNames.add(new String(data, 0, data.length - 1));
                }
                cursor.close();
                RecyclerView.Adapter adapter = new RecyclerView.Adapter<LineViewHolder>(){

                    @NonNull
                    @Override
                    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View itemView = getLayoutInflater().inflate(R.layout.line,
                                new LinearLayout(MainActivity.this), false);
                        return new LineViewHolder(itemView);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull LineViewHolder lineViewHolder, int i)
                    {
                        lineViewHolder.nameView.setText(names.get(i) == null ? "null": names.get(i));
                        lineViewHolder.descView.setText(descs.get(i) == null ? "null": descs.get(i));
                    }

                    @Override
                    public int getItemCount()
                    {
                        return names.size();
                    }
                };
                // 为show RecyclerView组件设置Adapter
                show.setAdapter(adapter);
            }
            if (requestCode == 0x456) {
// 创建ContentValues对象，准备插入数据
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "qiufen");
                values.put(MediaStore.Images.Media.DESCRIPTION, "秋分");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
// 插入数据，返回所插入数据对应的Uri
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
// 加载应用程序下的jinta图片
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.qiufen);
                try(
                        // 获取刚插入的数据的Uri对应的输出流
                        OutputStream os = getContentResolver().openOutputStream(uri)) // ①
                {
                    // 将bitmap图片保存到Uri对应的数据节点中
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                } catch (IOException e)	{
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,"图片添加成功", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.permisssion_tip, Toast.LENGTH_SHORT)
                    .show();
        }


    }

    class LineViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameView, descView;
        public LineViewHolder(@NonNull View itemView)
        {
            super(itemView);
            nameView = itemView.findViewById(R.id.name);
            descView = itemView.findViewById(R.id.desc);
            itemView.setOnClickListener(view -> {
                // 加载view.xml界面布局代表的视图
                View viewDialog = getLayoutInflater().inflate(R.layout.view, null);
                // 获取viewDialog中ID为image的组件
                ImageView image = viewDialog.findViewById(R.id.image);
                // 设置image显示指定图片
                image.setImageBitmap(BitmapFactory.decodeFile(fileNames.get(getAdapterPosition())));
                // 使用对话框显示用户单击的图片
                new AlertDialog.Builder(MainActivity.this)
                        .setView(viewDialog).setPositiveButton("确定", null).show();
            });
        }
    }



    @Override
    //拍完照有结果返回到此方法
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try{
                        //将照片解析为Bitmap对象
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);//显示图片
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeKitKat(data);//4.4以下
                    }
                }
                break;
            default:
                break;
        }
    }
    //解析URi
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath =null;
        Uri uri = data.getData();
        //是document类型的话
        if (DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            //Uri的authority是media格式的话，document id进一步解析
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                //取出id
                String id= docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" +id;
                //构建新的Uri和条件语句
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }
    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri,String selection){
        String path =null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

}
