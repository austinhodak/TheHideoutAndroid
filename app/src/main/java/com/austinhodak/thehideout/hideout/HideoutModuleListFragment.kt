@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.austinhodak.thehideout.hideout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.hideout.models.HideoutModule
import com.austinhodak.thehideout.hideout.viewmodels.HideoutViewModel
import net.idik.lib.slimadapter.SlimAdapter

class HideoutModuleListFragment : Fragment() {

    private lateinit var mAdapter: SlimAdapter
    private lateinit var mRecyclerView: RecyclerView
    private val viewModel: HideoutViewModel by activityViewModels()
    private val fleaViewModel: FleaViewModel by activityViewModels()
    private var chipSelected: Int? = R.id.chip_active

    private var mCompletedModuleIDs: ArrayList<Int> = ArrayList()
    private var mCompletedModules: MutableList<HideoutModule> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hideout_module_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupAdapter()

        (activity as MainActivity).isSearchHidden(true)

        viewModel.completedModules.observe(requireActivity()) {
            mCompletedModuleIDs.clear()
            mCompletedModules.clear()
            if (it?.completed != null) {
                for (item in it.completed!!.keys) {
                    mCompletedModuleIDs.add(item.replace("\"", "").toInt())
                    mCompletedModules.add(viewModel.moduleList.value?.find { it.id == item.replace("\"", "").toInt() }!!)
                }
            }

            chipSelected()

            Log.d("HIDEOUT", "COMPLETED MODULES: $mCompletedModuleIDs")
        }

        val chips = (requireActivity() as MainActivity).getQuestChips()
        (requireActivity() as MainActivity).updateChips(arrayListOf(getString(R.string.all), getString(R.string.current), getString(R.string.available), getString(
                    R.string.locked)))
        chipSelected = chips?.checkedChipId
        chipSelected()
        chips?.setOnCheckedChangeListener { _, checkedId ->
            chipSelected = checkedId
            chipSelected()
        }
    }

    private fun setupRecyclerView(view: View) {
        mRecyclerView = view.findViewById(R.id.moduleList)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        mAdapter = SlimAdapter.create().register<HideoutModule>(R.layout.item_hideout_tracker) { module, i ->
            val requirementRV = i.findViewById<RecyclerView>(R.id.hideoutModuleRequiresRV)
            requirementRV.layoutManager = LinearLayoutManager(requireContext())

            SlimAdapter.create().attachTo(requirementRV)
                .register<HideoutModule.ModuleRequire>(R.layout.item_hideout_requirement) { requirement, rI ->
                    rI.text(R.id.requirementName, "• $requirement")

                    //TODO Fix stuttering from this statement
                    rI.text(R.id.requirementSubtitle, requirement.getSubtitle(fleaViewModel.fleaItems.value))

                    i.text(R.id.hideoutModuleBuildPrice, module.getTotalBuildCost())
            }.updateData(module.require)

            i.image(R.id.hideoutModuleIcon, module.getIcon())
            i.text(R.id.hideoutModuleName, module.module)
            i.text(R.id.hideoutModuleSubtitle, module.getConstructionTime())

            val bonusesSection = i.findViewById<ConstraintLayout>(R.id.hideoutModuleBonusSection)
            bonusesSection.visibility = if (module.bonuses.isNullOrEmpty()) View.GONE else View.VISIBLE

            i.text(R.id.hideoutModuleBonuses, module.bonuses.joinToString(prefix = "• ", separator = "\n• "))

            val buildButton = i.findViewById<Button>(R.id.hideoutCompleteButton)

            if (mCompletedModuleIDs.contains(module.id)) {
                buildButton.text = "Level ${module.level}\nUndo"
                buildButton.isEnabled = true
                buildButton.setOnClickListener {
                    module.downgradeModule()
                }
            } else {
                buildButton.text = "Level ${module.level}\nBuild"
                buildButton.isEnabled = true
                buildButton.setOnClickListener {
                    module.buildModule()
                }
            }

            if (chipSelected == R.id.chip_completed) {
                buildButton.text = "Level ${module.level}\nLocked"
                buildButton.isEnabled = false
            }

        }.attachTo(mRecyclerView).updateData(viewModel.moduleList.value?.sortedWith(compareBy ({ it.level }, { it.module })))
    }

    private fun chipSelected() {
        when (chipSelected) {
            R.id.chip_all -> {
                //All
                mAdapter.updateData(viewModel.moduleList.value?.sortedWith(compareBy ({ it.level }, { it.module })))
            }
            R.id.chip_active -> {
                //Current
                mAdapter.updateData(viewModel.moduleList.value?.filter { mCompletedModuleIDs.contains(it.id) })
            }
            R.id.chip_locked -> {
                //Available
                mAdapter.updateData(viewModel.getAvailableModules(mCompletedModules))
            }
            R.id.chip_completed -> {
                //Locked
                mAdapter.updateData(viewModel.getLockedModules(mCompletedModules))
            }
            else -> {
                mAdapter.updateData(viewModel.moduleList.value?.sortedWith(compareBy ({ it.level }, { it.module })))
            }
        }

        if (mAdapter.itemCount == 0) {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.GONE
        }
    }
}