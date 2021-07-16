package com.example.githubclientapp.fragments

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubclientapp.BuildConfig
import com.example.githubclientapp.R
import com.example.githubclientapp.adapters.ClientAdapterDownload
import com.example.githubclientapp.databinding.FragmentDownloadsBinding
import com.example.githubclientapp.models.UserSingleRepository
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded
import com.example.githubclientapp.ui.ClientFactory
import com.example.githubclientapp.ui.ClientViewModel
import com.example.githubclientapp.util.ClientApplication
import com.example.githubclientapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import java.io.File

class FragmentDownloads : Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientViewModel: ClientViewModel
    private var downloadedId: Long = 0L
    private lateinit var clientAdapterDownload: ClientAdapterDownload
    private lateinit var downloadManager: DownloadManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDownloadsBinding
            .inflate(inflater, container, false)
        val view = binding.root
        setViewModel()
        changeProgressBarVisibility()
        observeRepositoryToDownload()
        updateRecyclerViewList()
        setRecyclerView()
        setSwipeToDelete(view)
        return view
    }

    private fun setRecyclerView() {
        with(binding.recyclerView) {
            val linearLayoutManager = LinearLayoutManager(activity)
            layoutManager = linearLayoutManager
            clientAdapterDownload = ClientAdapterDownload()
            adapter = clientAdapterDownload
            this.adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        setIntentOpenZip()
    }

    private fun setIntentOpenZip() {
        clientAdapterDownload.onItemClickOpenZip = {
//             deprecated, but through this method unarchive zip intent opens file successfully
//             other solutions only showed Toast message from phone: "Failed to extract file."
            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + File.separator + it.fileName

            val file = File(path)
            val map = MimeTypeMap.getSingleton()
            val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
            var type = map.getMimeTypeFromExtension(ext)
            if (type == null) type = getString(R.string.any_type)
            val intent = Intent()

            val uri = FileProvider.getUriForFile(
                requireActivity(),
                BuildConfig.APPLICATION_ID + getString(R.string.provider),
                File(path)
            )

            intent.action = Intent.ACTION_VIEW
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, type)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val intentChooser = Intent.createChooser(intent, getString(R.string.open_with))
            startActivity(intentChooser)
        }
    }

    private fun updateRecyclerViewList() {
        clientViewModel.allSavedUserSingleRepositoryDownloaded.observe(
            viewLifecycleOwner,
            { listOfUserSingleRepositoryDownloaded ->
                if (listOfUserSingleRepositoryDownloaded != null) {
                    clientAdapterDownload.getAsyncListDiffer()
                        .submitList(listOfUserSingleRepositoryDownloaded)
                }
            })
    }

    private fun getUrl(
        userSingleRepository: UserSingleRepository
    ): String = StringBuilder()
        .append(Constants.URL)
        .append(userSingleRepository.full_name)
        .append(Constants.ARCHIVE_MASTER_ZIP)
        .toString()

    private fun getFileName(
        userSingleRepository: UserSingleRepository
    ): String = StringBuilder()
        .append(userSingleRepository.owner.login)
        .append(getString(R.string.dash))
        .append(userSingleRepository.name)
        .append(getString(R.string.dash))
        .append(Constants.MASTER_ZIP)
        .toString()

    private fun makeDownloadManagerRequest(
        url: String,
        fileName: String
    ): DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        .setTitle(fileName)
        .setDescription(getString(R.string.app_name))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    private fun downloadRepository(userSingleRepository: UserSingleRepository) {
        clientViewModel.progressBarDownloadsFragmentVisibility.postValue(1)
        val url = getUrl(userSingleRepository)
        val fileName = getFileName(userSingleRepository)
        val downloadManagerRequest = makeDownloadManagerRequest(url, fileName)
        downloadManager = requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadedId = downloadManager.enqueue(downloadManagerRequest)
        createBroadcastReceiver(fileName)
    }

    private fun createBroadcastReceiver(fileName: String) {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val defaultValue = -1L
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, defaultValue)
                if (id == downloadedId) {
                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        )
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            clientViewModel.repositoryToDownload.postValue(null)

                            val path = requireActivity()
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath +
                                    File.separator + fileName

                            val userSingleRepositoryDownloaded =
                                UserSingleRepositoryDownloaded(downloadedId, fileName, path)

                            clientViewModel.saveUserSingleRepository(userSingleRepositoryDownloaded)
                            clientViewModel.allSavedUserSingleRepositoryDownloaded

                            showToastMessage(getString(R.string.downloaded_and_saved))
                        } else {
                            showToastMessage(getString(R.string.downloading_is_not_successful))
                        }
                    }
                }
                clientViewModel.progressBarDownloadsFragmentVisibility.postValue(0)
            }
        }
        registerBroadcastReceiver(broadcastReceiver)
    }

    private fun registerBroadcastReceiver(broadcastReceiver: BroadcastReceiver) {
        requireActivity().registerReceiver(
            broadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setViewModel() {
        clientViewModel = ViewModelProvider(
            requireActivity(), ClientFactory(
                requireActivity().application,
                (requireActivity().application as ClientApplication).clientRepository
            )
        ).get(ClientViewModel::class.java)
    }

    private fun observeRepositoryToDownload() {
        clientViewModel.repositoryToDownload.observe(
            viewLifecycleOwner,
            { userSingleRepository ->
                if (userSingleRepository != null) {
                    if (clientViewModel.isNetworkAvailableToUse.value == true) {
                        downloadRepository(userSingleRepository)
                        clientViewModel.repositoryToDownload.postValue(null)
                    } else {
                        clientViewModel.isShouldShowToastAboutNetworkIsNotAvailableToUse.postValue(
                            true
                        )
                    }
                }
            })
    }

    private fun changeProgressBarVisibility() {
        clientViewModel.progressBarDownloadsFragmentVisibility.observe(viewLifecycleOwner, {
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

    private fun setSwipeToDelete(view: View) {
        val itemTouchHelperSimpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.absoluteAdapterPosition
                val userSingleRepositoryDownloaded =
                    clientAdapterDownload.getAsyncListDiffer().currentList[position]
                clientViewModel.deleteUserSingleRepository(userSingleRepositoryDownloaded)
                showSnackBar(view, userSingleRepositoryDownloaded)
            }
        }

        ItemTouchHelper(itemTouchHelperSimpleCallback)
            .attachToRecyclerView(_binding?.recyclerView)
    }

    private fun showSnackBar(
        view: View,
        userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded
    ) {
        Snackbar.make(view, getString(R.string.deleted), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                clientViewModel.saveUserSingleRepository(userSingleRepositoryDownloaded)
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}