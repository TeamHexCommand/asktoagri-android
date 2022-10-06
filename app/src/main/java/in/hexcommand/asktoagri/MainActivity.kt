package `in`.hexcommand.asktoagri

import `in`.hexcommand.asktoagri.util.shared.Keys
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ls = LocalStorage(this)

        if (ls.getValueBoolean("is_guest_login")) {
            Toast.makeText(this, R.string.welcome_note_guest, Toast.LENGTH_SHORT).show()
        }

//        val apiKey = Keys.apiKey()
//        Log.e("KEY", apiKey)

    }
}