package com.austinhodak.thehideout.views

import androidx.annotation.LayoutRes
import com.austinhodak.thehideout.R
import com.mikepenz.materialdrawer.model.AbstractBadgeableDrawerItem

class OutdatedDrawerItem : AbstractBadgeableDrawerItem<OutdatedDrawerItem>() {

    override val type: Int
        get() = R.id.fast_adapter_id

    override val layoutRes: Int
        @LayoutRes
        get() = R.layout.view_drawer_item_outdated
}