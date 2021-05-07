package com.example.zadanie2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        var typeMap = intent.getStringExtra("typeMap")
        if(typeMap?.toInt()==1)first.isChecked=true
        else second.isChecked=true
    }

    fun saveAndReturnClick(view: View) {
        var i=Bundle()
        var t=0
        if(first.isChecked)
           t=1
        else
            t=2
        i.putString("typeMap",t.toString() )
        intent.putExtras(i)
        setResult(1,intent)
        finish()
    }
}