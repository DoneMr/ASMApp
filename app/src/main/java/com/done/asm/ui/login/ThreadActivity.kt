package com.done.asm.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.done.asm.KtCost
import com.done.asm.R
import com.done.testlibrary.TestLivActivity

class ThreadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread)
        testCost()
    }

    @KtCost
    private fun testCost() {
        val tv = findViewById<TextView>(R.id.tv_test).apply {
            this.text = "我是方法耗时方法"
        }.setOnClickListener {
            startActivity(Intent(it.context, TestLivActivity::class.java))
        }
    }
}