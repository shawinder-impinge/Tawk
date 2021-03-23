package com.app.tawk.data.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.app.tawk.data.local.dao.UsersDao
import com.app.tawk.data.remote.api.ApiInterfaceService
import com.app.tawk.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import retrofit2.Response
import javax.inject.Inject

interface UserRepository {
    fun getAllUsers(pageSize: Int): Flow<Resource<List<User>>>
    fun getSearchUserList(name: String): Flow<Resource<List<User>>>
    fun getUserById(userId: Int): Flow<User>
    fun getUserDetailByName(name: String, userId: Int): Flow<Resource<User>>
    fun updateUserDetail(userId: Int, notes: String)
}

/**
 * Singleton repository for fetching data from remote and storing it in database
 * for offline capability. This is Single source of data.
 */
@ExperimentalCoroutinesApi
class DefaultPostRepository @Inject constructor(
    private val usersDao: UsersDao,
    private val apiInterfaceService: ApiInterfaceService
) : UserRepository {

    /**
     * Fetched the users from network and stored it in database. At the end, data from persistence
     * storage is fetched and emitted.
     */
    override fun getAllUsers(pageSize: Int): Flow<Resource<List<User>>> {
        return object : NetworkBoundRepository<List<User>, List<User>>() {

            override suspend fun saveRemoteData(response: List<User>) = usersDao.addPosts(response)

            override fun fetchFromLocal(): Flow<List<User>> = usersDao.getAllUsers()

            override suspend fun fetchFromRemote(): Response<List<User>> = apiInterfaceService.getUsers(pageSize)
        }.asFlow()
    }

    override fun getSearchUserList(name: String): Flow<Resource<List<User>>> {
        return object : NetworkBoundRepository<List<User>, List<User>>() {


            override fun fetchFromLocal(): Flow<List<User>> = usersDao.getFilterUsers(name)
            override suspend fun fetchFromRemote(): Response<List<User>> {
                TODO("Not yet implemented")
            }

            override suspend fun saveRemoteData(response: List<User>) {
                TODO("Not yet implemented")
            }

        }.asFlow()
    }


    /**
     * Retrieves a user with specified [userId].
     * @param userId Unique id of a [User].
     * @return [user] data fetched from the database.
     */
    @MainThread
    override fun getUserById(userId: Int): Flow<User> = usersDao.getUserById(userId).distinctUntilChanged()
    /***
     * Get user detail
     */
    override fun getUserDetailByName(name: String, userId: Int):  Flow<Resource<User>> {
        return object : UserNetworkBoundRepository<User, User>() {
            override suspend fun fetchFromRemote(): Response<User> = apiInterfaceService.getUserDetail(name)
            override suspend fun saveRemoteData(response: User) {
                usersDao.updateUser(response)
            }

            override fun fetchFromLocal(): Flow<User> {
                return usersDao.getUserById(userId)
            }
        }.asFlow()
    }

    /***
     * Update notes according to user
     */
    @WorkerThread
    override fun updateUserDetail(userId: Int, notes: String) {

            usersDao.updateUserDetail(userId,notes)
    }

}
