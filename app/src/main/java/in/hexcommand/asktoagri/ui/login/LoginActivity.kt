package `in`.hexcommand.asktoagri.ui.login

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var mSendOtpBtn: MaterialButton
    private lateinit var mGuestLoginBtn: MaterialButton
    private lateinit var mMobileInput: TextInputEditText

    private lateinit var ls: LocalStorage

    private lateinit var auth: FirebaseAuth

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        mSendOtpBtn = findViewById(R.id.send_otp_btn)
        mGuestLoginBtn = findViewById(R.id.guest_btn)
        mMobileInput = findViewById(R.id.mobile_input)

        ls = LocalStorage(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
//                Toast.makeText(this@LoginActivity, R.string.otp_init, Toast.LENGTH_SHORT).show()
                openOtpDialog()
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        mMobileInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mSendOtpBtn.isEnabled = s.length == 10
            }
        })

        mSendOtpBtn.setOnClickListener {
            sendOtp("+91${mMobileInput.text}")
        }

        mGuestLoginBtn.setOnClickListener {
            ls.save("is_login", true)
            ls.save("is_guest_login", true)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    updateUI(user)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendOtp(mobile: String) {
        Toast.makeText(this@LoginActivity, R.string.otp_init, Toast.LENGTH_SHORT).show()
        startPhoneNumberVerification(mobile)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "MissingInflatedId")
    private fun openOtpDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null)

        val input = view.findViewById<TextInputEditText>(R.id.otp_input)

        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog
                .Builder(this).setView(view)
                .setTitle(R.string.verify_otp)
                .setMessage(R.string.otp_init)
                .setPositiveButton(R.string.submit) { dialog, which ->
                    if (input.text.isNullOrEmpty()) {
                        input.error = "Enter OTP"
                    } else {
                        verifyPhoneNumberWithCode(storedVerificationId, input.text.toString())
                    }
                }
                .setNegativeButton(R.string.resent_otp) { dialog, _ ->
                    input.text?.clear()
                    resendVerificationCode(mMobileInput.text.toString(), resendToken)
                }
                .setNeutralButton(R.string.change_mobile) { dialog, _ ->
                    mMobileInput.text?.clear()
                    dialog.dismiss()
                }
                .setView(view)

        val dialog = builder.show()
    }

    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
        ls.save("is_login", true)
        ls.save("is_guest_login", false)
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {
        const val TAG = "LoginActivity"
    }
}