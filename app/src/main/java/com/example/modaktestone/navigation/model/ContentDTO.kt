package com.example.modaktestone.navigation.model

data class ContentDTO(
    var uid: String? = null,
    var userName: String? = null,
    var region : String? = null,
    var contentCategory : String? = null,
    var title: String? = null,
    var explain: String? = null,
    var imageUrl : String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap(),
    var commentCount: Int = 0,
    var postCount: Int = 0,
    var anonymity : Map<String, Boolean> = HashMap()
) {
    data class Comment(
        var uid: String? = null,
        var userName: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null,
        var favoriteCount: Int = 0,
        var favorites: Map<String, Boolean> = HashMap()
    )
}
