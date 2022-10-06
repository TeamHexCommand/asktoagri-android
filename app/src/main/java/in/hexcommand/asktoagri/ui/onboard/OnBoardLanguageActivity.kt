package `in`.hexcommand.asktoagri.ui.onboard

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*


class OnBoardLanguageActivity : AppCompatActivity() {

    private lateinit var mPrimaryBtn: MaterialButton
    private lateinit var mSecondaryBtn: MaterialButton
    private lateinit var mChipGroup: ChipGroup

    private lateinit var ls: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board_language)

        mPrimaryBtn = findViewById(R.id.onboard_language_primary_btn)
        mSecondaryBtn = findViewById(R.id.onboard_language_secondary_btn)
        mChipGroup = findViewById(R.id.chipGroupLanguage)

        ls = LocalStorage(this)

        mChipGroup.addChip(this, "en")
        mChipGroup.addChip(this, "gu")
        mChipGroup.addChip(this, "hi")

//        mChipGroup.setOnCheckedChangeListener { group, checkedId ->
//            when (checkedId) {
//                R.id.chipEnglish -> {
//                    Toast.makeText(this, "English", Toast.LENGTH_SHORT).show()
//                    changeSelection(R.id.chipEnglish)
//                    setAppLocale("en")
//                }
//                R.id.chipGujarati -> {
//                    Toast.makeText(this, "Gujarati", Toast.LENGTH_SHORT).show()
//                    changeSelection(R.id.chipGujarati)
//                    setAppLocale("gu")
//                }
//                R.id.chipHindi -> {
//                    Toast.makeText(this, "Hindi", Toast.LENGTH_SHORT).show()
//                    changeSelection(R.id.chipHindi)
//                    setAppLocale("hi")
//                }
//            }
//        }

        mPrimaryBtn.setOnClickListener {
            startActivity(Intent(this, OnBoardSelectionActivity::class.java))
        }

        mSecondaryBtn.setOnClickListener {
            startActivity(Intent(this, OnBoardPermissionActivity::class.java))
        }

        getLastKnownLocation(this)

    }

    private fun ChipGroup.addChip(context: Context, locale: String) {

        var label = "English"

        when (locale) {
            "en" -> {
                label = "English"
            }
            "gu" -> {
                label = "ગુજરાતી"
            }
            "hi" -> {
                label = "हिन्दी"
            }
        }

        Chip(context).apply {
            id = View.generateViewId()
            text = label
            isClickable = true
            isCheckable = true
            isCheckedIconVisible = false
            isFocusable = true
            addView(this)
        }.setOnClickListener {
            it.isSelected = true
            setAppLocale(this@OnBoardLanguageActivity, locale)
            ls.save("locale", locale)
            Toast.makeText(
                this@OnBoardLanguageActivity,
                R.string.selected_language,
                Toast.LENGTH_SHORT
            ).show()
            recreate()
        }
    }

    private fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    public fun getLastKnownLocation(context: Context) {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)
        var location: Location? = null
        for (i in providers.size - 1 downTo 0) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(Intent(this, OnBoardPermissionActivity::class.java))
                return
            }
            location = locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }
        val gps = DoubleArray(2)
        if (location != null) {
            gps[0] = location.latitude
            gps[1] = location.longitude

            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? =
                geocoder.getFromLocation(location.latitude, location.longitude, 5)

            Log.e("LOC", gps[0].toString())
            Log.e("LOC", gps[1].toString())
            Log.e("LOC", addresses!![1].locale.toString())
        }

    }
}