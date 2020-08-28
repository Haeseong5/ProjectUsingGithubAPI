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
import kotlinx.android.synthetic.main.item_repository.view.*
import java.util.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.RepositoryHolder>() {

    private var items: MutableList<GithubRepo> = mutableListOf()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepositoryHolder = RepositoryHolder(parent)

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {

        items[position].let {repo ->
            with(holder.itemView) {
                Glide.with(context)
                    .load(repo.owner.avatarUrl)
                    .into(ivItemRepositoryProfile)
                tvItemRepositoryName.text = repo.fullName
                tvItemRepositoryLanguage.text =
                    if (TextUtils.isEmpty(repo.language)) context.getText(
                        R.string.no_language_specified
                    ) else repo.language
                setOnClickListener {
                    if (null != listener) {
                        listener!!.onItemClick(repo)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<GithubRepo>) {
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
    )
    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo)
    }
}