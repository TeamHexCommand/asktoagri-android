package `in`.hexcommand.asktoagri.ui.onboard

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.helper.PermissionHelper
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OnBoardPermissionActivity : AppCompatActivity() {

    private lateinit var mPrimaryBtn: MaterialButton
    private lateinit var mSecondaryBtn: MaterialButton

    private val requestCode = 5
    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board_permission)

        mPrimaryBtn = findViewById(R.id.onboard_permission_primary_btn)
        mSecondaryBtn = findViewById(R.id.onboard_permission_secondary_btn)

        mPrimaryBtn.isEnabled = false

        val list = listOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

        permissionHelper = PermissionHelper(this, list, requestCode)

        Handler().postDelayed({
            permissionHelper.checkPermissions()
            mPrimaryBtn.isEnabled = true
        }, 2000)

        mPrimaryBtn.setOnClickListener {

            if (permissionHelper.isPermissionsGranted() == 0) {
                startActivity(Intent(this, OnBoardLanguageActivity::class.java))
            } else {
                permissionHelper.checkPermissions()
            }
        }

        mSecondaryBtn.setOnClickListener {
            finish()
        }
    }

}