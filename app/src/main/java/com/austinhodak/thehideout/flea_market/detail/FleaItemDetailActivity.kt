package com.austinhodak.thehideout.flea_market.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ActivityFleaDetailBinding
import com.austinhodak.thehideout.flea_market.models.Barter
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.quests.models.QuestNew
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.userRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.lang.reflect.Type


class FleaItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFleaDetailBinding
    private var fleaItem: FleaItem? = null
    private var itemID: String? = null
    private lateinit var fleaViewModel: FleaViewModel
    private lateinit var menu: Menu
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFleaDetailBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        setupToolbar()

        fleaViewModel = ViewModelProvider(this).get(FleaViewModel::class.java)
        fleaViewModel.fleaItems.observe(this) { list ->
            if (intent.getStringExtra("id").isNullOrEmpty()) return@observe
            fleaItem = list.find { it.uid == intent.getStringExtra("id")!! }
            binding.item = fleaItem
            itemID = fleaItem?.bsgId
            setupFavorite()
            setupNav()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fleaNavFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.fleaBottomNavBar.setupWithNavController(navController)

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.item_detail_graph)

        val navArgument1 = NavArgument.Builder().setDefaultValue(intent.getStringExtra("id")).build()
        graph.addArgument("id", navArgument1)
        navController.graph = graph

        binding.fleaBottomNavBar.setOnNavigationItemSelectedListener { item ->
            val args = Bundle()
            args.putString("id", intent.getStringExtra("id"))
            navController.popBackStack()
            navController.navigate(item.itemId, args)
            true
        }
    }

    private fun setupNav() {
        val questsNeeded = getQuests().filter {
            it.needsItem(itemID ?: "")
        }

        val questBadge = binding.fleaBottomNavBar.getOrCreateBadge(R.id.itemDetailQuests)
        questBadge.backgroundColor = getColor(R.color.md_red_400)

        if (questsNeeded.isNotEmpty()) {
            questBadge.isVisible = true
            questBadge.number = questsNeeded.size
        } else {
            questBadge.isVisible = false
            binding.fleaBottomNavBar.menu.removeItem(R.id.itemDetailQuests)
        }

        val bartersNeeded = getBarters().filter { it.isNeededForAny(itemID ?: "") }
        Timber.d(itemID.toString())
        if (bartersNeeded.isEmpty()) {
            binding.fleaBottomNavBar.menu.removeItem(R.id.itemDetailBarters)
        } else {
            val barterBadge = binding.fleaBottomNavBar.getOrCreateBadge(R.id.itemDetailBarters)
            barterBadge.backgroundColor = getColor(R.color.md_red_400)
            barterBadge.number = bartersNeeded.size
        }

        if (questsNeeded.isEmpty() && bartersNeeded.isEmpty()) {
            binding.fleaBottomAppBar.visibility = View.GONE
        } else {
            binding.fleaBottomAppBar.visibility = View.VISIBLE
        }
    }

    private fun getQuests(): List<QuestNew> {
        val groupListType: Type = object : TypeToken<ArrayList<QuestNew?>?>() {}.type
        return Gson().fromJson(resources.openRawResource(R.raw.quests_new).bufferedReader().use { it.readText() }, groupListType)
    }

    private fun getBarters(): List<Barter> {
        val groupListType: Type = object : TypeToken<ArrayList<Barter?>?>() {}.type
        return Gson().fromJson(resources.openRawResource(R.raw.barters).bufferedReader().use { it.readText() }, groupListType)
    }

    private fun setupFavorite() {
        fleaItem?.uid?.let {
            userRef("flea").child("favorites").child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value == true) {
                        //Item is favorite
                        if (this@FleaItemDetailActivity::menu.isInitialized) {
                            menu.let { menu ->
                                menu.findItem(R.id.fleaDetailFavorite).setIcon(R.drawable.ic_baseline_favorite_24_colored)
                            }
                        }
                        isFavorite = true
                    } else {
                        //Item is not favorite
                        if (this@FleaItemDetailActivity::menu.isInitialized) {
                            menu.let { menu ->
                                menu.findItem(R.id.fleaDetailFavorite).setIcon(R.drawable.ic_baseline_favorite_border_24)
                            }
                        }
                        isFavorite = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.weaponDetailToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.weaponDetailToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_flea_detail, menu).also {
            menu?.let {
                this.menu = it
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fleaDetailFavorite -> {
                fleaItem?.uid?.let {
                    if (!isFavorite) {
                        userRef("flea").child("favorites").child(it).setValue(!isFavorite)
                    } else {
                        userRef("flea").child("favorites").child(it).removeValue()
                    }
                }
            }
            R.id.fleaDetailTM -> {
                fleaItem?.link?.openWithCustomTab(this)
            }
            R.id.fleaDetailWiki -> {
                fleaItem?.wikiLink?.openWithCustomTab(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}