package models

import com.google.gson.annotations.SerializedName

data class Comment (
    var id: String,
    var articleId: String,
    @SerializedName("published-at") var publishedAt: String,
    var name: String,
    var avatar: String,
    var body: String
)