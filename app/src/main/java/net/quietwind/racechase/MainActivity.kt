package net.quietwind.racechase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.quietwind.racechase.ui.TimingFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TimingFragment.newInstance())
                .commitNow()
        }
    }
}