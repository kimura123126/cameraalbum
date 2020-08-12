package com.example.dell1.cameraalbumtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_main.*

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        // 配置了 android:scrollbars="vertical" 要写下一句才会滚动
        // Caused by: java.lang.IllegalStateException: noteBtn must not be null
        //没用 还是不能动 还是老实使用ScrollView吧
        //noteBtn?.movementMethod = ScrollingMovementMethod.getInstance()

    }
}