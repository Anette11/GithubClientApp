package com.example.githubclientapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.githubclientapp.models.UserSingleRepository
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded
import com.example.githubclientapp.repository.ClientRepository
import kotlinx.coroutines.launch

class ClientViewModel(
    application: Application,
    private val clientRepository: ClientRepository
) : AndroidViewModel(application) {
    private val _userAllRepositories = MutableLiveData<List<UserSingleRepository>>()
    val userAllRepositories: LiveData<List<UserSingleRepository>>
        get() = _userAllRepositories

    private val _progressBarSearchFragmentVisibility = MutableLiveData<Int>()
    val progressBarSearchFragmentVisibility: LiveData<Int>
        get() = _progressBarSearchFragmentVisibility

    val progressBarWebViewFragmentVisibility = MutableLiveData<Int>()
    val progressBarDownloadsFragmentVisibility = MutableLiveData<Int>()
    val repositoryToShowInWebView = MutableLiveData<UserSingleRepository>()
    val repositoryToDownload = MutableLiveData<UserSingleRepository>()
    val isNetworkAvailableToUse = MutableLiveData(false)
    val isShouldShowToastNotFoundRepositories = MutableLiveData(false)
    val isShouldShowToastAboutNetworkIsNotAvailableToUse = MutableLiveData(false)
    private val userNameToSearchRepositories = MutableLiveData<String>()
    private var pageNumber: Int = 1

    val allSavedUserSingleRepositoryDownloaded: LiveData<List<UserSingleRepositoryDownloaded>> =
        clientRepository.getAllSavedUserSingleRepositories()

    fun getUserAllRepositories(userName: String) = viewModelScope.launch {
        if (userNameToSearchRepositories.value != userName) {
            userNameToSearchRepositories.postValue(userName)
            pageNumber = 1
        }
        _progressBarSearchFragmentVisibility.postValue(1)
        if (isNetworkAvailableToUse.value == true) {
            val response = userName.let { clientRepository.getUserAllRepositories(it, pageNumber) }
            if (response.isSuccessful && response.body() != null && response.body()!!
                    .isNotEmpty()
            ) {
                _userAllRepositories.postValue(response.body())
                pageNumber++
            } else {
                if (pageNumber == 1) {
                    isShouldShowToastNotFoundRepositories.postValue(true)
                }
            }
        } else {
            isShouldShowToastAboutNetworkIsNotAvailableToUse.postValue(true)
        }
        _progressBarSearchFragmentVisibility.postValue(0)
    }

    fun saveUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded) =
        viewModelScope.launch {
            progressBarDownloadsFragmentVisibility.postValue(1)
            clientRepository.saveUserSingleRepository(userSingleRepositoryDownloaded)
            progressBarDownloadsFragmentVisibility.postValue(0)
        }

    fun deleteUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded) =
        viewModelScope.launch {
            progressBarDownloadsFragmentVisibility.postValue(1)
            clientRepository.deleteUserSingleRepository(userSingleRepositoryDownloaded)
            progressBarDownloadsFragmentVisibility.postValue(0)
        }
}