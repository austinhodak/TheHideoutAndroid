package com.austinhodak.thehideout.skills

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import com.austinhodak.thehideout.R

@Composable
fun CharacterSkillsScreen() {

}

sealed class Skill(
    val name: String,
    @DrawableRes val icon: Int,
    val description: String,
    val type: SkillType,
    val effects: List<String>,
    val effects_elite: List<String>,
    val raise: List<String>
) {
    object Endurance : Skill(
        "Endurance",
        R.drawable.skill_physical_endurance,
        "Endurance influences the amount of stamina and the rate of exhaustion while running or jumping, as well as holding and restoring breath.",
        SkillType.PHYSICAL,
        listOf("Increases stamina (+75% at Elite Level)", "Decreases jump stamina drain (-30% at Elite Level)", "Increases holding breath time (+100% at Elite Level)"),
        listOf("+75% stamina", "Increased hands Endurance", "Breathing is independent of energy"),
        listOf("Sprinting while not being overweight", "Walking while not being overweight (i.e yellow or red)")
    )

    object Health : Skill(
        "Health",
        R.drawable.skill_physical_health,
        "Good health speeds up the recovery from the damage sustained in the raids, decreases the probability of fractures and lowers the energy and dehydration rate.",
        SkillType.PHYSICAL,
        listOf("Decreases chance of fractures (-60% at elite level)", "Increases offline regeneration", "Decreases energy consumption (-30% at elite level)", "Decreases dehydration rate (-30% at elite level)"),
        listOf("Damage absorption", "Poison immunity level"),
        listOf("Whenever you gain EXP for Strength, Endurance or Vitality you also gain EXP for Health.", "Completing the quest An Apple a day keeps the Doctor away (+2 levels)")
    )

    object Immunity : Skill(
        "Immunity",
        R.drawable.skill_physical_immunity,
        "Affects the susceptibility to illnesses and the effectiveness of their treatment.",
        SkillType.PHYSICAL,
        listOf("Immunity to poisons chance", "Reduction of all negative effects of stimulants, food and water (+50% at elite level)", "Painkiller action time increased (+25% at elite level)", "Decrease of poison's action force (+50% at elite level)"),
        listOf("Chance to have immunity to negative effects from stimulants, food, water", "Chance to have immunity to poisons"),
        listOf("Using stimulants with negative effects", "Surviving negative stimulant and food effects")
    )

    object Metabolism : Skill(
        "Metabolism",
        R.drawable.skill_physical_metabolism,
        "Healthy metabolism improves and extends the effects of eating and drinking, and sustains the physical skills longer.",
        SkillType.PHYSICAL,
        listOf("Reduction of time of action of all negative effects of stimulants, food, water", "Reduction of poisons' action time", "Enhances food and drink positive effects"),
        listOf("Zero chance of dehydration and exhaustion effects"),
        listOf("Replenishing Energy and Hydration through provisions", "For efficient leveling you need to eat dehydrating food and then drink something", "Completing the quest The survivalist path - Junkie (+2 levels)")
    )

    object Strength : Skill(
        "Vitality",
        R.drawable.skill_physical_strength,
        "Increasing strength allows you to jump higher, sprint faster, hit harder, throw farther, and carry more weight.",
        SkillType.PHYSICAL,
        listOf("Aiming drains less stamina (+20% at elite level)", "Increases jump height (+20% at elite level)", "Increases carrying weight (+30% at elite level → 91 kg)", "Increases melee strike power (+30% at elite level)", "Increases movement and sprint speed (+20% at elite level)", "Increases throw distance (+20% at elite level)"),
        listOf("Weapons, body armor and chest rigs are now weightless", "Chance to deal critical melee damage (50%)\n"),
        listOf("Using throwable weapons (3 skill points limit per raid)", "Using Melee weapons (3 skill points limit per raid)", "Walk or sprint while carrying more than ~40% (yellow number) of your maximum carry weight (no skill point limit per raid)")
    )

    object Vitality : Skill(
        "Vitality",
        R.drawable.skill_physical_vitality,
        "Vitality improves your chances of surviving a wound by decreasing the possibility of bleeding and instant death through critical damage of a body part.",
        SkillType.PHYSICAL,
        listOf("Decreases chance of bleeding (-60% at elite level)", "Decreases chance of dying by losing a limb (-20% at elite level)"),
        listOf("All bleedings stop on their own (Note: This means after bleeding occurs, the bleeding will stop after a few seconds)"),
        listOf("Taking damage", "Bloodloss", "Completing the quest Ambulance (+1 level)", "Completing the quest The survivalist path - Combat medic (+2 levels)")
    )

    object StressResistance : Skill(
        "Stress Resistance",
        R.drawable.skill_mental_stressresistance,
        "Stress resistance improves the chances of withstanding injury shock, shaking hands, and tremors.",
        SkillType.PHYSICAL,
        listOf("Decreases chance of getting pain shock (-50% at elite level)", "Decreases tremor oscillation"),
        listOf("Berserk mode"),
        listOf("Breaking legs by falling.", "Being on low health", "Having the \"Pain\" debuff", "Taking damage.", "Completing the quest Psycho Sniper (+1 level)", "Completing the quest The survivalist path - Cold blooded (+1 level)", "Completing the quest The survivalist path - Wounded Beast (+1 level)")
    )

    enum class SkillType () {
        PHYSICAL,
        MENTAL,
        COMBAT,
        PRACTICAL,
        SPECIAL_USEC,
        SPECIAL_BEAR
    }
}