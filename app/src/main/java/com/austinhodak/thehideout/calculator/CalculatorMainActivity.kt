package com.austinhodak.thehideout.calculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.*
import com.austinhodak.thehideout.calculator.viewmodels.SimViewModel
import com.austinhodak.thehideout.calculator.views.HealthBar
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.pickers.PickerActivity
import com.austinhodak.thehideout.utils.toSimAmmo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class CalculatorMainActivity : AppCompatActivity() {

    private val simViewModel: SimViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                val scaffoldState = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()

                val body by simViewModel.body.observeAsState(Body())
                val ammo by tarkovRepo.getAllAmmo.collectAsState(initial = emptyList())
                val armor by tarkovRepo.getItemsByTypesArmor(
                    listOf(
                        ItemTypes.ARMOR,
                        ItemTypes.RIG,
                        ItemTypes.HELMET
                    )
                ).collectAsState(initial = emptyList())

                val selectedCharacter by simViewModel.selectedCharacter.observeAsState()
                val characterList by simViewModel.characterList.observeAsState()

                val selectedArmor by simViewModel.selectedArmor.observeAsState()
                val selectedHelmet by simViewModel.selectedHelmet.observeAsState()
                val selectedAmmo by simViewModel.selectedAmmo.observeAsState()

                val selectedArmorC by simViewModel.selectedArmorC.observeAsState()
                val selectedHelmetC by simViewModel.selectedHelmetC.observeAsState()

                Timber.d(selectedHelmetC.toString())

                if (selectedAmmo == null && !ammo.isNullOrEmpty()) {
                    simViewModel.selectAmmo(ammo.first())
                }

                BottomSheetScaffold(
                    sheetContent = {
                        Column(
                            Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .fillMaxWidth()
                        ) {
                            ResetHealthCard(simViewModel)
                            CharacterCard(selectedCharacter, simViewModel)
                            AmmoCard(selectedAmmo, simViewModel)
                            HelmetCard(selectedHelmet, simViewModel)
                            ArmorCard(selectedArmor, simViewModel)
                        }
                    },
                    scaffoldState = scaffoldState,
                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                    topBar = { SimToolbar(simViewModel = simViewModel) },
                    sheetPeekHeight = 0.dp,
                    sheetBackgroundColor = Color(0xFF303030),

                    sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }, modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.icons8_ammo_100),
                                contentDescription = "Open Bottom Drawer",
                                tint = Color.Black
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.skeleton),
                            contentDescription = "Skeleton",
                            modifier = Modifier
                                .align(
                                    Alignment.Center
                                )
                                .fillMaxHeight()
                                .alpha(0.3f)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            //Text(text = "HELMET: ${selectedHelmetC?.durability}", color = White)
                            //Text(text = "ARMOR: ${selectedArmorC?.durability}", color = White)
                            Health(
                                Modifier.padding(top = 64.dp),
                                body.head,
                                viewModel = simViewModel
                            )
                            Health(
                                Modifier.padding(top = 96.dp),
                                body.thorax,
                                viewModel = simViewModel
                            )
                            Health(
                                Modifier.padding(top = 32.dp),
                                body.stomach,
                                viewModel = simViewModel
                            )
                            Row(
                                Modifier.padding(top = 32.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Health(
                                    bodyPart = body.rightArm,
                                    viewModel = simViewModel
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Health(
                                    bodyPart = body.leftArm,
                                    viewModel = simViewModel
                                )
                            }
                            Row(
                                Modifier.padding(top = 72.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Health(
                                    bodyPart = body.rightLeg,
                                    viewModel = simViewModel
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Health(
                                    bodyPart = body.leftLeg,
                                    viewModel = simViewModel
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {

                                val totalHealth by simViewModel.totalHealthTV.observeAsState()
                                val totalHealthCurrent by simViewModel.currentHealthTV.observeAsState()

                                Image(painter = painterResource(id = R.drawable.health_star), contentDescription = "", modifier = Modifier.align(CenterVertically))
                                Text(text = totalHealthCurrent.toString(), style = MaterialTheme.typography.h6, fontSize = 30.sp, color = Color(0xFFCDDC39), modifier = Modifier.padding(start = 8.dp))
                                Text(text = "/$totalHealth", color = Color(0x99FFFFFF), style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ResetHealthCard(
        simViewModel: SimViewModel
    ) {
        BottomCard(
            onClick = { simViewModel.resetHealth() },
            color = Color(0xFFFF5252),
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = CenterVertically
            ) {
                Icon(Icons.Filled.Refresh, "", tint = White)
                Text(
                    text = "Reset Health",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun BottomCard(
        onClick: () -> Unit,
        color: Color = Color(0xFF212121),
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = color,
            shape = RoundedCornerShape(16.dp),
            onClick = onClick,
            content = content
        )
    }


    @Composable
    private fun AmmoCard(selectedAmmo: Ammo?, simViewModel: SimViewModel) {
        BottomCard({
            Intent(this, PickerActivity::class.java).apply {
                putExtra("type", "ammo")
            }.let {
                ammoPickerLauncher.launch(it)
            }
        }) {
            Row(
                Modifier.padding(all = 16.dp),
                verticalAlignment = CenterVertically
            ) {
                if (selectedAmmo?.pricing?.iconLink != null) {
                    Image(
                        painter = rememberImagePainter(data = selectedAmmo.pricing?.iconLink),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_ammo_100),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp),
                        tint = White
                    )
                }
                Column(
                    Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = selectedAmmo?.name ?: "Choose Ammo",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                    Text(
                        text = "Damage: ${selectedAmmo?.ballistics?.damage} • Armor Damage: ${selectedAmmo?.ballistics?.armorDamage} • Penetration ${selectedAmmo?.ballistics?.penetrationPower}",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp,
                        color = White
                    )
                }
            }
        }
    }

    @Composable
    private fun ArmorCard(selectedArmor: Item?, simViewModel: SimViewModel) {
        BottomCard({
            Intent(this, PickerActivity::class.java).apply {
                putExtra("type", "armor")
            }.let {
                armorPickerLauncher.launch(it)
            }
        }) {
            Row(
                Modifier.padding(all = 16.dp),
                verticalAlignment = CenterVertically
            ) {
                if (selectedArmor?.pricing?.iconLink != null) {
                    Image(
                        painter = rememberImagePainter(data = selectedArmor.pricing?.iconLink),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_bulletproof_vest_100),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp),
                        tint = White
                    )
                }
                Column(
                    Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = selectedArmor?.ShortName ?: "No Chest Armor",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                    Text(
                        text = selectedArmor?.armorZone?.joinToString(", ") ?: "Select Chest Armor",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp,
                        color = White
                    )
                }
            }
        }
    }

    @Composable
    private fun HelmetCard(selectedArmor: Item?, simViewModel: SimViewModel) {
        BottomCard({
            Intent(this, PickerActivity::class.java).apply {
                putExtra("type", "helmet")
            }.let {
                armorPickerLauncher.launch(it)
            }
        }) {
            Row(
                Modifier.padding(all = 16.dp),
                verticalAlignment = CenterVertically
            ) {
                if (selectedArmor?.pricing?.iconLink != null) {
                    Image(
                        painter = rememberImagePainter(data = selectedArmor.pricing?.iconLink),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_helmet_96),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp),
                        tint = White
                    )
                }

                Column(
                    Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = selectedArmor?.ShortName ?: "No Helmet",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium, color = White
                    )
                    Text(
                        text = selectedArmor?.armorZone?.joinToString(", ") ?: "Select a Helmet",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp,
                        color = White
                    )
                }
            }
        }
    }

    @Composable
    private fun CharacterCard(selectedCharacter: Character?, simViewModel: SimViewModel) {
        BottomCard({
            Intent(this, PickerActivity::class.java).apply {
                putExtra("type", "character")
            }.let {
                characterPickerLauncher.launch(it)
            }
        }) {
            Row(
                Modifier.padding(all = 16.dp),
                verticalAlignment = CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.ic_baseline_person_24), contentDescription = "", modifier = Modifier.size(40.dp))
                Column(
                    Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = selectedCharacter?.name ?: "Choose Character",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                    Text(text = "${selectedCharacter?.health} Health", style = MaterialTheme.typography.caption, fontSize = 10.sp, color = White)
                }
            }
        }
    }

    @Composable
    private fun SimToolbar(
        simViewModel: SimViewModel
    ) {
        TopAppBar(
            title = { Text("Damage Simulator", color = Color.White) },
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = {
                    finish()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = {
                    //Reset health.
                    simViewModel.resetHealth()
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                }
            },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        )
    }

    @Composable
    private fun Health(
        modifier: Modifier = Modifier,
        bodyPart: BodyPart,
        viewModel: SimViewModel
    ) {
        AndroidView(factory = { context ->
            HealthBar(context, bodyPart = bodyPart, body = viewModel.body.value!!)
        }, modifier = modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
            when (bodyPart.name) {
                Part.HEAD -> {
                    viewModel.body.value?.shoot(bodyPart.name, viewModel.selectedAmmo.value?.toSimAmmo()!!, viewModel.selectedHelmetC.value ?: CArmor())
                }
                else -> {
                    viewModel.body.value?.shoot(bodyPart.name, viewModel.selectedAmmo.value?.toSimAmmo()!!, viewModel.selectedArmorC.value ?: CArmor())
                }
            }
            viewModel.updateHealth()
        })
    }

    private var ammoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.getSerializableExtra("item")?.let {
                if (it is Ammo) {
                    simViewModel.selectAmmo(it)
                }
            }
        }
    }

    private var armorPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.getSerializableExtra("item")?.let {
                if (it is Item) {
                    if (it.itemType == ItemTypes.HELMET) {
                        simViewModel.selectHelmet(it)
                    } else {
                        simViewModel.selectArmor(it)
                    }
                }
            }
        }
    }

    private var characterPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.getSerializableExtra("item")?.let {
                if (it is Character) {
                    simViewModel.selectCharacter(it)
                }
            }
        }
    }

