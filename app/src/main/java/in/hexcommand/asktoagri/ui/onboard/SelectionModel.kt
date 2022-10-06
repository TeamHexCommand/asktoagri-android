package `in`.hexcommand.asktoagri.ui.onboard

import com.google.gson.JsonObject

class SelectionModel {
    private var id: Int = 0
    private var category: String = ""
    private var title: String = ""
    private var image: String = ""
    private var selected: Boolean = false

    constructor()

    constructor(
        id: Int,
        category: String,
        title: String,
        image: String,
        selected: Boolean
    ) {
        this.setId(id)
        this.setCategory(category)
        this.setTitle(title)
        this.setImage(image)
        this.setSelected(selected)
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getId(): Int {
        return this.id
    }

    fun setCategory(category: String) {
        this.category = category
    }

    fun getCategory(): String {
        return this.category
    }

    fun setTitle(title: String) {
        this.title
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

    fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    fun getSelected(): Boolean {
        return this.selected
    }
}