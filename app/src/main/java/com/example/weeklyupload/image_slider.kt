package com.example.weeklyupload

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.weeklyupload.Object.Image
import com.example.weeklyupload.ViewModel.ImageViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_image_slider.*
import kotlinx.android.synthetic.main.fragment_image_slider.view.*
import kotlin.math.abs


class image_slider : Fragment() {

    private lateinit var imageAdapter: imageAdapter
    private val slideHanlder = Handler()
    private lateinit var call: OnPageChangeCallback
    private lateinit var imageViewModel: ImageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = imageAdapter()
        imageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_slider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewModel.getallImage().observe(viewLifecycleOwner) { image ->
            imageAdapter.setImage(image)


        }

        vp_image.apply {
            adapter = imageAdapter
            offscreenPageLimit = 3
            clipChildren = false
            clipToPadding = false
            setPageTransformer(tranformer())
            //registerOnPageChangeCallback()
        }
        /*vp_image.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                slideHanlder.removeCallbacks(slideRun)
                slideHanlder.postDelayed(slideRun, 2000)

            }
        })*/

        vp_image.children.find { it is RecyclerView }.let {

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val image: Image = imageAdapter.getImage(viewHolder.adapterPosition)



                    imageViewModel.deleteimage(image)

                    Snackbar.make(vp_image, "Delete " + image.path, Snackbar.LENGTH_LONG)
                        .setAction(
                            "Undo"
                        ) {
                            imageViewModel.addimage(image)

                        }.show()
                }

            }).attachToRecyclerView(it as RecyclerView)


        }


    }


    private fun tranformer(): ViewPager2.PageTransformer {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer(object : ViewPager2.PageTransformer {
            override fun transformPage(page: View, position: Float) {
                val r: Float = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }


        })
        return transformer
    }

}