package com.example.githubclientapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.githubclientapp.R
import com.example.githubclientapp.databinding.ActivityMainBinding
import com.example.githubclientapp.util.ClientApplication
import com.example.githubclientapp.util.ConnectivityLiveData

class ClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var clientViewModel: ClientViewModel
    private lateinit var connectivityLiveData: ConnectivityLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_GithubClientApp)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setViewModel()
        checkIfNetworkIsAvailableToUse()
        checkIfShouldShowToastAboutNetworkIsNotAvailableToUse()
        checkIfShouldShowToastAboutNotFoundRepositories()
        setNavigation()
    }

    private fun setViewModel() {
        clientViewModel = ViewModelProvider(
            this,
            ClientFactory(application, (application as ClientApplication).clientRepository)
        ).get(ClientViewModel::class.java)
    }

    private fun checkIfNetworkIsAvailableToUse() {
        connectivityLiveData = ConnectivityLiveData(this)

        connectivityLiveData.observe(this, {
            if (it != null) {
                clientViewModel.isNetworkAvailableToUse.postValue(it)
            }
        })
    }

    private fun checkIfShouldShowToastAboutNetworkIsNotAvailableToUse() {
        clientViewModel.isShouldShowToastAboutNetworkIsNotAvailableToUse.observe(this, {
            if (it != null) if (it) {
                clientViewModel.isShouldShowToastAboutNetworkIsNotAvailableToUse.postValue(false)
                showToastMessage(getString(R.string.network_is_not_available))
            }
        })
    }

    private fun checkIfShouldShowToastAboutNotFoundRepositories() {
        clientViewModel.isShouldShowToastNotFoundRepositories.observe(this, {
            if (it != null) if (it) {
                clientViewModel.isShouldShowToastNotFoundRepositories.postValue(false)
                showToastMessage(getString(R.string.repositories_not_found))
            }
        })
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment?

        NavigationUI.setupWithNavController(
            binding.bottomNavigationView,
            navHostFragment!!.navController
        )
    }
}