package com.haeseong5.android.githubwithrx.src.kotlin.ui.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubRepo
import java.util.*

class SearchAdapter :
    RecyclerView.Adapter<SearchAdapter.RepositoryHolder>() {
    private var items: MutableList<GithubRepo> =
        ArrayList()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepositoryHolder {
        return RepositoryHolder(
            parent
        )
    }

    override fun onBindViewHolder(
        holder: RepositoryHolder,
        position: Int
    ) {
        val repo = items[position]
        Glide.with(holder.itemView.context)
            .load(repo.owner.avatarUrl)
            .into(holder.ivProfile)
        holder.tvName.text = repo.fullName
        holder.tvLanguage.text =
            if (TextUtils.isEmpty(repo.language)) holder.itemView.context.getText(
                R.string.no_language_specified
            ) else repo.language
        holder.itemView.setOnClickListener {
            if (null != listener) {
                listener!!.onItemClick(repo)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: MutableList<GithubRepo>) {
        //인자로 받은 리스트의 형태를 어댑터 내부에서 사용하는
        //리스트 형태로 변환
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun clearItems() {
        items.clear()
    }

    class RepositoryHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false)
    ) {
        var ivProfile: ImageView
        var tvName: TextView
        var tvLanguage: TextView

        init {
            ivProfile =
                itemView.findViewById(R.id.ivItemRepositoryProfile)
            tvName = itemView.findViewById(R.id.tvItemRepositoryName)
            tvLanguage = itemView.findViewById(R.id.tvItemRepositoryLanguage)
        }
    }

    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo)
    }
}