package `in`.hexcommand.asktoagri.ui.login

//import `in`.hexcommand.asktoagri.model.AddressModel
import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.ConfigData
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.AddressModel
import `in`.hexcommand.asktoagri.model.User
import `in`.hexcommand.asktoagri.ui.expert.ExpertActivity
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var mSendOtpBtn: MaterialButton
    private lateinit var mGuestLoginBtn: MaterialButton
    private lateinit var mMobileInput: TextInputEditText
    private lateinit var mRegionInput: TextInputEditText
    private lateinit var mTitle: MaterialTextView

    private lateinit var ls: LocalStorage
    private lateinit var ah: AppHelper

    private lateinit var auth: FirebaseAuth

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var isExpert: Boolean = false

    private lateinit var spinnerDistricts: Spinner

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spinnerDistricts = findViewById(R.id.spinnerDistrictsLogin)
        auth = Firebase.auth

        mSendOtpBtn = findViewById(R.id.send_otp_btn)
        mGuestLoginBtn = findViewById(R.id.guest_btn)
        mMobileInput = findViewById(R.id.mobile_input)
        mRegionInput = findViewById(R.id.region_input)
        mTitle = findViewById(R.id.login_title)

        ls = LocalStorage(this)
        ah = AppHelper(this)

        try {
            val addressModel: AddressModel = ah.getLocationFromCode(
                ls.getValueString("latitude").toDouble(),
                ls.getValueString("longitude").toDouble()
            )

            Log.e(
                "Login",
                "${addressModel.getPincode()} village: ${addressModel.getVillage()} Dis=${addressModel.getDistrict()} City: ${addressModel.getCity()}"
            )
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Failed to get location")
        }

        val adapterDis = ArrayAdapter.createFromResource(
            this,
            R.array.districts_list,
            android.R.layout.simple_spinner_item
        )
        adapterDis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistricts.adapter = adapterDis

        if (intent.hasExtra("user")) {
            ls.save("user", intent.getStringExtra("user").toString())
            if (intent.getStringExtra("user") == "farmer") {
                mTitle.text = "Farmer Login"
            } else {
                isExpert = true
                mTitle.text = "Expert Login"
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
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

//            if (mRegionInput.text.isNullOrEmpty()) {
//                mRegionInput.error = "Enter region"
//            } else {
//
//            }

            ls.save("region", spinnerDistricts.selectedItem.toString())
            sendOtp("+91${mMobileInput.text}")
        }

        mGuestLoginBtn.setOnClickListener {
            ls.save("is_login", true)
            ls.save("is_guest_login", true)
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
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
        signInWithPhoneAuthCredential(credential)
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
        ls.save("show_onboard", false)
        ls.save("is_guest_login", false)
        ls.save("user_token", user?.getIdToken(false).toString())

        val u = User(
            firebaseID = user?.uid.toString(),
            defaultFcm = ls.getValueString("userFcm"),
            mobile = user?.phoneNumber.toString().replace("+91", ""),
            longitude = ls.getValueString("longitude"),
            latitude = ls.getValueString("latitude")
        )

        GlobalScope.launch(Dispatchers.IO) {

            val checkUser = async { ApiHelper(this@LoginActivity).checkUser(u) }.await()
            val userData = JSONObject(checkUser)

            when {
                userData.getInt("code") == 400 -> {
                    ls.save("is_new_user", true)
                    val res = async { ApiHelper(this@LoginActivity).addUser(u) }.await()
                    val data = JSONObject(res)

                    if (data.getInt("code") == 200) {
                        Log.e(TAG, data.getJSONObject("result").getJSONObject("data").toString())
                        ls.save(
                            "current_user",
                            data.getJSONObject("result").getJSONObject("data").toString()
                        )
                    } else {
                        Log.e("LoginActivity", "LoginFailed")
                    }
                }
                else -> {
                    ls.save("is_new_user", false)
                    Log.e(
                        TAG, userData.getJSONObject("result").getJSONArray("data").getJSONObject(0)
                            .toString()
                    )
                    ls.save(
                        "current_user",
                        userData.getJSONObject("result").getJSONArray("data").getJSONObject(0)
                            .toString()
                    )
                }
            }

            finishLogin()
        }
    }

    private fun finishLogin() {

        try {
            val userModel = Gson().fromJson(
                ls.getValueString("current_user"),
                User::class.java
            )

            ls.save("user_id", userModel.id)
            ls.save("user_mobile", userModel.mobile)
            ls.save("user_firebaseId", userModel.firebaseID)
            ls.save("user_fcm", userModel.defaultFcm)

            if (ls.getValueBoolean("is_new_user")) {
                val selectedCrops = JSONArray(ls.getValueString("selected_crops"))

                (0 until selectedCrops.length()).forEach { i ->
                    GlobalScope.launch(Dispatchers.IO) {
                        async {
                            ApiHelper(this@LoginActivity).addConfig(
                                ConfigData(
                                    user = userModel.id,
                                    name = "selectedCrops",
                                    value = selectedCrops.getString(i)
                                )
                            )
                        }
                    }
                }

            }

            if (userModel.isBanned == 1) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.msg_ban),
                    Toast.LENGTH_SHORT
                ).show()
                ah.logoutUser()
                startActivity(
                    Intent(this@LoginActivity, LoginSelectionActivity::class.java).setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
            } else {
                if (userModel.isExpert == 1) {
                    ls.save("user", "expert")
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            ExpertActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                } else {
                    ls.save("user", "farmer")
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            MainActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            }
        } catch (e: NullPointerException) {
            ah.logoutUser()
            startActivity(
                Intent(this@LoginActivity, LoginSelectionActivity::class.java).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
        }
    }

    companion object {
        const val TAG = "LoginActivity"
    }
}