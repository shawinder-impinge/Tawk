package com.app.tawk.data.local.dao

import androidx.room.*
import com.app.tawk.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO)
 */
@Dao
interface UsersDao {

    /**
     * Inserts [users] into the [User.TABLE_NAME] table.
     * Duplicate values are replaced in the table.
     * @param users Posts
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPosts(users: List<User>)

    /**
     * Deletes all the users from the [User.TABLE_NAME] table.
     */
    @Query("DELETE FROM ${User.TABLE_NAME}")
    suspend fun deleteAllUsers()

    /**
     * Fetches the user from the [User.TABLE_NAME] table whose id is [userId].
     * @param userId Unique ID of [User]
     * @return [Flow] of [User] from database table.
     */
    @Query("SELECT * FROM ${User.TABLE_NAME} WHERE ID = :userId")
    fun getUserById(userId: Int): Flow<User>

    /**
     * Fetches all the users from the [User.TABLE_NAME] table.
     * @return [Flow]
     */
    @Query("SELECT * FROM ${User.TABLE_NAME}")
    fun getAllUsers(): Flow<List<User>>
    /**
     *  Fetch list of users according to keyword
     */
    @Query("SELECT * FROM ${User.TABLE_NAME} WHERE login  LIKE  :name || '%' OR note LIKE '%' || :name || '%'")
    fun getFilterUsers(name: String): Flow<List<User>>

    /**
     * Update detail of particular user
     */
    @Query("UPDATE ${User.TABLE_NAME} SET note=:notes WHERE ID = :userId")
    fun updateUserDetail(userId: Int, notes:String)

    /***
     * Update user detail from remote server response
     */
    @Update
    fun updateUser(user:User)

}
