package `in`.hexcommand.asktoagri.helper

import android.content.Context
import android.os.Build
import java.util.*

class AppHelper(private val context: Context) {
    fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}