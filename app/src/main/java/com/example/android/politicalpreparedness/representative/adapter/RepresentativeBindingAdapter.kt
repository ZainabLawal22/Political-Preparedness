package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.let {
        val uri = it.toUri().buildUpon().scheme("https").build()

        // Use Glide to load the image with a placeholder and error image (circular cropped)
        Glide.with(view.context)
            .load(uri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
            )
            .into(view)
    } ?: run {
        // Handle case where src is null or empty
        Glide.with(view.context)
            .load(R.drawable.ic_profile)
            .into(view)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T> {
    return adapter.takeIf { it is ArrayAdapter<*> }
        ?.let { it as ArrayAdapter<T> }
        ?: throw IllegalArgumentException("Adapter cannot be cast to the specified type")
}
