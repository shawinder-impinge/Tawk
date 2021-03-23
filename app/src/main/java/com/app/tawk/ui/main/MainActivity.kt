package com.app.tawk.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tawk.R
import com.app.tawk.databinding.ActivityMainBinding
import com.app.tawk.model.User
import com.app.tawk.model.State
import com.app.tawk.ui.base.BaseActivity
import com.app.tawk.ui.details.UserDetailsActivity
import com.app.tawk.ui.main.adapter.UserListAdapter
import com.app.tawk.utils.*
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.Executor

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    var linearLayoutManager: LinearLayoutManager?=null

    override val mViewModel: MainViewModel by viewModels()

    private val mAdapter = UserListAdapter(this::onItemClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) // Set AppTheme before setting content view.

        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initView()
    }

    override fun onStart() {
        super.onStart()
        observeUsers()
        handleNetworkChanges()
    }

    fun initView() {
        mViewBinding.run {
            userRecyclerView.adapter = mAdapter

             linearLayoutManager = LinearLayoutManager(this@MainActivity)
            mViewBinding.userRecyclerView.layoutManager = linearLayoutManager

            swipeRefreshLayout.setOnRefreshListener {
                Constants.Size=0
                getUsers(Constants.Size)
            }

        }
        setRecyclerViewScrollListener()
        // If Current State isn't Success then reload users.
        mViewModel.userLiveData.value?.let { currentState ->
            if (!currentState.isSuccessful()) {
                getUsers(Constants.Size)
            }
        }
        mViewBinding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if (s!!.trim().isNotEmpty()) {
                    getSearchUserList(s.toString())

                }else{
                    getUsers(Constants.Size)
                }
            }

        })
    }

    private fun observeUsers() {
        mViewModel.userLiveData.observe(this) { state ->
            when (state) {
                is State.Loading -> showLoading(true)
                is State.Success -> {
                    if (state.data.isNotEmpty()) {
                        mViewBinding.loadStateProgress.visibility= View.GONE
                        mAdapter.submitList(mutableListToPagingList(state.data.toMutableList()))
                        showLoading(false)
                    }
                }
                is State.Error -> {
                    showToast(state.message)
                    showLoading(false)
                    mViewBinding.loadStateProgress.visibility= View.GONE
                }
            }
        }
    }

    /***
     * get List of users
     */
    private fun getUsers(pageSize:Int) = mViewModel.getUsers(pageSize)

    /***
     * Filter user according to username or note
     */
    private fun getSearchUserList(name: String) = mViewModel.getSearchUserList(name)

    private fun showLoading(isLoading: Boolean) {
        mViewBinding.swipeRefreshLayout.isRefreshing = isLoading
    }

    /**
     * Observe network changes i.e. Internet Connectivity
     */
    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this) { isConnected ->
            if (!isConnected) {
                mViewBinding.textViewNetworkStatus.text =
                    getString(R.string.text_no_connectivity)
                mViewBinding.networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                    getUsers(Constants.Size)
                }
            } else {
                if (mViewModel.userLiveData.value is State.Error || mAdapter.itemCount == 0) {
                    getUsers(Constants.Size)
                }
                mViewBinding.textViewNetworkStatus.text = getString(R.string.text_connectivity)
                mViewBinding.networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                // Get new mode.
                val mode =
                    if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_NO
                    ) {
                        AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    }

                // Change UI Mode
                AppCompatDelegate.setDefaultNightMode(mode)
                true
            }

            else -> true
        }
    }


    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    private fun onItemClicked(user: User, imageView: ImageView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            imageView,
            imageView.transitionName
        )
        val userId = user.id ?: run {
            showToast("Unable to launch details")
            return
        }
        val intent = UserDetailsActivity.getStartIntent(this, userId,user.login.toString())
        startActivity(intent, options.toBundle())
    }

    companion object {
        const val ANIMATION_DURATION = 1000L
    }
    fun mutableListToPagingList(items: List<User>): PagedList<User> {
        val config = PagedList.Config.Builder()
                .setPageSize(items.size)
                .setEnablePlaceholders(true)
                .setInitialLoadSizeHint(items.size)
                .build()

        val pagedList = PagedList.Builder(ListDataSource(items), config)
                .setNotifyExecutor(UiThreadExecutor())
                .setFetchExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .build()
        return pagedList
    }

    class UiThreadExecutor: Executor {
        private val handler = Handler (Looper.getMainLooper ())
        override fun execute (command: Runnable) {
            handler.post (command)
        }
    }

    class ListDataSource (private val items: List<User>): PageKeyedDataSource<Int, User>() {
        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
            callback.onResult (items, 0, items.size)
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
            Constants.Size= items.get(items.size-1).id!!

        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {

        }
    }
    private fun setRecyclerViewScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                if (totalItemCount == (lastVisibleItemPosition + 1)) {
                    mViewBinding.loadStateProgress.visibility=View.VISIBLE
                    getUsers(Constants.Size)
                }
            }
        }
        mViewBinding.userRecyclerView.addOnScrollListener(scrollListener)
    }
    private var lastVisibleItemPosition: Int = 0
        get() = linearLayoutManager!!.findLastVisibleItemPosition()
}
