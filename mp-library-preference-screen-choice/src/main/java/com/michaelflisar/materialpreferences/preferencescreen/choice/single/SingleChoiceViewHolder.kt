package com.michaelflisar.materialpreferences.preferencescreen.choice.single

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.michaelflisar.materialpreferences.preferencescreen.recyclerview.PreferenceAdapter
import com.michaelflisar.materialpreferences.preferencescreen.recyclerview.viewholders.base.BaseDialogViewHolder

class SingleChoiceViewHolder<T : Any>(
    inflater: LayoutInflater,
    parent: ViewGroup,
    override val adapter: PreferenceAdapter
) : BaseDialogViewHolder<T, SingleChoicePreference<T>, ViewBinding?>(inflater, parent) {

    override fun createSubBinding(inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean): ViewBinding? = null

    override fun updateSummary(preference: SingleChoicePreference<T>) {
        val displayValue = preference.getChoiceDisplayValue(itemView.context, value)
        preference.summary.display(binding.summary, View.GONE, displayValue)
    }

    @SuppressLint("CheckResult")
    override fun createDialog(preference: SingleChoicePreference<T>): Dialog {
        val dialog = if (preference.bottomSheet) MaterialDialog(itemView.context, BottomSheet(LayoutMode.WRAP_CONTENT)) else MaterialDialog(itemView.context)

        dialog.show {
            title(text = preference.title.get(itemView.context))
            if (preference.showCheckBoxes) {
                val labels = preference.getChoiceLabels(context)
                val selectedIndex = labels.indexOf(preference.getChoiceDisplayValue(context, value))
                listItemsSingleChoice(
                    items = labels,
                    waitForPositiveButton = false,
                    initialSelection = selectedIndex,
                ) { dialog, index, text ->
                    update(preference.getChoiceValue(index), preference)
                    dialog.dismiss()
                }
                cornerRadius(16f)
            } else {
                listItems(
                    items = preference.getChoiceLabels(context),
                    waitForPositiveButton = false
                ) { dialog, index, text ->
                    update(preference.getChoiceValue(index), preference)
                    dialog.dismiss()
                }
                positiveButton(android.R.string.ok)
            }
        }
        return dialog
    }
}