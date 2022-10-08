package `in`.hexcommand.asktoagri.helper

import `in`.hexcommand.asktoagri.R
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class WebViewHelper : AppCompatActivity() {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var clipData: ClipData

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    private lateinit var mWebView: WebView
    private lateinit var mWebUrl: String
    private lateinit var mWebViewClient: WebViewClient
    private lateinit var topAppBar: MaterialToolbar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_helper)

        this.mWebViewClient = WebViewClient()
        this.mWebView = findViewById(R.id.web_view_holder)
        topAppBar = findViewById(R.id.webTopAppBar)
        this.mWebUrl = intent.getStringExtra("weburl").toString()
        this.mWebView.webViewClient = this.mWebViewClient

        val setting = this.mWebView.settings
        setting.javaScriptEnabled = true
        setting.useWideViewPort = true
        mWebView.requestFocus(View.FOCUS_DOWN)
        setting.domStorageEnabled = true
        setting.databaseEnabled = true
        setting.allowUniversalAccessFromFileURLs = true
        setting.javaScriptCanOpenWindowsAutomatically = true
        setting.allowUniversalAccessFromFileURLs = true
        setting.allowContentAccess = true
        setting.setSupportZoom(true)

        if (intent.hasExtra("title")) {
            this.mWebView.loadUrl(mWebUrl)
            topAppBar.title = intent.getStringExtra("title")
        }
        this.mWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = filePathCallback

                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*" // set MIME type to filter

                startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILE_REQUEST_CODE
                )
                return true
            }
        }

        mWebView.webViewClient = object : WebViewClient() {

           /* override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request!!.url.toString()

                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") || url.startsWith(
                        "https://craftyapp.in/"
                    )
                ) {
                    val i = Intent()
                    i.action = Intent.ACTION_VIEW
                    i.data = Uri.parse(url)
                    startActivity(i)
                    finish()
                    return false
                } else {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }*/

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }
        }

        topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == FILE_REQUEST_CODE) {
            if (null == mUploadMessage || intent == null || resultCode != RESULT_OK) {
                return
            }
            var result: Array<Uri>? = null
            val dataString = intent.dataString
            if (dataString != null) {
                result = arrayOf(Uri.parse(dataString))
            }
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        }
    }

    fun doCopy(lable: String, txt: String) {
        this.clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        this.clipData = ClipData.newPlainText(lable, txt)
        this.clipboardManager.setPrimaryClip(clipData)
    }

    companion object {
        const val FILE_REQUEST_CODE = 1603
    }
}