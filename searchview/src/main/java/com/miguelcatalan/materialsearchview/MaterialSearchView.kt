package com.miguelcatalan.materialsearchview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miguelcatalan.materialsearchview.utils.AnimationUtil
import net.idik.lib.slimadapter.SlimAdapter

/**
 * @author Miguel Catalan Bañuls
 */
class MaterialSearchView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    mContext, attrs
), Filter.FilterListener {
    private var mMenuItem: MenuItem? = null

    /**
     * Return true if search is open
     *
     * @return
     */
    var isSearchOpen = false
        private set
    private var mAnimationDuration = 0
    private var mClearingFocus = false

    //Views
    private var mSearchLayout: View? = null
    private var mTintView: View? = null
    private var mSuggestionsListView: RecyclerView? = null
    private var mSearchSrcTextView: EditText? = null
    private var mBackBtn: ImageButton? = null
    private var mVoiceBtn: ImageButton? = null
    private var mEmptyBtn: ImageButton? = null
    private var mSearchTopBar: RelativeLayout? = null
    private var mOldQueryText: CharSequence? = null
    private var mUserQuery: CharSequence? = null
    private var mOnQueryChangeListener: OnQueryTextListener? = null
    private var mSearchViewListener: SearchViewListener? = null
    private val mAdapter: ListAdapter? = null
    private var mNewAdapter: SlimAdapter? = null
    private var mSavedState: SavedState? = null
    private var submit = false
    private var ellipsize = false
    private var allowVoiceSearch = false
    private var suggestionIcon: Drawable? = null
    private var mList: MutableList<SuggestionModel>? = null
    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        val a =
            mContext.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, defStyleAttr, 0)
        if (a != null) {
            if (a.hasValue(R.styleable.MaterialSearchView_searchBackground)) {
                background = a.getDrawable(R.styleable.MaterialSearchView_searchBackground)!!
            }
            if (a.hasValue(R.styleable.MaterialSearchView_android_textColor)) {
                setTextColor(a.getColor(R.styleable.MaterialSearchView_android_textColor, 0))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_android_textColorHint)) {
                setHintTextColor(
                    a.getColor(
                        R.styleable.MaterialSearchView_android_textColorHint,
                        0
                    )
                )
            }
            if (a.hasValue(R.styleable.MaterialSearchView_android_hint)) {
                setHint(a.getString(R.styleable.MaterialSearchView_android_hint))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_searchVoiceIcon)) {
                setVoiceIcon(a.getDrawable(R.styleable.MaterialSearchView_searchVoiceIcon))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_searchCloseIcon)) {
                setCloseIcon(a.getDrawable(R.styleable.MaterialSearchView_searchCloseIcon))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_searchBackIcon)) {
                setBackIcon(a.getDrawable(R.styleable.MaterialSearchView_searchBackIcon))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_searchSuggestionBackground)) {
                setSuggestionBackground(a.getDrawable(R.styleable.MaterialSearchView_searchSuggestionBackground))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_searchSuggestionIcon)) {
                setSuggestionIcon(a.getDrawable(R.styleable.MaterialSearchView_searchSuggestionIcon))
            }
            if (a.hasValue(R.styleable.MaterialSearchView_android_inputType)) {
                setInputType(
                    a.getInt(
                        R.styleable.MaterialSearchView_android_inputType,
                        EditorInfo.TYPE_NULL
                    )
                )
            }
            a.recycle()
        }
    }

    private fun initiateView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true)
        mSearchLayout = findViewById(R.id.search_layout)
        mSearchTopBar = mSearchLayout?.findViewById<View>(R.id.search_top_bar) as RelativeLayout
        mSuggestionsListView =
            mSearchLayout?.findViewById<View>(R.id.suggestion_list) as RecyclerView
        mSearchSrcTextView = mSearchLayout?.findViewById<View>(R.id.searchTextView) as EditText
        mBackBtn = mSearchLayout?.findViewById<View>(R.id.action_up_btn) as ImageButton
        mVoiceBtn = mSearchLayout?.findViewById<View>(R.id.action_voice_btn) as ImageButton
        mEmptyBtn = mSearchLayout?.findViewById<View>(R.id.action_empty_btn) as ImageButton
        mTintView = mSearchLayout?.findViewById(R.id.transparent_view)
        mSearchSrcTextView!!.setOnClickListener(mOnClickListener)
        mBackBtn!!.setOnClickListener(mOnClickListener)
        mVoiceBtn!!.setOnClickListener(mOnClickListener)
        mEmptyBtn!!.setOnClickListener(mOnClickListener)
        mTintView?.setOnClickListener(mOnClickListener)
        allowVoiceSearch = false
        showVoice(true)
        initSearchView()
        mSuggestionsListView!!.visibility = GONE
        setAnimationDuration(AnimationUtil.ANIMATION_DURATION_MEDIUM)

        mSuggestionsListView?.layoutManager = LinearLayoutManager(context)
    }

    fun getRV(): RecyclerView? {
        return mSuggestionsListView
    }

    private fun initSearchView() {
        mSearchSrcTextView!!.setOnEditorActionListener { v, actionId, event ->
            onSubmitQuery()
            true
        }
        mSearchSrcTextView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mUserQuery = s
                startFilter(s)
                this@MaterialSearchView.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mSearchSrcTextView!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showKeyboard(mSearchSrcTextView)
               // showSuggestions()
            }
        }
    }

    private fun startFilter(s: CharSequence) {
        if (s.isBlank()) {
            mNewAdapter?.updateData(mutableListOf(""))
            showSuggestions()
            return
        }
        val mFilteredList = mList?.filter { it.s?.contains(s, true) == true }?.sortedBy { it.s }
        Log.d("SEARCH", mFilteredList?.size.toString())
        mNewAdapter?.updateData(mFilteredList?.map { it.item })
        showSuggestions()
    }

    private val mOnClickListener = OnClickListener { v ->
        if (v === mBackBtn) {
            closeSearch()
        } else if (v === mVoiceBtn) {
            onVoiceClicked()
        } else if (v === mEmptyBtn) {
            mSearchSrcTextView?.setText(null)
        } else if (v === mSearchSrcTextView) {
            showSuggestions()
        } else if (v === mTintView) {
            closeSearch()
        }
    }

    private fun onVoiceClicked() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak an item name or number");    // user hint
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        ) // setting recognition model, optimized for short phrases – search queries
        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            1
        ) // quantity of results we want to receive
        if (mContext is Activity) {
            mContext.startActivityForResult(intent, REQUEST_VOICE)
        }
    }

    private fun onTextChanged(newText: CharSequence) {
        val text: CharSequence = mSearchSrcTextView!!.text
        mUserQuery = text
        val hasText = !TextUtils.isEmpty(text)
        if (hasText) {
            mEmptyBtn!!.visibility = VISIBLE
            showVoice(false)
        } else {
            mEmptyBtn!!.visibility = GONE
            mSuggestionsListView!!.visibility = GONE
            showVoice(true)
        }
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener!!.onQueryTextChange(newText.toString())
        }
        mOldQueryText = newText.toString()
    }

    private fun onSubmitQuery() {
        val query: CharSequence? = mSearchSrcTextView!!.text
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener == null || !mOnQueryChangeListener!!.onQueryTextSubmit(query.toString())) {
                closeSearch()
                //mSearchSrcTextView?.setText(null)
            }
        }
    }

    private val isVoiceAvailable: Boolean
        private get() {
            if (isInEditMode) {
                return true
            }
            val pm = context.packageManager
            val activities = pm.queryIntentActivities(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
            )
            return activities.size == 0
        }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View?) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view!!.hasFocus()) {
            view.clearFocus()
        }
        view?.requestFocus()
        val imm = view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    //Public Attributes
    override fun setBackground(background: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchTopBar!!.background = background
        } else {
            mSearchTopBar!!.setBackgroundDrawable(background)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mSearchTopBar!!.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        mSearchSrcTextView!!.setTextColor(color)
    }

    fun setHintTextColor(color: Int) {
        mSearchSrcTextView!!.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence?) {
        mSearchSrcTextView!!.hint = hint
    }

    fun setVoiceIcon(drawable: Drawable?) {
        mVoiceBtn!!.setImageDrawable(drawable)
    }

    fun setCloseIcon(drawable: Drawable?) {
        mEmptyBtn!!.setImageDrawable(drawable)
    }

    fun setBackIcon(drawable: Drawable?) {
        mBackBtn!!.setImageDrawable(drawable)
    }

    fun setSuggestionIcon(drawable: Drawable?) {
        suggestionIcon = drawable
    }

    fun setInputType(inputType: Int) {
        mSearchSrcTextView!!.inputType = inputType
    }

    fun setSuggestionBackground(background: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSuggestionsListView!!.background = background
            //mSuggestionsListView!!.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.rv_bg))
        } else {
            mSuggestionsListView!!.setBackgroundDrawable(background)
        }
    }

    fun setCursorDrawable(drawable: Int) {
        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            f.isAccessible = true
            f[mSearchSrcTextView] = drawable
        } catch (ignored: Exception) {
            Log.e("MaterialSearchView", ignored.toString())
        }
    }

    fun setVoiceSearch(voiceSearch: Boolean) {
        allowVoiceSearch = voiceSearch
    }

    //Public Methods
    fun showSuggestions() {
        /*if (mNewAdapter != null && mNewAdapter?.itemCount!! > 0 && mSuggestionsListView!!.visibility == GONE) {
            mSuggestionsListView!!.visibility = VISIBLE
        } else if (mNewAdapter?.itemCount!! == 0) {
            mSuggestionsListView!!.visibility = GONE
        }*/
    }

    /**
     * Submit the query as soon as the user clicks the item.
     *
     * @param submit submit state
     */
    fun setSubmitOnClick(submit: Boolean) {
        this.submit = submit
    }

    /**
     * Set Suggest List OnItemClickListener
     *
     * @param listener
     */
    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        //mSuggestionsListView.setOnItemClickListener(listener);
    }

    /**
     * Set Adapter for suggestions list. Should implement Filterable.
     *
     * @param adapter
     */
    fun setAdapter(adapter: SlimAdapter?) {
        mNewAdapter = adapter
        mSuggestionsListView!!.adapter = adapter
    }

    /**
     * Set Adapter for suggestions list with the given suggestion array
     *
     * @param suggestions array of suggestions
     */
    fun setSuggestions(suggestions: MutableList<SuggestionModel>) {
        mList = suggestions
        if (suggestions != null && suggestions.size > 0) {
            mTintView!!.visibility = VISIBLE
        } else {
            mTintView!!.visibility = GONE
        }
    }
    /*public void setSuggestions(String[] suggestions, boolean checkSuggestion) {

        if (suggestions != null && suggestions.length > 0) {
            mTintView.setVisibility(VISIBLE);
            final SearchAdapter adapter = new SearchAdapter(mContext, suggestions, suggestionIcon, ellipsize, checkSuggestion);
            setAdapter(adapter);

            setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setQuery((String) adapter.getItem(position), submit);
                }
            });
        } else {
            mTintView.setVisibility(GONE);
        }

    }*/
    /**
     * Dismiss the suggestions list.
     */
    fun dismissSuggestions() {
        if (mSuggestionsListView!!.visibility == VISIBLE) {
            mSuggestionsListView!!.visibility = GONE
        }
    }

    /**
     * Calling this will set the query to search text box. if submit is true, it'll submit the query.
     *
     * @param query
     * @param submit
     */
    fun setQuery(query: CharSequence?, submit: Boolean) {
        mSearchSrcTextView!!.setText(query)
        if (query != null) {
            mSearchSrcTextView!!.setSelection(mSearchSrcTextView!!.length())
            mUserQuery = query
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery()
        }
    }

    /**
     * if show is true, this will enable voice search. If voice is not available on the device, this method call has not effect.
     *
     * @param show
     */
    fun showVoice(show: Boolean) {
        if (show && isVoiceAvailable && allowVoiceSearch) {
            mVoiceBtn!!.visibility = VISIBLE
        } else {
            mVoiceBtn!!.visibility = GONE
        }
    }

    /**
     * Call this method and pass the menu item so this class can handle click events for the Menu Item.
     *
     * @param menuItem
     */
    fun setMenuItem(menuItem: MenuItem?) {
        mMenuItem = menuItem
        mMenuItem!!.setOnMenuItemClickListener {
            showSearch()
            true
        }
    }

    /**
     * Sets animation duration. ONLY FOR PRE-LOLLIPOP!!
     *
     * @param duration duration of the animation
     */
    fun setAnimationDuration(duration: Int) {
        mAnimationDuration = duration
    }
    /**
     * Open Search View. If animate is true, Animate the showing of the view.
     *
     * @param animate true for animate
     */
    /**
     * Open Search View. This will animate the showing of the view.
     */
    @JvmOverloads
    fun showSearch(animate: Boolean = true) {
        if (isSearchOpen) {
            return
        }

        mTintView!!.visibility = GONE

        //Request Focus
        mSearchSrcTextView?.text = null
        mSearchSrcTextView?.requestFocus()
        if (animate) {
            setVisibleWithAnimation()
        } else {
            mSearchLayout!!.visibility = VISIBLE
            if (mSearchViewListener != null) {
                mSearchViewListener!!.onSearchViewShown()
            }
        }
        isSearchOpen = true
    }

    private fun setVisibleWithAnimation() {
        val animationListener: AnimationUtil.AnimationListener =
            object : AnimationUtil.AnimationListener {
                override fun onAnimationStart(view: View): Boolean {
                    return false
                }

                override fun onAnimationEnd(view: View): Boolean {
                    if (mSearchViewListener != null) {
                        mSearchViewListener!!.onSearchViewShown()
                    }
                    return false
                }

                override fun onAnimationCancel(view: View): Boolean {
                    return false
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchLayout!!.visibility = VISIBLE
            AnimationUtil.reveal(mSearchTopBar, animationListener, mAnimationDuration)
        } else {
            //AnimationUtil.fadeInView(mSearchLayout, mAnimationDuration, animationListener);
        }
    }

    /**
     * Close search view.
     */
    fun closeSearch() {
        if (!isSearchOpen) {
            return
        }
        mSearchSrcTextView?.setText(null)
        dismissSuggestions()
        clearFocus()
        mSearchLayout!!.visibility = GONE
        if (mSearchViewListener != null) {
            mSearchViewListener!!.onSearchViewClosed()
        }
        isSearchOpen = false
    }

    /**
     * Set this listener to listen to Query Change events.
     *
     * @param listener
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        mOnQueryChangeListener = listener
    }

    /**
     * Set this listener to listen to Search View open and close events
     *
     * @param listener
     */
    fun setOnSearchViewListener(listener: SearchViewListener?) {
        mSearchViewListener = listener
    }

    /**
     * Ellipsize suggestions longer than one line.
     *
     * @param ellipsize
     */
    fun setEllipsize(ellipsize: Boolean) {
        this.ellipsize = ellipsize
    }

    override fun onFilterComplete(count: Int) {
        if (count > 0) {
            showSuggestions()
        } else {
            dismissSuggestions()
        }
    }

    /*override fun requestFocus(direction: Int, previouslyFocusedRect: Rect): Boolean {
        // Don't accept focus if in the middle of clearing focus
        if (mClearingFocus) return false
        // Check if SearchView is focusable.
        return if (!isFocusable) false else mSearchSrcTextView?.requestFocus(
            direction,
            previouslyFocusedRect
        ) ?: false
    }*/

    override fun clearFocus() {
        mClearingFocus = true
        hideKeyboard(this)
        super.clearFocus()
        mSearchSrcTextView!!.clearFocus()
        mClearingFocus = false
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        mSavedState = SavedState(superState)
        mSavedState!!.query = if (mUserQuery != null) mUserQuery.toString() else null
        mSavedState!!.isSearchOpen = isSearchOpen
        return mSavedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        mSavedState = state
        if (mSavedState!!.isSearchOpen) {
            showSearch(false)
            setQuery(mSavedState!!.query, false)
        }
        super.onRestoreInstanceState(mSavedState!!.superState)
    }

    internal class SavedState : BaseSavedState {
        var query: String? = null
        var isSearchOpen = false

        constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            query = `in`.readString()
            isSearchOpen = `in`.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(query)
            out.writeInt(if (isSearchOpen) 1 else 0)
        }

        companion object {
            //required field that makes Parcelables from a Parcel
            val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    interface OnQueryTextListener {
        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        fun onQueryTextSubmit(query: String?): Boolean

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        fun onQueryTextChange(newText: String?): Boolean
    }

    interface SearchViewListener {
        fun onSearchViewShown()
        fun onSearchViewClosed()
    }

    companion object {
        const val REQUEST_VOICE = 9999
    }

    init {
        initiateView()
        initStyle(attrs, defStyleAttr)
    }
}