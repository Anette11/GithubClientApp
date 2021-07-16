package com.example.githubclientapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.githubclientapp.databinding.OneItemDownloadBinding
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded

class ClientAdapterDownload : RecyclerView.Adapter<ClientAdapterDownload.ClientViewHolder>() {

    inner class ClientViewHolder(private val oneItemDownloadBinding: OneItemDownloadBinding) :
        RecyclerView.ViewHolder(oneItemDownloadBinding.root) {
        fun bind(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded) =
            with(oneItemDownloadBinding) {
                textViewDownloadedRepositoryName.text = userSingleRepositoryDownloaded.fileName

                imageButtonUnarchive.setOnClickListener {
                    onItemClickOpenZip?.invoke(
                        userSingleRepositoryDownloaded
                    )
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val oneItemDownloadBinding: OneItemDownloadBinding = OneItemDownloadBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(oneItemDownloadBinding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val userSingleRepositoryDownloaded = asyncListDiffer.currentList[position]
        holder.bind(userSingleRepositoryDownloaded)
    }

    private val diffUtilItemCallback =
        object : DiffUtil.ItemCallback<UserSingleRepositoryDownloaded>() {
            override fun areItemsTheSame(
                oldItem: UserSingleRepositoryDownloaded,
                newItem: UserSingleRepositoryDownloaded
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: UserSingleRepositoryDownloaded,
                newItem: UserSingleRepositoryDownloaded
            ): Boolean =
                oldItem.downloadedId == newItem.downloadedId
        }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    fun getAsyncListDiffer() = asyncListDiffer

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    var onItemClickOpenZip: ((UserSingleRepositoryDownloaded) -> Unit)? = null
}