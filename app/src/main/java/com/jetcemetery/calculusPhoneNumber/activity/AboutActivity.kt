package com.jetcemetery.calculusPhoneNumber.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.jetcemetery.androidcalculus.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val what = findViewById<EditText>(R.id.txt_What)
        val why = findViewById<EditText>(R.id.txt_Why)
        val setting = findViewById<EditText>(R.id.txt_Setting)
        val how = findViewById<EditText>(R.id.txt_How)

        what.setTextColor(Color.BLACK)
        why.setTextColor(Color.BLACK)
        setting.setTextColor(Color.BLACK)
        how.setTextColor(Color.BLACK)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Calling onResume")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.menu_about) {
                menu.getItem(i).isVisible = false
                break
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.menu_home -> {
                Log.d(TAG, "Menu button hit, going to menu_home")
                intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_settings -> {
                Log.d(TAG, "Menu button hit, going to menu_settings")
                intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    companion object {
        private const val TAG = "AboutActivity"
    }
}
