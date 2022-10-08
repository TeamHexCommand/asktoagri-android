package `in`.hexcommand.asktoagri.ui.user.Trending

class TrendingModel {
    private var id: Int = 0
    private var title: String = ""
    private var image: String = ""
    private var caption: String = ""
    private var tags: String = ""

    constructor()

    constructor(
        id: Int,
        title: String,
        caption: String,
        tags: String,
        image: String
    ) {
        this.setId(id)
        this.setTitle(title)
        this.setCaption(caption)
        this.setTags(tags)
        this.setImage(image)
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getId(): Int {
        return this.id
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getTitle(): String {
        return this.title
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getImage(): String {
        return this.image
    }

    fun setCaption(content: String) {
        this.caption = content
    }

    fun getCaption(): String {
        return this.caption
    }

    fun setTags(tags: String) {
        this.tags = tags
    }

    fun getTags(): String {
        return this.tags
    }

}