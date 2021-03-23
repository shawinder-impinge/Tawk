package com.app.tawk.ui.details

import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import com.app.tawk.data.repository.UserRepository
import com.app.tawk.model.User
import com.app.tawk.model.State
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel for [UserDetailsActivity]
 */
@ExperimentalCoroutinesApi
class UserDetailsViewModel @AssistedInject constructor(
        private val userRepository: UserRepository,
        @Assisted userId: Int, @Assisted notes:String
) : ViewModel() {

    val user = userRepository.getUserById(userId).asLiveData()

    private val _userDetailLiveData = MutableLiveData<State<User>>()

    val userDetailLiveData: LiveData<State<User>> = _userDetailLiveData

    fun getUserDetail(userName: String,userId: Int) {
        viewModelScope.launch {
            userRepository.getUserDetailByName(userName,userId)
                .onStart { _userDetailLiveData.value = State.loading() }
                .map { resource -> State.fromResource(resource) }
                .collect { state -> _userDetailLiveData.value = state }
        }
    }
    @WorkerThread
    fun updateUserDetail(userId: Int, notes: String) {
    viewModelScope.launch {
        userRepository.updateUserDetail(userId,notes)
    }

    }
    @AssistedFactory
    interface UserDetailsViewModelFactory {
        fun create(userId: Int, notes: String): UserDetailsViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
                assistedFactory: UserDetailsViewModelFactory,
                userId: Int,
                notes: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(userId,notes) as T
            }
        }
    }


}
