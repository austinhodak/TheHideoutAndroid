package com.austinhodak.thehideout.weapons.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.austinhodak.tarkovapi.utils.getCaliberImage
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.bsg.models.ammo.BsgAmmo
import com.austinhodak.thehideout.bsg.models.weapon.BsgWeapon
import com.austinhodak.thehideout.bsg.viewmodels.BSGViewModel
import com.austinhodak.thehideout.databinding.FragmentWeaponDetailHomeBinding
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetailActivity
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.utils.log
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics

private const val ARG_PARAM1 = "id"

class WeaponDetailHomeFragment : Fragment() {

    private var _binding: FragmentWeaponDetailHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var weapon: BsgWeapon
    private val bsgViewModel: BSGViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            weapon = it.getSerializable(ARG_PARAM1) as BsgWeapon
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeaponDetailHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadWeapon()
    }

    private fun loadWeapon() {
        binding.weapon = weapon
        binding.fleaItem = weapon.fleaItem

        setupFleaData(weapon.fleaItem!!)

        Glide.with(this).load(getCaliberImage(weapon._props.ammoCaliber)).into(binding.weaponDetailAmmoIcon)

        var ammoList: List<BsgAmmo>
        bsgViewModel.getAmmo().observe(viewLifecycleOwner) { list ->
            ammoList = list.filter { it._props.Caliber == weapon._props.ammoCaliber }
            binding.weaponDetailAmmoDamage.text = ammoList.maxByOrNull { it._props.Damage }?._props?.ShortName
            binding.weaponDetailAmmoPen.text = ammoList.maxByOrNull { it._props.PenetrationPower }?._props?.ShortName
        }
    }

    private fun setupFleaData(item: FleaItem) {
        binding.ammoDetailFleaCard.setOnClickListener {
            log(FirebaseAnalytics.Event.SELECT_ITEM, item.uid ?: "", item.name ?: "", "flea_item")
            startActivity(Intent(requireActivity(), FleaItemDetailActivity::class.java).apply {
                putExtra("id", item.uid)
            })
        }

        when {
            item.diff24h!! > 0.0 -> {
                binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.md_green_500))
                binding.fleaDetail24HIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            item.diff24h < 0.0 -> {
                binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.md_red_500))
                binding.fleaDetail24HIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
            }
            else -> {
                binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.primaryText60))
                binding.fleaDetail24HIcon.setImageResource(R.drawable.icons8_horizontal_line_96)
                binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
            }
        }

        when {
            item.diff7days!! > 0.0 -> {
                binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.md_green_500))
                binding.fleaDetail7DIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            item.diff7days < 0.0 -> {
                binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.md_red_500))
                binding.fleaDetail7DIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
            }
            else -> {
                binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.primaryText60))
                binding.fleaDetail7DIcon.setImageResource(R.drawable.icons8_horizontal_line_96)
                binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: BsgWeapon) =
            WeaponDetailHomeFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}