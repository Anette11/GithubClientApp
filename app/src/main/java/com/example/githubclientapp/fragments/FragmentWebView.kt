package com.example.githubclientapp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.githubclientapp.databinding.FragmentWebViewBinding
import com.example.githubclientapp.models.UserSingleRepository
import com.example.githubclientapp.ui.ClientFactory
import com.example.githubclientapp.ui.ClientViewModel
import com.example.githubclientapp.util.ClientApplication

class FragmentWebView : Fragment() {
    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientViewModel: ClientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWebViewBinding
            .inflate(inflater, container, false)
        val view = binding.root
        setViewModel()
        setWebView()
        letWebViewGoBackOnBackPressedButton()
        restoreWebViewState(savedInstanceState)
        changeProgressBarVisibility()
        observeRepositoryToShowInWebView()
        return view
    }

    private fun setViewModel() {
        clientViewModel = ViewModelProvider(
            requireActivity(), ClientFactory(
                requireActivity().application,
                (requireActivity().application as ClientApplication).clientRepository
            )
        ).get(ClientViewModel::class.java)
    }

    private fun observeRepositoryToShowInWebView() {
        clientViewModel.repositoryToShowInWebView.observe(
            viewLifecycleOwner,
            { userSingleRepository ->
                if (userSingleRepository != null) {
                    loadWebView(userSingleRepository)
                }
            })
    }

    private fun restoreWebViewState(savedInstanceState: Bundle?) {
        when {
            savedInstanceState != null -> {
                _binding?.webView?.restoreState(savedInstanceState)
                clientViewModel.repositoryToShowInWebView.postValue(null)
            }
            else -> {
                val userSingleRepository = clientViewModel.repositoryToShowInWebView.value
                if (userSingleRepository != null) {
                    loadWebView(userSingleRepository)
                }
            }
        }
    }

    private fun loadWebView(userSingleRepository: UserSingleRepository) {
        userSingleRepository.html_url.let {
            _binding?.webView?.apply {
                loadUrl(it)
            }
        }
    }

    private fun setWebView() {
        _binding?.webView?.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    clientViewModel.progressBarWebViewFragmentVisibility.postValue(1)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    clientViewModel.progressBarWebViewFragmentVisibility.postValue(0)
                }
            }
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.setSupportZoom(true)
        }
    }

    private fun letWebViewGoBackOnBackPressedButton() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (_binding?.webView?.canGoBack() == true) {
                        _binding?.webView?.goBack()
                    } else if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun changeProgressBarVisibility() {
        clientViewModel.progressBarWebViewFragmentVisibility.observe(viewLifecycleOwner, {
            if (it != null) when (it) {
                1 -> {
                    _binding?.progressBar?.visibility = View.VISIBLE
                }
                else -> {
                    _binding?.progressBar?.visibility = View.GONE
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.webView?.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}