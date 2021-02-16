package com.austinhodak.thehideout

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var mActivityTestRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun mainActivityTest() {
        val textView = onView(
            allOf(
                withId(R.id.ammo_name), withText("AAC Blackout AP"),
                withParent(withParent(IsInstanceOf.instanceOf(androidx.cardview.widget.CardView::class.java))),
                isDisplayed()
            )
        )
        textView.check(matches(withText("AAC Blackout AP")))

        val appCompatImageButton = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())

        val recyclerView = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(2, click()))

        val textView2 = onView(
            allOf(
                withId(R.id.armorName), withText("6B5-15"),
                withParent(
                    allOf(
                        withId(R.id.linearLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("6B5-15")))

        val appCompatImageButton2 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton2.perform(click())

        val recyclerView2 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(3, click()))

        val textView3 = onView(
            allOf(
                withId(R.id.backpackName), withText("6SH118"),
                withParent(
                    allOf(
                        withId(R.id.linearLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView3.check(matches(withText("6SH118")))

        val appCompatImageButton3 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton3.perform(click())

        val recyclerView3 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(4, click()))

        val textView4 = onView(
            allOf(
                withId(R.id.keyName), withText("Cabinet Key"),
                withParent(withParent(withId(R.id.keyRoot))),
                isDisplayed()
            )
        )
        textView4.check(matches(withText("Cabinet Key")))

        val recyclerView4 = onView(
            allOf(
                withId(R.id.key_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    0
                )
            )
        )
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val recyclerView5 = onView(
            allOf(
                withId(R.id.key_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    0
                )
            )
        )
        recyclerView5.perform(actionOnItemAtPosition<ViewHolder>(0, longClick()))

        val recyclerView6 = onView(
            allOf(
                withId(R.id.key_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    0
                )
            )
        )
        recyclerView6.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val appCompatImageButton4 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton4.perform(click())

        val recyclerView7 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView7.perform(actionOnItemAtPosition<ViewHolder>(6, click()))

        val textView5 = onView(
            allOf(
                withId(R.id.weaponName), withText("APB"),
                withParent(withParent(withId(R.id.weaponListItemCard))),
                isDisplayed()
            )
        )
        //textView5.check(matches(withText("APB")))

        val recyclerView8 = onView(
            allOf(
                withId(R.id.weaponList),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    0
                )
            )
        )
        //recyclerView8.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        /*val textView6 = onView(
            allOf(
                withId(R.id.weaponName), withText("9x18mm Makarov"),
                withParent(
                    allOf(
                        withId(R.id.linearLayout),
                        withParent(withId(R.id.timeline_top_card))
                    )
                ),
                isDisplayed()
            )
        )
        textView6.check(matches(withText("9x18mm Makarov")))*/

        /*val textView7 = onView(
            allOf(
                withId(R.id.ammoSmallName), withText("PBM"),
                withParent(
                    allOf(
                        withId(R.id.ammoSmallTop),
                        withParent(withId(R.id.weaponDetailAmmoList))
                    )
                ),
                isDisplayed()
            )
        )
        textView7.check(matches(withText("PBM")))*/

        /*val appCompatImageButton5 = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.weaponDetailToolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton5.perform(click())*/

        val appCompatImageButton6 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton6.perform(click())

        val recyclerView9 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView9.perform(actionOnItemAtPosition<ViewHolder>(8, click()))

        val textView8 = onView(
            allOf(
                withId(R.id.fleaItemName), withText("Lab. Red keycard"),
                withParent(withParent(withId(R.id.itemCard))),
                isDisplayed()
            )
        )
        textView8.check(matches(withText("Lab. Red keycard")))

        val recyclerView10 = onView(
            allOf(
                withId(R.id.flea_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    1
                )
            )
        )
        recyclerView10.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val textView9 = onView(
            allOf(
                withText("Lab. Red keycard"),
                withParent(
                    allOf(
                        withId(R.id.weaponDetailToolbar),
                        withParent(withId(R.id.appBarLayout))
                    )
                ),
                isDisplayed()
            )
        )
        textView9.check(matches(withText("Lab. Red keycard")))

        val appCompatImageButton7 = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.weaponDetailToolbar),
                        childAtPosition(
                            withId(R.id.appBarLayout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageButton7.perform(click())

        val appCompatImageButton8 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton8.perform(click())

        val recyclerView11 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView11.perform(actionOnItemAtPosition<ViewHolder>(9, click()))

        val chip = onView(
            allOf(
                withId(R.id.chip_all), withText("All"),
                childAtPosition(
                    allOf(
                        withId(R.id.chipGroup2),
                        childAtPosition(
                            withId(R.id.questSelectorScrollbar),
                            0
                        )
                    ),
                    0
                )
            )
        )
        chip.perform(scrollTo(), click())

        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.menuCrafts), withContentDescription("Crafts"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.hideoutBottomNavBar),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val appCompatImageButton10 = onView(
            allOf(
                withContentDescription("Open"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton10.perform(click())

        val recyclerView12 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView12.perform(actionOnItemAtPosition<ViewHolder>(10, click()))

        val bottomNavigationItemView2 = onView(
            allOf(
                withId(R.id.questTraders), withContentDescription("Traders"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.questBottomNavBar),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView2.perform(click())

        val bottomNavigationItemView3 = onView(
            allOf(
                withId(R.id.questItems), withContentDescription("Items"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.questBottomNavBar),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView3.perform(click())

        val bottomNavigationItemView4 = onView(
            allOf(
                withId(R.id.questMaps), withContentDescription("Maps"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.questBottomNavBar),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView4.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
