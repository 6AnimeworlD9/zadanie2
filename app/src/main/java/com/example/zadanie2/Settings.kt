package com.example.zadanie2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        var typeMap = intent.getStringExtra("typeMap")
        if(typeMap?.toInt()==1)first.isChecked=true
        else second.isChecked=true
        editText.setText(intent.getStringExtra("titleM"))
        var colorM=intent.getStringExtra("colorM")
        if(colorM?.toInt()==1)red.isChecked=true
        else if(colorM?.toInt()==2)green.isChecked=true
        else if(colorM?.toInt()==3)yellow.isChecked=true
        else blue.isChecked=true
    }

    fun saveAndReturnClick(view: View) {
        var i=Bundle()
        var t=0
        var t1=0
        if(first.isChecked)
           t=1
        else
            t=2
        if(red.isChecked)t1=1
        else if(green.isChecked)t1=2
        else if(yellow.isChecked)t1=3
        else t1=4
        i.putString("typeMap",t.toString() )
        i.putString("titleM",editText.text.toString())
        i.putString("colorM",t1.toString())
        intent.putExtras(i)
        setResult(1,intent)
        finish()
    }
}