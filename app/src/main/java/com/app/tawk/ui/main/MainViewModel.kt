package com.app.tawk.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tawk.data.repository.UserRepository
import com.app.tawk.model.User
import com.app.tawk.model.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [MainActivity]
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {

    private val _userLiveData = MutableLiveData<State<List<User>>>()

    val userLiveData: LiveData<State<List<User>>> = _userLiveData

    fun getUsers(pageSize: Int) {
        viewModelScope.launch {
            userRepository.getAllUsers(pageSize)
                .onStart { _userLiveData.value = State.loading() }
                .map { resource -> State.fromResource(resource) }
                .collect { state -> _userLiveData.value = state }
        }
    }
    fun getSearchUserList(name: String) {
        viewModelScope.launch {
            userRepository.getSearchUserList(name)
                    .onStart { _userLiveData.value = State.loading() }
                    .map { resource -> State.fromResource(resource) }
                    .collect { state -> _userLiveData.value = state }
        }
    }
}
