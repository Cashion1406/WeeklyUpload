package com.example.weeklyupload

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        //setupActionBarWithNavController(findNavController(R.id.fragmentContainerView))


        btm_navigate.setupWithNavController(findNavController(R.id.fragmentContainerView))

    }

    override fun onSupportNavigateUp(): Boolean {

        return findNavController(R.id.fragmentContainerView).navigateUp() || super.onSupportNavigateUp()
    }

}