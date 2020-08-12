package com.example.dell1.cameraalbumtest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    //申请两个权限
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)

    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    private val mPermissionList = ArrayList<String>()

    private val mRequestCode = 100 //权限请求码


    val takePhoto = 1
    val takePhoto11 = 11
    val fromAlbum = 2
   // lateinit var imageUri: Uri
  //  lateinit var outputImage: File


   private val outputImage by lazy{
       File(externalCacheDir, "output_image.jpg")
   }



    private val imageUri by lazy{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //第二个参数可以是任意的字符串 但需要和配置文件里<provider>的
            //android:authorities 一致，且每个应用这个值是唯一的
            //如果其他应用配置了这个值，那么这个应用安装的时候就会失败
            //一般不会发生这种现象，因为不同的应用包名是不一样，
            // 而authorities命名跟包名走的



            //高版本的使用如下方法是出于安全考虑 p369<<第一行代码 第3版>>
            //FileProvider是一种特殊的ContentProvider 使用了和ContentProvider类似
            //的机制来对数据进行保护
            FileProvider.getUriForFile(this,
                "com.example.dell1.cameraalbumtest.fileprovider", outputImage)
        } else {
            Uri.fromFile(outputImage) //系统版本低于7.0 File对象转换为Uri对象
            //Uri对象标识着该图片的本地真实路径
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        //lateinit property outputImage has not been initialized
        // at android.app.ActivityThread.deliverResults(ActivityThread.java:3979)
        //粗心大意造成的 前面已经声明了一个outputImage 这里的还加个val 错误！！！
        takePhotoBtn.setOnClickListener { //使用关联缓存目录存放图片 出于安全原因。具体见第三版P369
           //  outputImage = File(externalCacheDir, "output_image.jpg")//图片命名为output_image.jpg
            try {
                if (outputImage.exists()) {
                    outputImage.delete()
                }
                outputImage.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri) //指定图片输出地址
            startActivityForResult(intent, takePhoto)//打开相机程序 拍下地照片存储到output_image.jpg
        }

        takePhotoBtn2.setOnClickListener {
          if(initPermission() == 1){
              startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),takePhoto11)
          }else{
              Toast.makeText(this,"请开启读写文件的授权",Toast.LENGTH_SHORT).show()
          }


        }

        fromAlbumBtn.setOnClickListener {
            //打开文件选择器
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type="image/*"//指定只显示图片
            startActivityForResult(intent,fromAlbum)
        }

        noteBtn.setOnClickListener {
            intent = Intent(this,InfoActivity::class.java)
            startActivity(intent)
        }
    }

   //拍完照后 有结果回调下面的方法 显示照片
    //可能时手机性能不好 点击调用系统相机的确定按钮 有时有反应有时没反应
    //或许可能是图片太大没有经过压缩，直接加载到应用的缓存内存中可能会导致程序崩溃
    //那应该可以把图片不放到应用关联缓存目录
   // 放到sd卡的其他目录应该就不会点击按钮没反应了吧（需要运行时权限）
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
     //  Log.d("打印imageUri 进入回调方法",imageUri.toString())
     Toast.makeText(this,"回调onActivityResult()",Toast.LENGTH_SHORT).show()
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {

                    try{
                        // 将拍摄的照片显示出来
                        //可能手机性能不好 没调用rotateIfRequired()来处理照片旋转的情况
                        //调用处理照片旋转的方法 有时点击照片照好后的按钮半天没反应
                        // Logcat提示bitmap 为 null

                        //imageUri!! 就是自己主观标记它不为空 不要kotlin来检查它空不空，有没有初始化了
                        Log.d("打印imageUri",imageUri.toString())
                        //将照片解析为Bitmap对象
                        val stream = contentResolver.openInputStream(imageUri)
                        Log.d("打印stream","：$stream")
                        //不显示照片时 stream 还是不为空  bitmap为空
                        //打印stream: ：java.io.FileInputStream@672e8be
                        //打印bitmap: BitmapFactory后 setImageBitmap前：null
                        val bitmap = BitmapFactory.decodeStream(stream)

                        if(bitmap == null){
                            Toast.makeText(this,"获取图片失败 bitmap = null",Toast.LENGTH_SHORT).show()
                        }

                        //换成下面这样bitmap也可能为null
                         /* val bytes = toByteArray(stream)
                          val bitmap = bytes?.size?.let {
                              BitmapFactory.decodeByteArray(bytes,0,
                                  it
                              )
                          }*/
                       // Log.d("打印bitmap",bitmap.toString())
                       // Log.d("打印bitmap","BitmapFactory后 setImageBitmap前：$bitmap")
                        imageView.setImageBitmap(bitmap);//显示图片  没有图片显示时 打印bitmap: BitmapFactory后 setImageBitmap前：null


                        /* D/打印imageUri: file:///storage/emulated/0/Android/data/com.example.dell1.cameraalbumtest/cache/output_image.jpg
                          D/打印bitmap: android.graphics.Bitmap@6c03247
                          闪退地时候bitmap为空 'java.lang.String android.graphics.Bitmap.toString()' on a null object reference
                          那就是没照在ImageView ，bitmap为空*/
                    }catch (e: FileNotFoundException){
                        e.printStackTrace();
                    }



                }
            }


            takePhoto11 ->{

                    saveCameraImage(data)

            }

            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的照片显示
                        val bitmap = getBitmapFromUri(uri)
                        imageView.setImageBitmap(bitmap)
                    }//let
                }//if
            }//fromAlbum
        }//when
    }


    /** 保存相机的图片 到SD卡 */

    private fun saveCameraImage(data: Intent?) {
        // 检查sd card是否存在
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
           // Log.i(FragmentActivity.TAG, "sd card is not avaiable/writeable right now.")
            Toast.makeText(this,"sd card is not avaiable/writeable right now.",Toast.LENGTH_SHORT).show()
            return
        }
        // 为图片命名啊
        val name = "output_image.jpg"
        val bmp = data?.extras!!["data"] as Bitmap? // 解析返回的图片成bitmap
        Log.d("打印bmp", bmp.toString())
        // 保存文件
        var fos: FileOutputStream? = null

        val file = File("/mnt/sdcard/test/","output_image.jpg")
        try {
            if (file.exists()) {
                file.delete()
            }

            file.mkdirs()
            //这里好像不能用file.createNewFile() 会连同文件名一起创建
            //后面的fos = FileOutputStream(fileName) 可能就不能执行了
            //待测试
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d("打印file", file.toString())
        val fileName = "/mnt/sdcard/test/$name" // 保存路径
        try { // 写入SD card
            fos = FileOutputStream(fileName)

            bmp?.compress(Bitmap.CompressFormat.JPEG, 100, fos)


        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.flush()//fos!!.flush() 用这个会提示错误
                fos?.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } // 显示图片
        imageView.setImageBitmap(bmp)
    }





    //权限判断和申请
    private fun initPermission():Int {
        var success = 0
        mPermissionList.clear() //清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        //如果AndoridManifest.xml 设置了 checkSelfPermission() 检查是有权限的
        //6.0的vivo 机子 运行时权限在 AndoridManifest.xml 也要配置，不配写了运行时权限也会闪退

        //AndoridManifest.xml配置了权限后 程序运行相应需要权限时vivo系统自己会弹出授权框
        for (i in 0 until permissions.size) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[0]) //添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size > 0) {
            //有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode)

        } else {
            //说明权限都已经通过，可以做你想做的事情去
            //比如 拍照
            success = 1
        }

        return success
    }


    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasPermissionDismiss = false //有权限没有通过
        if (mRequestCode == requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog() //跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            } else {
                //全部权限通过，可以进行下一步操作。。。
            }
        }
    }


    /**
     * 不再提示权限时的展示对话框
     */


    private val mPackName = "com.example.dell1.cameraalbumtest"

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).apply {
                 setMessage("已禁用权限 请手动授予")
                 setPositiveButton("设置"){dialog,which ->

                     val packageURI = Uri.parse("package:$mPackName")
                     val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                     startActivity(intent)

            }
            setNegativeButton("取消"){dialog,which ->
                //关闭页面或者做其他操作

            }
            create()
            show()

        }
    }




    private fun toByteArray(input: InputStream?): ByteArray? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var n = 0
       if (input != null) {
           while (-1 != input.read(buffer).also { n = it }) {
               output.write(buffer, 0, n)
           }
       }
        return output.toByteArray()
    }


    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    //处理照片旋转的情况 下面加了判断图片方向的代码 如果图片有旋转，那么就将图片旋转相应的角度
    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedBitmap
    }




}