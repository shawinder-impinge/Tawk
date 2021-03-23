package com.app.tawk.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.tawk.databinding.ItemPostBinding
import com.app.tawk.model.User
import com.app.tawk.ui.main.viewholder.UserViewHolder

/**
 * Adapter class [RecyclerView.Adapter] for [RecyclerView] which binds [Users] along with [UserViewHolder]
 * @param onItemClicked which will receive callback when item is clicked.
 */
class UserListAdapter(
    private val onItemClicked: (User, ImageView) -> Unit) : PagedListAdapter<User, UserViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserViewHolder(
        ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int): Unit =
        getItem(position)?.let {
            holder.bind(it,position, onItemClicked)
        }!!



    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
}
