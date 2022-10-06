package `in`.hexcommand.asktoagri.ui.splashscreen

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.ui.onboard.OnBoardPermissionActivity
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var ah: AppHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ls = LocalStorage(this)
        ah = AppHelper(this)

        if (ls.getValueString("locale").isNotEmpty()) {
            ah.setAppLocale(this@SplashActivity, ls.getValueString("locale"))
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            if (ls.getValueBoolean("is_login")) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, OnBoardPermissionActivity::class.java))
            }

            finish()
        }, 3000)
    }
}