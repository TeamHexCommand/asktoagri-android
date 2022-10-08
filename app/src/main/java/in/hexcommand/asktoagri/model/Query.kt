package `in`.hexcommand.asktoagri.model

class Query {
    private var id: Int = 0
    private var userId: Int = 0
    private var title: String = ""
    private var content: String = ""
    private var region: String = ""
    private var category: String = ""
    private var crops: String = ""

    constructor()

    constructor(
        id: Int,
        userId: Int,
        title: String,
        content: String,
        category: String,
        crops: String,
        region: String
    ) {
        this.setId(id)
        this.setUserId(userId)
        this.setTitle(title)
        this.setContent(content)
        this.setCategory(category)
        this.setCrops(crops)
        this.setRegion(region)
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getId(): Int {
        return this.id
    }

    fun setUserId(id: Int) {
        this.userId = id
    }

    fun getUserId(): Int {
        return this.userId
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getTitle(): String {
        return this.title
    }

    fun setContent(title: String) {
        this.content = title
    }

    fun getContent(): String {
        return this.content
    }

    fun setRegion(title: String) {
        this.region = title
    }

    fun getRegion(): String {
        return this.region
    }

    fun setCategory(title: String) {
        this.category = title
    }

    fun getCategory(): String {
        return this.category
    }

    fun setCrops(title: String) {
        this.crops = title
    }

    fun getCrops(): String {
        return this.crops
    }
}