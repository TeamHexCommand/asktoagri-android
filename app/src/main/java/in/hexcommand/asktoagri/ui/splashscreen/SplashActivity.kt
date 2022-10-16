package `in`.hexcommand.asktoagri.ui.splashscreen

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.User
import `in`.hexcommand.asktoagri.ui.expert.ExpertActivity
import `in`.hexcommand.asktoagri.ui.login.LoginSelectionActivity
import `in`.hexcommand.asktoagri.ui.onboard.OnBoardPermissionActivity
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var ah: AppHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ls = LocalStorage(this)
        ah = AppHelper(this)

        ah.getFCMToken()
        ah.getRemoteConfig(getString(R.string.config_url))

        if (!ls.getValueBoolean("show_onboard")) {
            ls.save("latitude", "0")
            ls.save("longitude", "0")
        }

        if (ls.getValueString("locale").isNotEmpty()) {
            ah.setAppLocale(this@SplashActivity, ls.getValueString("locale"))
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            if (ls.getValueBoolean("is_login")) {
                ah.saveUserInfo()
                finishLogin()
            } else {
                startActivity(
                    Intent(
                        this,
                        OnBoardPermissionActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
            finish()
        }, 3000)

    }

    private fun finishLogin() {

        try {

            val userModel = Gson().fromJson(
                ls.getValueString("current_user"),
                User::class.java
            )

            ls.save("user_id", userModel.id)
            ls.save("user_mobile", userModel.mobile)

            if (userModel.isBanned == 1) {
                Toast.makeText(
                    this@SplashActivity,
                    getString(R.string.msg_ban),
                    Toast.LENGTH_SHORT
                ).show()
                ah.logoutUser()
                startActivity(
                    Intent(this@SplashActivity, LoginSelectionActivity::class.java).setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
            } else {
                if (userModel.isExpert == 1) {
                    ls.save("user", "expert")
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            ExpertActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                } else {
                    ls.save("user", "farmer")
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            MainActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            }
        } catch (e: NullPointerException) {
            ah.logoutUser()
            startActivity(
                Intent(this@SplashActivity, LoginSelectionActivity::class.java).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
        }
    }
}