/* private lateinit var binding: ActivityCalculatorMainBinding
    private lateinit var bottomBinding: BottomSheetCalculatorMainBinding
    private var body = Body()

    private lateinit var selectedAmmo: AmmoOld
    private var selectedHelmet: Armor? = null
    private var selectedChestArmor: Armor?  = null
    private lateinit var selectedCharacter: Character

    private lateinit var ammoViewModel: AmmoViewModel

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            if (result.data?.hasExtra("ammoID") == true) {
                //selectedAmmo = ammoViewModel.ammoList.value?.find { it._id == result.data?.getStringExtra("ammoID") }!!
                updateBottomSheet()
            }
            if (result.data?.hasExtra("helmetID") == true) {
                selectedHelmet = ArmorHelper.getArmors(this).find { it._id == result.data?.getStringExtra("helmetID") }
                updateBottomSheet()
            }
            if (result.data?.hasExtra("chestID") == true) {
                selectedChestArmor = ArmorHelper.getArmors(this).find { it._id == result.data?.getStringExtra("chestID") }
                updateBottomSheet()
            }
            if (result.data?.hasExtra("character") == true) {
                selectedCharacter = CalculatorHelper.getCharacters(this).find { it.name == result.data?.getStringExtra("character") }!!
                body.reset(selectedCharacter)
                updateBottomSheet()
            }
            updateDurabilities()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            bottomBinding = it.bottomSheet
        }

        setupToolbar()

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)

        val bs = BottomSheetBehavior.from(findViewById<ConstraintLayout>(R.id.bottomSheet))
        bs.skipCollapsed = true

        binding.floatingActionButton.setOnClickListener {
            if (bs.state == BottomSheetBehavior.STATE_HIDDEN || bs.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bs.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bs.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        body.linkToHealthBar(Part.HEAD, binding.healthHead)
        body.linkToHealthBar(Part.THORAX, binding.healthThorax)
        body.linkToHealthBar(Part.STOMACH, binding.healthStomach)
        body.linkToHealthBar(Part.LEFTARM, binding.healthLArm)
        body.linkToHealthBar(Part.RIGHTARM, binding.healthRArm)
        body.linkToHealthBar(Part.LEFTLEG, binding.healthLLeg)
        body.linkToHealthBar(Part.RIGHTLEG, binding.healthRLeg)

        *//*ammoViewModel.ammoList.observe(this) {
            selectedAmmo = it.find { it._id == "5f4a52549f319f4528ac363b" }!!

            if (intent.hasExtra("ammoID")) {
                selectedAmmo = it.find { it._id == intent.getStringExtra("ammoID") }!!
            }

            updateBottomSheet()
            updateDurabilities()
        }*//*

        selectedHelmet = if (intent.hasExtra("helmetID")) {
            ArmorHelper.getArmors(this).find { it._id == intent.getStringExtra("helmetID") }
        } else {
            null
        }

        selectedChestArmor = if (intent.hasExtra("chestID")) {
            ArmorHelper.getArmors(this).find { it._id == intent.getStringExtra("chestID") }
        } else {
            null
        }

        selectedCharacter = CalculatorHelper.getCharacters(this).first()

        updateDurabilities()
        updateBottomSheet()

        binding.healthHead.setOnClickListener {
            body.shoot(Part.HEAD, selectedAmmo.getCAmmo(), selectedHelmet?.getArmor())
        }

        binding.healthThorax.setOnClickListener {
            body.shoot(Part.THORAX, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthStomach.setOnClickListener {
            body.shoot(Part.STOMACH, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthLArm.setOnClickListener {
            body.shoot(Part.LEFTARM, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthRArm.setOnClickListener {
            body.shoot(Part.RIGHTARM, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthLLeg.setOnClickListener {
            body.shoot(Part.LEFTLEG, selectedAmmo.getCAmmo())
        }

        binding.healthRLeg.setOnClickListener {
            body.shoot(Part.RIGHTLEG, selectedAmmo.getCAmmo())
        }

        body.bindTotalTextView(binding.healthTotal)
        body.bindCurrentTextView(binding.healthCurrentTV)

        body.onShootListener = {
            updateDurabilities()
        }

        binding.bottomSheet.calcAmmoCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.AMMO)
        }
        binding.bottomSheet.calcHelmetCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.HELMET)
        }
        binding.bottomSheet.calcChestCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.CHEST)
        }
        binding.bottomSheet.calcPlayerCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.CHARACTER)
        }
    }

    private fun launchPicker(itemType: CalculatorPickerActivity.ItemType) {
        val intent = Intent(this, CalculatorPickerActivity::class.java)
        intent.putExtra("itemType", itemType)
        resultLauncher.launch(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun updateDurabilities() {
        binding.calcArmorDurabliltyCard.visibility = if (selectedHelmet == null && selectedChestArmor == null) View.GONE else View.VISIBLE

        var text = ""
        text += "${selectedHelmet?.getArmor()?.durability?.round(2) ?: 0}/${selectedHelmet?.getArmor()?.maxDurability ?: 0}"
        text += "\n${selectedChestArmor?.getArmor()?.durability?.round(2) ?: 0}/${selectedChestArmor?.getArmor()?.maxDurability ?: 0}"
        binding.calcDurabilitiesTV.text = text

        binding.calcDurabilitiesArmorNamesTV.text = "${selectedHelmet?.name ?: "Helmet"}: \n${selectedChestArmor?.name ?: "Chest"}: "
    }

    private fun updateBottomSheet() {
        if (selectedHelmet == null) {
            bottomBinding.calcHelmetName.text = getString(R.string.helmet_none)
            bottomBinding.calcHelmetSubtitle.text = getString(R.string.helmet_select)
        } else {
            bottomBinding.calcHelmetName.text = "${selectedHelmet?.name} • Class ${selectedHelmet?.level}"
            bottomBinding.calcHelmetSubtitle.text = selectedHelmet?.zones?.joinToString(separator = ", ")
        }

        if (selectedChestArmor == null) {
            bottomBinding.calcChestTitle.text = getString(R.string.armor_chest_none)
            bottomBinding.calcChestSubtitle.text = getString(R.string.armor_chest_select)
        } else {
            bottomBinding.calcChestTitle.text = "${selectedChestArmor?.name} • Class ${selectedChestArmor?.level}"
            bottomBinding.calcChestSubtitle.text = selectedChestArmor?.zones?.joinToString(separator = ", ")
        }

        if (this::selectedAmmo.isInitialized) {
            val caliber = AmmoHelper.getCaliberByID(selectedAmmo.caliber)
            bottomBinding.calcAmmoTitle.text = "${selectedAmmo.name} • ${caliber?.longName}"
            bottomBinding.calcAmmoSubtitle.text = "Damage: ${selectedAmmo.damage} • Armor Damage: ${selectedAmmo.armor_damage} • Penetration: ${selectedAmmo.penetration}"
        }

        if (this::selectedCharacter.isInitialized) {
            bottomBinding.calcPlayerTitle.text = selectedCharacter.name
            bottomBinding.calcPlayerSubtitle.text = "${selectedCharacter.health} Health"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_damage_calculator, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reset -> {
                body.reset(selectedCharacter)
                selectedHelmet?.resetDurability()
                selectedChestArmor?.resetDurability()
                updateDurabilities()
            }
        }
        return super.onOptionsItemSelected(item)
    }*/
}