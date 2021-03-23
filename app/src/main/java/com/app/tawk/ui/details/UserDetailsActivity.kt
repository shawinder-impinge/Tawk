package com.app.tawk.ui.details

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import coil.load
import com.app.tawk.R
import com.app.tawk.databinding.ActivityPostDetailsBinding
import com.app.tawk.model.State
import com.app.tawk.ui.base.BaseActivity
import com.app.tawk.ui.main.MainActivity
import com.app.tawk.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UserDetailsActivity : BaseActivity<UserDetailsViewModel, ActivityPostDetailsBinding>() {
    var notes=""
    var userId:Int?=null
    var userName=""
    @Inject
    lateinit var viewModelFactory: UserDetailsViewModel.UserDetailsViewModelFactory

    override val mViewModel: UserDetailsViewModel by viewModels {
         userId = intent.extras?.getInt(KEY_USER_ID)
                ?: throw IllegalArgumentException("userId must be non-null")
        userName = intent.extras?.getString(KEY_USER_NAME)
                ?: throw IllegalArgumentException("userName must be non-null")
        UserDetailsViewModel.provideFactory(viewModelFactory, userId!!,notes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        setSupportActionBar(mViewBinding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mViewBinding.btnSave.setOnClickListener {
            notes= mViewBinding.etNotes.text.toString()
            if (notes.isNotEmpty()) {
                mViewModel.viewModelScope.launch(Dispatchers.IO) {
                    mViewModel.updateUserDetail(userId!!, notes)
                }
            }else{
                Toast.makeText(this,getString(R.string.please_enter_note),Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onStart() {
        super.onStart()
        initPost()
        observeUserDetail()
        handleNetworkChanges()
    }

    private fun initPost() {
        mViewModel.user.observe(this) { user ->
            if (user.name.isNullOrEmpty()){
                mViewBinding.toolbarTitle.text = user.login
                mViewBinding.userName.text = getString(R.string.name, user.login)
            }else {
                mViewBinding.toolbarTitle.text = user.name
                mViewBinding.userName.text = getString(R.string.name, user.name)
            }
            if (!user.company.isNullOrEmpty()){
                mViewBinding.companyName.text = getString(R.string.company_name, user.company)
            }else{
                mViewBinding.companyName.text = getString(R.string.company_name, "")
            }
            mViewBinding.blog.text = getString(R.string.blog, user.blog)
            mViewBinding.tvFollowers.text = getString(R.string.followers, user.followers.toString())
            mViewBinding.tvFollowings.text = getString(R.string.followings, user.following.toString())
            mViewBinding.etNotes.setText(user.note)
            mViewBinding.etNotes.setSelection(mViewBinding.etNotes.text.length)
            userName=user.login.toString()

            if (Constants.colorInvert){
                try {
                Glide.with(this)
                    .asBitmap()
                    .load(user.avatar_url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            resource.apply {
                                // set inverted colors bitmap to second image view
                                invertColors()?.apply {
                                    mViewBinding.imageView.setImageBitmap(this)
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else{
                mViewBinding.imageView.load(user.avatar_url)

            }


        }
          getUsersDetail(userName, userId!!)


    }
    // extension function to invert bitmap colors
    fun Bitmap.invertColors(): Bitmap? {
        val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        )

        val matrixInvert = ColorMatrix().apply {
            set(
                    floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                    )
            )
        }

        val paint = Paint()
        ColorMatrixColorFilter(matrixInvert).apply {
            paint.colorFilter = this
        }

        Canvas(bitmap).drawBitmap(this, 0f, 0f, paint)
        return bitmap
    }



    override fun getViewBinding(): ActivityPostDetailsBinding =
        ActivityPostDetailsBinding.inflate(layoutInflater)



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME="username"

        fun getStartIntent(
                context: Context,
                userId: Int,
                username: String
        ) = Intent(context, UserDetailsActivity::class.java).apply {
            putExtra(KEY_USER_ID, userId)
            putExtra(KEY_USER_NAME,username)
        }
    }
    /**
     * Observe network changes  Internet Connectivity
     */
    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this) { isConnected ->
            if (!isConnected) {
                mViewBinding.textViewNetworkStatus.text =
                    getString(R.string.text_no_connectivity)
                mViewBinding.networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                    initPost()
                }
            } else {
                if (mViewModel.userDetailLiveData.value is State.Error) {
                    getUsersDetail(userName,userId!!)
                }
                mViewBinding.textViewNetworkStatus.text = getString(R.string.text_connectivity)
                mViewBinding.networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(MainActivity.ANIMATION_DURATION)
                        .setDuration(MainActivity.ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        }
    }
    /***
     *  get User Detail from remote server
     */
    private fun getUsersDetail(userName:String, userId: Int) = mViewModel.getUserDetail(userName,userId)



    private fun observeUserDetail() {
        mViewModel.userDetailLiveData.observe(this) { state ->
            when (state) {
                is State.Loading ->{

                }
                is State.Success -> {
                    if (state.data.note.isNullOrEmpty()&& !mViewBinding.etNotes.text.isNullOrEmpty()){

                        mViewBinding.btnSave.performClick()
                    }
                }
                is State.Error -> {
                    showToast(state.message)
                }
            }
        }
    }
}
