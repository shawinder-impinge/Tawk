package com.app.tawk.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.tawk.model.User.Companion.TABLE_NAME

/**
 * Data class for Database entity and Serialization.
 */
@Entity(tableName = TABLE_NAME)
data class User(

    @PrimaryKey
    var id: Int? = 0,
    var login: String? = null,
    var node_id: String? = null,
    var gravatar_id: String? = null,
    var avatar_url: String? = null,
    var url: String? = null,
    var html_url: String? = null,
    var followers_url: String? = null,
    var following_url: String?=null,
    var gists_url: String? = null,
    var starred_url: String? = null,
    var subscriptions_url: String? = null,
    var organizations_url: String? = null,
    var repos_url: String? = null,
    var events_url: String? = null,
    var received_events_url: String? = null,
    var type: String? = null,
    var site_admin: Boolean? = false,
    var name: String? = null,
    var company: String? = null,
    var blog: String? = null,
    var location: String? = null,
    var followers: Int?=0,
    var following: Int?=0,
    var note: String?=null

) {
    companion object {
        const val TABLE_NAME = "users_list"
    }
}
