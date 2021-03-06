package com.boardgamegeek.ui.adapter

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.boardgamegeek.R
import com.boardgamegeek.auth.Authenticator
import com.boardgamegeek.extensions.colorize
import com.boardgamegeek.extensions.getSyncPlays
import com.boardgamegeek.extensions.isCollectionSetToSync
import com.boardgamegeek.extensions.showAndSurvive
import com.boardgamegeek.ui.*
import com.boardgamegeek.ui.dialog.CollectionStatusDialogFragment
import com.boardgamegeek.ui.viewmodel.GameViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GamePagerAdapter(fragmentManager: FragmentManager, private val activity: FragmentActivity, private val gameId: Int, var gameName: String) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var currentPosition = 0
        set(value) {
            field = value
            displayFab()
        }

    private var thumbnailUrl = ""
    private var imageUrl = ""
    private var heroImageUrl = ""
    private var arePlayersCustomSorted = false
    private var isFavorite = false
    @ColorInt
    private var iconColor = Color.TRANSPARENT
    private val tabs = arrayListOf<Tab>()

    private val fab: FloatingActionButton by lazy { activity.findViewById(R.id.fab) as FloatingActionButton }
    private val viewModel: GameViewModel by lazy { ViewModelProviders.of(activity).get(GameViewModel::class.java) }

    private inner class Tab(
            @field:StringRes val titleResId: Int,
            @field:DrawableRes var imageResId: Int = INVALID_RES_ID,
            val listener: () -> Unit = {})

    init {
        updateTabs()
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        updateTabs()
    }

    override fun getPageTitle(position: Int): CharSequence {
        @StringRes val resId = tabs.getOrNull(position)?.titleResId ?: INVALID_RES_ID
        return if (resId != INVALID_RES_ID) activity.getString(resId) else ""
    }

    override fun getItem(position: Int): Fragment {
        return when (tabs.getOrNull(position)?.titleResId) {
            R.string.title_description -> GameDescriptionFragment.newInstance()
            R.string.title_info -> GameFragment.newInstance()
            R.string.title_collection -> GameCollectionFragment.newInstance()
            R.string.title_plays -> GamePlaysFragment.newInstance()
            R.string.title_forums -> ForumsFragment.newInstanceForGame(gameId, gameName)
            R.string.links -> GameLinksFragment.newInstance()
            else -> ErrorFragment.newInstance()
        }
    }

    override fun getCount(): Int = tabs.size

    private fun updateTabs() {
        tabs.clear()
        tabs.add(Tab(R.string.title_description, R.drawable.fab_log_play) {
            LogPlayActivity.logPlay(activity, gameId, gameName, thumbnailUrl, imageUrl, heroImageUrl, arePlayersCustomSorted)
        })
        tabs.add(Tab(R.string.title_info, R.drawable.fab_favorite_off) {
            viewModel.updateFavorite(!isFavorite)
        })
        if (shouldShowCollection())
            tabs.add(Tab(R.string.title_collection, R.drawable.fab_add) {
                activity.showAndSurvive(CollectionStatusDialogFragment.newInstance())
            })
        if (shouldShowPlays())
            tabs.add(Tab(R.string.title_plays, R.drawable.fab_log_play) {
                LogPlayActivity.logPlay(activity, gameId, gameName, thumbnailUrl, imageUrl, heroImageUrl, arePlayersCustomSorted)
            })
        tabs.add(Tab(R.string.title_forums))
        tabs.add(Tab(R.string.links))

        viewModel.game.observe(activity, Observer { resource ->
            resource.data?.let { entity ->
                gameName = entity.name
                imageUrl = entity.imageUrl
                thumbnailUrl = entity.thumbnailUrl
                heroImageUrl = entity.heroImageUrl
                arePlayersCustomSorted = entity.customPlayerSort
                iconColor = entity.iconColor
                isFavorite = entity.isFavorite
                updateFavIcon(isFavorite)
                fab.colorize(iconColor)
                fab.setOnClickListener { tabs.getOrNull(currentPosition)?.listener?.invoke() }
                displayFab(false)
            }
        })
    }

    private fun updateFavIcon(isFavorite: Boolean) {
        tabs.find { it.titleResId == R.string.title_info }?.let {
            it.imageResId = if (isFavorite) R.drawable.fab_favorite_on else R.drawable.fab_favorite_off
        }
    }

    private fun displayFab(animateChange: Boolean = true) {
        @DrawableRes val resId = tabs.getOrNull(currentPosition)?.imageResId ?: INVALID_RES_ID
        if (resId != INVALID_RES_ID) {
            val existingResId = fab.getTag(R.id.res_id) as? Int? ?: INVALID_RES_ID
            if (resId != existingResId) {
                if (fab.isShown) {
                    if (animateChange) {
                        fab.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                            override fun onHidden(fab: FloatingActionButton?) {
                                super.onHidden(fab)
                                fab?.setImageResource(resId)
                                fab?.show()
                            }
                        })
                    } else {
                        fab.setImageResource(resId)
                        // HACK or else the icon just disappears
                        fab.hide()
                        fab.show()
                    }
                } else {
                    fab.setImageResource(resId)
                    fab.show()
                }
            } else {
                if (!fab.isOrWillBeShown) fab.show()
            }
        } else {
            fab.hide()
        }
        fab.setTag(R.id.res_id, resId)
    }

    private fun shouldShowPlays() = Authenticator.isSignedIn(activity) && activity.getSyncPlays()

    private fun shouldShowCollection() = Authenticator.isSignedIn(activity) && activity.isCollectionSetToSync()

    companion object {
        const val INVALID_RES_ID = 0
    }
}