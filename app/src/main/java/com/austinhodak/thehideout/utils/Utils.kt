package com.austinhodak.thehideout.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

class Utils {
    companion object {
        @JvmStatic @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, url:String) {
            Glide.with(view.context).load(url).into(view)
        }

        @JvmStatic @BindingAdapter("rvAdapter")
        fun setAdapter(rv: RecyclerView, caliber: CaliberModel) {
            //val mAdapter = RecyclerAdapter(RecyclerItem.diffCallback<AmmoModel>(), R.layout.ammo_list_item_small)
            val mAdapter = SlimAdapter.create().register<AmmoModel>(R.layout.ammo_list_item_small) { ammo, i ->

            }.attachTo(rv)
            mAdapter.updateData(caliber.ammo)
        }
    }
}