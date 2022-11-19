package com.example.weeklyupload

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.weeklyupload.Object.Image
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_dashboard.*
import kotlinx.android.synthetic.main.fragment_image_slider.view.*
import kotlinx.android.synthetic.main.image_container.view.*


class imageAdapter :
    RecyclerView.Adapter<imageAdapter.ViewHolder>() {

    private var images: List<Image> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.image_container,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = images[position]
        Picasso.get().load(item.path).into(holder.imageview);
        holder.imageLocation.text = item.location
        holder.imageview.setOnClickListener {
            val action = image_sliderDirections.actionImageSliderToImageDetail(item)
            holder.imageview.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageview = view.iv_image
        val imageLocation = view.image_location
    }

    fun setImage(image: List<Image>) {
        this.images = image
        notifyDataSetChanged()
    }

    fun getImage(position: Int): Image {
        return images[position]
    }

}