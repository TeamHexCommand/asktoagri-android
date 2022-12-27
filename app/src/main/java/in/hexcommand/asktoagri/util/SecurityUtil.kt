package `in`.hexcommand.asktoagri.util

import android.content.Context
import java.security.MessageDigest

class SecurityUtil(private val context: Context) {
    fun getSHA256(text: String, salt: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val bytes: ByteArray = md.digest(text.toByteArray())
        return bytes.toString()
    }
}