package com.example.githubclientapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubclientapp.R
import com.example.githubclientapp.adapters.ClientAdapterSearch
import com.example.githubclientapp.databinding.FragmentSearchBinding
import com.example.githubclientapp.ui.ClientFactory
import com.example.githubclientapp.ui.ClientViewModel
import com.example.githubclientapp.util.ClientApplication

class FragmentSearch : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientViewModel: ClientViewModel
    private lateinit var clientAdapterSearch: ClientAdapterSearch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding
            .inflate(inflater, container, false)
        val view = binding.root
        setViewModel()
        updateRecyclerViewList()
        setImageButtonSearchOnClickListener()
        setRecyclerView()
        changeProgressBarVisibility()
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

    private fun setRecyclerView() {
        with(binding.recyclerView) {
            val linearLayoutManager = LinearLayoutManager(activity)
            layoutManager = linearLayoutManager
            clientAdapterSearch = ClientAdapterSearch()
            adapter = clientAdapterSearch
            this.adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            setNavigationToFragmentDownloads()
            setNavigationToFragmentWebView()
            setOnScrollListenerForRecyclerView()
        }
    }

    private fun setOnScrollListenerForRecyclerView() {
        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    val listOfArticles = clientViewModel.userAllRepositories.value
                    val lastCompletelyVisibleItemPosition =
                        linearLayoutManager?.findLastCompletelyVisibleItemPosition()

                    if (linearLayoutManager != null &&
                        listOfArticles != null &&
                        lastCompletelyVisibleItemPosition == listOfArticles.size - 1
                    ) {
                        loadMore()
                    }
                }
            }
        })
    }

    private fun setNavigationToFragmentDownloads() {
        clientAdapterSearch.onItemClickDownload = { userSingleRepository ->
            if (!checkIfPermissionIsGranted()) {
                showToastMessage()
                makeRequestForPermission()
            } else {
                clientViewModel.repositoryToDownload.postValue(userSingleRepository)
                findNavController().navigate(R.id.action_fragmentSearch_to_fragmentDownloads)
            }
        }
    }

    private fun setNavigationToFragmentWebView() {
        clientAdapterSearch.onItemClickShowInWebView = { userSingleRepository ->
            clientViewModel.repositoryToShowInWebView.postValue(userSingleRepository)
            findNavController().navigate(R.id.action_fragmentSearch_to_fragmentWebView)
        }
    }

    private fun checkIfPermissionIsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
    }

    private fun makeRequestForPermission() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        for (i in permissions.indices) {
            requestPermission(permissions[i], i)
        }
    }

    private fun showToastMessage() {
        Toast.makeText(requireActivity(), getString(R.string.allow_permissions), Toast.LENGTH_LONG)
            .show()
    }

    private fun updateRecyclerViewList() {
        clientViewModel.userAllRepositories.observe(viewLifecycleOwner, { userAllRepositories ->
            if (userAllRepositories != null) {
                clientAdapterSearch.updateList(userAllRepositories)
            }
        })
    }

    private fun changeProgressBarVisibility() {
        clientViewModel.progressBarSearchFragmentVisibility.observe(viewLifecycleOwner, {
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

    private fun loadMore() {
        val userName = _binding?.textInputEditText?.text.toString()
        if (userName.isNotEmpty()) {
            clientViewModel.getUserAllRepositories(userName)
        }
    }

    private fun setImageButtonSearchOnClickListener() {
        _binding?.imageButtonSearch?.setOnClickListener {
            val userName = _binding?.textInputEditText?.text.toString()
            if (userName.isNotEmpty()) {
                clientViewModel.getUserAllRepositories(userName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}