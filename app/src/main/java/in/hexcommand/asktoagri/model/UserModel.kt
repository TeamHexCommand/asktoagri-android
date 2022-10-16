package `in`.hexcommand.asktoagri.model

import org.json.JSONObject

class UserModel() {

    private var id: Int = 0
    private var firstName: String = ""
    private var lastName: String = ""
    private var email: String = ""
    private var password: String = ""
    private var firebaseId: String = ""
    private var defaultFcm: String = ""
    private var mobile: String = ""
    private var isAdmin: Boolean = false
    private var isExpert: Boolean = false
    private var isBanned: Boolean = false
    private var longitude: String = ""
    private var latitude: String = ""
    private var defaultLang: Int = 0
    private var city: Int = 0
    private var createdAt: String = ""

    fun setData(j: JSONObject) {
        if (j.has("id")) this.id = j.getInt("id") else this.id = 0
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getId(): Int {
        return this.id
    }

    fun setFirstName(s: String) {
        this.firstName = s
    }

    fun getFirstName(): String {
        return this.firstName
    }

    fun setLastName(s: String) {
        this.lastName = s
    }

    fun getLastName(): String {
        return this.lastName
    }

    fun setEmail(s: String) {
        this.email = s
    }

    fun getEmail(): String {
        return this.email
    }

    fun setPassword(s: String) {
        this.password = s
    }

    fun getPassword(): String {
        return this.password
    }

    fun setFirebaseId(s: String) {
        this.firebaseId = s
    }

    fun getFirebaseId(): String {
        return this.firebaseId
    }

    fun setDefaultFcm(s: String) {
        this.defaultFcm = s
    }

    fun getDefaultFcm(): String {
        return this.defaultFcm
    }

    fun setMobile(s: String) {
        this.mobile = s
    }

    fun getMobile(): String {
        return this.firstName
    }

    fun setIsAdmin(b: Boolean) {
        this.isAdmin = b
    }

    fun getIsAdmin(): Boolean {
        return this.isAdmin
    }

    fun setIsExpert(b: Boolean) {
        this.isExpert = b
    }

    fun getIsExpert(): Boolean {
        return this.isExpert
    }

    fun setIsBanned(b: Boolean) {
        this.isBanned = b
    }

    fun getIsBanned(): Boolean {
        return this.isBanned
    }

    fun setLongitude(s: String) {
        this.longitude = s
    }

    fun getLongitude(): String {
        return this.longitude
    }

    fun setLatitude(s: String) {
        this.latitude = s
    }

    fun getLatitude(): String {
        return this.latitude
    }

    fun setDefaultLang(i: Int) {
        this.defaultLang = i
    }

    fun getDefaultLang(): Int {
        return this.defaultLang
    }

    fun setCreatedAt(s: String) {
        this.createdAt = s
    }

    fun getCreatedAt(): String {
        return this.createdAt
    }

}