package com.app.tawk.ui.main.viewholder

import android.graphics.*
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.app.tawk.R
import com.app.tawk.databinding.ItemPostBinding
import com.app.tawk.model.User
import com.app.tawk.utils.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget

/**
 * [RecyclerView.ViewHolder] implementation to inflate View for RecyclerView.
 * See [UserListAdapter]]
 */
class UserViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User, position: Int, onItemClicked: (User, ImageView) -> Unit) {
        if ((position+1)%4==0){
            binding.imageView.loadCircularImage(
                user.avatar_url, // or any object supported by Glide
                2f, // default is 0. If you don't change it, then the image will have no border
                 ContextCompat.getColor(itemView.context,R.color.colorAccent),true
            )
        }else{
            binding.imageView.loadCircularImage(
                user.avatar_url, // or any object supported by Glide
                    2f, // default is 0. If you don't change it, then the image will have no border
                ContextCompat.getColor(itemView.context,R.color.bulbColor),false
            )
        }

        binding.title.text = user.login
        binding.subTitle.text = user.html_url
        if (user.note.isNullOrEmpty()){
            binding.ivNote.visibility= View.GONE
        }else{
            binding.ivNote.visibility= View.VISIBLE
        }
        binding.root.id=position


        binding.root.setOnClickListener {
            Constants.colorInvert = (binding.root.id+1)%4==0
            onItemClicked(user, binding.imageView)
        }
    }
    fun <T> ImageView.loadCircularImage(
            model: T,
            borderSize: Float = 0F,
            borderColor: Int = Color.WHITE,
            colorInverter: Boolean=false
    ) {
        Glide.with(context)
                .asBitmap()
                .load(model)
                .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .fitCenter()
                .circleCrop()
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_profie_placeholder)
                .error(R.drawable.ic_broken_image)
                .priority(Priority.HIGH))
                .into(object : BitmapImageViewTarget(this) {
                    override fun setResource(resource: Bitmap?) {
                        setImageDrawable(
                                resource?.run {
                                    RoundedBitmapDrawableFactory.create(
                                            resources,
                                            if (borderSize > 0) {
                                                createBitmapWithBorder(borderSize, borderColor, colorInverter)
                                            } else {
                                                this
                                            }
                                    ).apply {
                                        isCircular = true
                                    }
                                }
                        )
                    }
                })
    }

    /**
     * Create a new bordered bitmap with the specified borderSize and borderColor
     *
     * @param borderSize - The border size in pixel
     * @param borderColor - The border color
     * @return A new bordered bitmap with the specified borderSize and borderColor
     */
    private fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int,colorInverter: Boolean): Bitmap {
        val borderOffset = (borderSize * 2).toInt()
        val halfWidth = width / 2
        val halfHeight = height / 2
        val circleRadius = Math.min(halfWidth, halfHeight).toFloat()
        var newBitmap = Bitmap.createBitmap(
                width + borderOffset,
                height + borderOffset,
                Bitmap.Config.ARGB_8888
        )

        // Center coordinates of the image
        val centerX = halfWidth + borderSize
        val centerY = halfHeight + borderSize

        val paint = Paint()
        // extension function to invert bitmap colors
        if (colorInverter) {
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

            ColorMatrixColorFilter(matrixInvert).apply {
                paint.colorFilter = this
            }
        }
        val canvas = Canvas(newBitmap).apply {
            // Set transparent initial area
            drawARGB(0, 0, 0, 0)
        }

        // Draw the transparent initial area
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, circleRadius, paint)

        // Draw the image
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, borderSize, borderSize, paint)

        // Draw the createBitmapWithBorder
        paint.xfermode = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderSize
        canvas.drawCircle(centerX, centerY, circleRadius, paint)
        return newBitmap

    }

}
