package es.chav.musicplayer.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import es.chav.musicplayer.activities.SimpleActivity
import es.chav.musicplayer.databinding.*
import es.chav.musicplayer.extensions.getVisibleTabs
import es.chav.musicplayer.fragments.*
import es.chav.musicplayer.helpers.*
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor

class ViewPagerAdapter(val activity: SimpleActivity) : PagerAdapter() {
    private val fragments = arrayListOf<MyViewPagerFragment>()
    private var primaryItem: MyViewPagerFragment? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return getFragment(position, container).apply {
            fragments.add(this)
            container.addView(this)
            setupFragment(activity)
            setupColors(activity.getProperTextColor(), activity.getProperPrimaryColor())
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        fragments.remove(item)
        container.removeView(item as View)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        primaryItem = `object` as MyViewPagerFragment
    }

    override fun getCount() = activity.getVisibleTabs().size

    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun getFragment(position: Int, container: ViewGroup): MyViewPagerFragment {
        val tab = activity.getVisibleTabs()[position]
        val layoutInflater = activity.layoutInflater
        return when (tab) {
            TAB_PLAYLISTS -> FragmentPlaylistsBinding.inflate(layoutInflater, container, false).root
            TAB_FOLDERS -> FragmentFoldersBinding.inflate(layoutInflater, container, false).root
            TAB_ARTISTS -> FragmentArtistsBinding.inflate(layoutInflater, container, false).root
            TAB_ALBUMS -> FragmentAlbumsBinding.inflate(layoutInflater, container, false).root
            TAB_TRACKS -> FragmentTracksBinding.inflate(layoutInflater, container, false).root
            TAB_GENRES -> FragmentGenresBinding.inflate(layoutInflater, container, false).root
            else -> throw IllegalArgumentException("Unknown tab: $tab")
        }
    }

    fun getAllFragments() = fragments

    fun getCurrentFragment() = primaryItem

    fun getPlaylistsFragment() = fragments.find { it is PlaylistsFragment }

    fun getTracksFragment() = fragments.find { it is TracksFragment }
}
