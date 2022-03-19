package net.quietwind.racechase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import net.quietwind.racechase.databinding.MainActivityBinding
import net.quietwind.racechase.ui.TimingFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*
        setContentView(R.layout.main_activity)
        Log.d("RaceChaseMain", "Checking Instance")
        if (savedInstanceState == null) {
            Log.d("RaceChaseMain", "Making new TimingFragment Instance")
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TimingFragment.newInstance())
                .commitNow()
        }
         */
    }
}