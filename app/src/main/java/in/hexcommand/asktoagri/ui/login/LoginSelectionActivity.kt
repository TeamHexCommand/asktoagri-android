package `in`.hexcommand.asktoagri.ui.login

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginSelectionActivity : AppCompatActivity() {

    private lateinit var mFarmerBtn: MaterialButton
    private lateinit var mExpertBtn: MaterialButton

    private lateinit var ls: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_selection)

        mFarmerBtn = findViewById(R.id.farmer_btn)
        mExpertBtn = findViewById(R.id.expert_btn)

        ls = LocalStorage(this)

        mFarmerBtn.setOnClickListener {
            ls.save("user", "farmer")
            startActivity(Intent(this, LoginActivity::class.java).putExtra(
                "user",
                "farmer"
            ))
        }

        mExpertBtn.setOnClickListener {
            ls.save("user", "expert")
            startActivity(Intent(this, LoginActivity::class.java).putExtra(
                "user",
                "expert"
            ))
        }

    }
}