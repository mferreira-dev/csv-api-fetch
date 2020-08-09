package models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Article (
    var id: String,
    var title: String,
    @SerializedName("published-at") var publishedAt: String,
    var hero: String,
    var author: String,
    var avatar: String,
    var summary: String,
    var body: String
) : Serializable