package com.example.githubclientapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.githubclientapp.databinding.OneItemSearchBinding
import com.example.githubclientapp.models.UserSingleRepository

class ClientAdapterSearch : RecyclerView.Adapter<ClientAdapterSearch.ClientViewHolder>() {
    private var list: List<UserSingleRepository> = ArrayList()

    inner class ClientViewHolder(private val oneItemSearchBinding: OneItemSearchBinding) :
        RecyclerView.ViewHolder(oneItemSearchBinding.root) {
        fun bind(userSingleRepository: UserSingleRepository) = with(oneItemSearchBinding) {
            textViewRepositoryName.text = userSingleRepository.full_name

            imageButtonDownload.setOnClickListener {
                onItemClickDownload?.invoke(
                    userSingleRepository
                )
            }

            imageButtonOpen.setOnClickListener {
                onItemClickShowInWebView?.invoke(
                    userSingleRepository
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val oneItemSearchBinding: OneItemSearchBinding = OneItemSearchBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(oneItemSearchBinding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val userSingleRepository = list[position]
        holder.bind(userSingleRepository)
    }

    override fun getItemCount(): Int = list.size

    var onItemClickShowInWebView: ((UserSingleRepository) -> Unit)? = null

    var onItemClickDownload: ((UserSingleRepository) -> Unit)? = null

    fun updateList(newList: List<UserSingleRepository>) {
        this.list = newList
        notifyDataSetChanged()
    }
}