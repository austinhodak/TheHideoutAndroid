package com.austinhodak.thehideout

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.databinding.TestBindingItemBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class TestItemBinding(
    val name: String
) : AbstractBindingItem<TestBindingItemBinding>() {

    override fun bindView(binding: TestBindingItemBinding, payloads: List<Any>) {
        binding.test = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): TestBindingItemBinding {
        return TestBindingItemBinding.inflate(inflater, parent, false)
    }

    override val type: Int
        get() = R.id.fast_adapter_id
}