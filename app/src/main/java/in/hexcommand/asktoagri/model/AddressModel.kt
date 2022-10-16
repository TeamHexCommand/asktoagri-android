package `in`.hexcommand.asktoagri.model

class AddressModel {
    private var city: String = ""
    private var pincode: String = ""
    private var country: String = ""
    private var state: String = ""
    private var district: String = ""
    private var village: String = ""
    private var latitude: String = ""
    private var longitude: String = ""

    constructor()

    constructor(
        pincode: String,
        village: String,
        city: String,
        district: String,
        state: String,
        country: String,
        latitude: String,
        longitude: String
    ) {
        setPincode(pincode)
        setVillage(village)
        setCity(city)
        setDistrict(district)
        setState(state)
        setCountry(country)
        setLatitude(latitude)
        setLongitude(longitude)
    }

    fun setPincode(s: String) {
        this.pincode = s
    }

    fun getPincode(): String {
        return this.pincode
    }

    fun setVillage(s: String) {
        this.village = s
    }

    fun getVillage(): String {
        return this.village
    }

    fun setCity(s: String) {
        this.city = s
    }

    fun getCity(): String {
        return this.city
    }

    fun setDistrict(s: String) {
        this.district = s
    }

    fun getDistrict(): String {
        return this.district
    }

    fun setState(s: String) {
        this.state = s
    }

    fun getState(): String {
        return this.state
    }

    fun setCountry(s: String) {
        this.country = s
    }

    fun getCountry(): String {
        return this.country
    }

    fun setLatitude(s: String) {
        this.latitude = s
    }

    fun getLatitude(): String {
        return this.latitude
    }

    fun setLongitude(s: String) {
        this.longitude = s
    }

    fun getLongitude(): String {
        return this.longitude
    }
}