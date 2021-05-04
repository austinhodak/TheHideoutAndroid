package com.austinhodak.thehideout


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainRoboTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainRoboTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView = onView(
            allOf(
                withId(R.id.ammo_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.ammoDetailToolbar),
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
        appCompatImageButton.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

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
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(2, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

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
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(3, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

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

        val recyclerView4 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(4, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton5 = onView(
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
        appCompatImageButton5.perform(click())

        val recyclerView5 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView5.perform(actionOnItemAtPosition<ViewHolder>(5, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

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

        val recyclerView6 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView6.perform(actionOnItemAtPosition<ViewHolder>(6, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton7 = onView(
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
        appCompatImageButton7.perform(click())

        val recyclerView7 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView7.perform(actionOnItemAtPosition<ViewHolder>(8, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView8 = onView(
            allOf(
                withId(R.id.flea_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    1
                )
            )
        )
        recyclerView8.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton8 = onView(
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
        appCompatImageButton8.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView9 = onView(
            allOf(
                withId(R.id.flea_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    1
                )
            )
        )
        recyclerView9.perform(actionOnItemAtPosition<ViewHolder>(6, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton9 = onView(
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
        appCompatImageButton9.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView10 = onView(
            allOf(
                withId(R.id.flea_list),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    1
                )
            )
        )
        recyclerView10.perform(actionOnItemAtPosition<ViewHolder>(7, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.itemDetailBarters), withContentDescription("Barters"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fleaBottomNavBar),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val recyclerView11 = onView(
            allOf(
                withId(R.id.barterInputRV),
                childAtPosition(
                    withId(R.id.constraintLayout9),
                    3
                )
            )
        )
        recyclerView11.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val bottomNavigationItemView2 = onView(
            allOf(
                withId(R.id.itemDetailBarters), withContentDescription("Barters"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fleaBottomNavBar),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView2.perform(click())

        val appCompatImageButton10 = onView(
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
        appCompatImageButton10.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton11 = onView(
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
        appCompatImageButton11.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val appCompatImageButton12 = onView(
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
        appCompatImageButton12.perform(click())

        val recyclerView12 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView12.perform(actionOnItemAtPosition<ViewHolder>(9, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val bottomNavigationItemView3 = onView(
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
        bottomNavigationItemView3.perform(click())

        val appCompatImageButton13 = onView(
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
        appCompatImageButton13.perform(click())

        val recyclerView13 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView13.perform(actionOnItemAtPosition<ViewHolder>(10, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val bottomNavigationItemView4 = onView(
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
        bottomNavigationItemView4.perform(click())

        val floatingActionButton = onView(
            allOf(
                withId(R.id.questStartRaidFAB),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_host_fragment),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        val recyclerView14 = onView(
            allOf(
                withId(R.id.startRaidMapRV),
                childAtPosition(
                    withClassName(`is`("androidx.core.widget.NestedScrollView")),
                    0
                )
            )
        )
        recyclerView14.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton14 = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.questInRaidToolbar),
                        childAtPosition(
                            withId(R.id.appBarLayout),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton14.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton15 = onView(
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
        appCompatImageButton15.perform(click())

        val recyclerView15 = onView(
            allOf(
                withId(R.id.material_drawer_recycler_view),
                childAtPosition(
                    withId(R.id.slider),
                    0
                )
            )
        )
        recyclerView15.perform(actionOnItemAtPosition<ViewHolder>(11, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val healthBar = onView(
            allOf(
                withId(R.id.healthThorax),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                        1
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        healthBar.perform(click())

        val healthBar2 = onView(
            allOf(
                withId(R.id.healthThorax),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                        1
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        healthBar2.perform(click())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.reset), withContentDescription("Reset"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val floatingActionButton2 = onView(
            allOf(
                withId(R.id.floatingActionButton),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        floatingActionButton2.perform(click())

        val materialCardView = onView(
            allOf(
                withId(R.id.calcPlayerCard),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomSheet),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialCardView.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView16 = onView(
            allOf(
                withId(R.id.calculatorPickerRV),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    2
                )
            )
        )
        recyclerView16.perform(actionOnItemAtPosition<ViewHolder>(2, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val materialCardView2 = onView(
            allOf(
                withId(R.id.calcAmmoCard),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomSheet),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialCardView2.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val recyclerView17 = onView(
            allOf(
                withId(R.id.calculatorPickerRV),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    2
                )
            )
        )
        recyclerView17.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val materialCardView3 = onView(
            allOf(
                withId(R.id.calcHelmetCard),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomSheet),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialCardView3.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val recyclerView18 = onView(
            allOf(
                withId(R.id.calculatorPickerRV),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    2
                )
            )
        )
        recyclerView18.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val materialCardView4 = onView(
            allOf(
                withId(R.id.calcChestCard),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomSheet),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialCardView4.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val recyclerView19 = onView(
            allOf(
                withId(R.id.calculatorPickerRV),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    2
                )
            )
        )
        recyclerView19.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val floatingActionButton3 = onView(
            allOf(
                withId(R.id.floatingActionButton),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        floatingActionButton3.perform(click())

        val healthBar3 = onView(
            allOf(
                withId(R.id.healthThorax),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                        1
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        healthBar3.perform(click())

        val appCompatImageButton16 = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
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
        appCompatImageButton16.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000)

        val appCompatImageButton17 = onView(
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
        appCompatImageButton17.perform(click())
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
