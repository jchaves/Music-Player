package es.chav.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.google.gson.Gson
import es.chav.musicplayer.R
import es.chav.musicplayer.activities.SimpleActivity
import es.chav.musicplayer.activities.TracksActivity
import es.chav.musicplayer.adapters.PlaylistsAdapter
import es.chav.musicplayer.databinding.FragmentPlaylistsBinding
import es.chav.musicplayer.dialogs.ChangeSortingDialog
import es.chav.musicplayer.dialogs.NewPlaylistDialog
import es.chav.musicplayer.extensions.audioHelper
import es.chav.musicplayer.extensions.config
import es.chav.musicplayer.extensions.mediaScanner
import es.chav.musicplayer.extensions.viewBinding
import es.chav.musicplayer.helpers.PLAYLIST
import es.chav.musicplayer.helpers.TAB_PLAYLISTS
import es.chav.musicplayer.models.Events
import es.chav.musicplayer.models.Playlist
import es.chav.musicplayer.models.sortSafely
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.ensureBackgroundThread
import org.greenrobot.eventbus.EventBus

class PlaylistsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var playlists = ArrayList<Playlist>()
    private val binding by viewBinding(FragmentPlaylistsBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        binding.playlistsPlaceholder2.underlineText()
        binding.playlistsPlaceholder2.setOnClickListener {
            NewPlaylistDialog(activity) {
                EventBus.getDefault().post(Events.PlaylistsUpdated())
            }
        }

        ensureBackgroundThread {
            val playlists = context.audioHelper.getAllPlaylists()
            playlists.forEach {
                it.trackCount = context.audioHelper.getPlaylistTrackCount(it.id)
            }

            playlists.sortSafely(context.config.playlistSorting)
            this.playlists = playlists

            activity.runOnUiThread {
                val scanning = activity.mediaScanner.isScanning()
                binding.playlistsPlaceholder.text = if (scanning) {
                    context.getString(R.string.loading_files)
                } else {
                    context.getString(org.fossify.commons.R.string.no_items_found)
                }
                binding.playlistsPlaceholder.beVisibleIf(playlists.isEmpty())
                binding.playlistsPlaceholder2.beVisibleIf(playlists.isEmpty() && !scanning)

                val adapter = binding.playlistsList.adapter
                if (adapter == null) {
                    PlaylistsAdapter(activity, playlists, binding.playlistsList) {
                        activity.hideKeyboard()
                        Intent(activity, TracksActivity::class.java).apply {
                            putExtra(PLAYLIST, Gson().toJson(it))
                            activity.startActivity(this)
                        }
                    }.apply {
                        binding.playlistsList.adapter = this
                    }

                    if (context.areSystemAnimationsEnabled) {
                        binding.playlistsList.scheduleLayoutAnimation()
                    }
                } else {
                    (adapter as PlaylistsAdapter).updateItems(playlists)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = playlists.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Playlist>
        getAdapter()?.updateItems(filtered, text)
        binding.playlistsPlaceholder.beVisibleIf(filtered.isEmpty())
        binding.playlistsPlaceholder2.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(playlists)
        binding.playlistsPlaceholder.beGoneIf(playlists.isNotEmpty())
        binding.playlistsPlaceholder2.beGoneIf(playlists.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_PLAYLISTS) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            playlists.sortSafely(activity.config.playlistSorting)
            adapter.updateItems(playlists, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.playlistsPlaceholder.setTextColor(textColor)
        binding.playlistsPlaceholder2.setTextColor(adjustedPrimaryColor)
        binding.playlistsFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.playlistsList.adapter as? PlaylistsAdapter
}
