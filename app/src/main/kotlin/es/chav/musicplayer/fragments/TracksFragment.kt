package es.chav.musicplayer.fragments

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import es.chav.musicplayer.R
import es.chav.musicplayer.activities.SimpleActivity
import es.chav.musicplayer.adapters.TracksAdapter
import es.chav.musicplayer.databinding.FragmentTracksBinding
import es.chav.musicplayer.dialogs.ChangeSortingDialog
import es.chav.musicplayer.extensions.audioHelper
import es.chav.musicplayer.extensions.config
import es.chav.musicplayer.extensions.mediaScanner
import es.chav.musicplayer.extensions.viewBinding
import es.chav.musicplayer.helpers.TAB_TRACKS
import es.chav.musicplayer.models.Track
import es.chav.musicplayer.models.sortSafely
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.dialogs.PermissionRequiredDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.ensureBackgroundThread

// Artists -> Albums -> Tracks
class TracksFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var tracks = ArrayList<Track>()
    private val binding by viewBinding(FragmentTracksBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            tracks = context.audioHelper.getAllTracks()

            val excludedFolders = context.config.excludedFolders
            tracks = tracks.filter {
                !excludedFolders.contains(it.path.getParentPath())
            }.toMutableList() as ArrayList<Track>

            activity.runOnUiThread {
                val scanning = activity.mediaScanner.isScanning()
                binding.tracksPlaceholder.text = if (scanning) {
                    context.getString(R.string.loading_files)
                } else {
                    context.getString(org.fossify.commons.R.string.no_items_found)
                }
                binding.tracksPlaceholder.beVisibleIf(tracks.isEmpty())
                val adapter = binding.tracksList.adapter
                if (adapter == null) {
                    TracksAdapter(activity = activity, recyclerView = binding.tracksList, sourceType = TracksAdapter.TYPE_TRACKS, items = tracks) {
                        activity.hideKeyboard()
                        activity.handleNotificationPermission { granted ->
                            if (granted) {
                                val startIndex = tracks.indexOf(it as Track)
                                prepareAndPlay(tracks, startIndex)
                            } else {
                                if (context is Activity) {
                                    PermissionRequiredDialog(
                                        activity,
                                        org.fossify.commons.R.string.allow_notifications_music_player,
                                        { activity.openNotificationSettings() }
                                    )
                                }
                            }
                        }
                    }.apply {
                        binding.tracksList.adapter = this
                    }

                    if (context.areSystemAnimationsEnabled) {
                        binding.tracksList.scheduleLayoutAnimation()
                    }
                } else {
                    (adapter as TracksAdapter).updateItems(tracks)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = tracks.filter {
            it.title.contains(text, true) || ("${it.artist} - ${it.album}").contains(text, true)
        }.toMutableList() as ArrayList<Track>
        getAdapter()?.updateItems(filtered, text)
        binding.tracksPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(tracks)
        binding.tracksPlaceholder.beGoneIf(tracks.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_TRACKS) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            tracks.sortSafely(activity.config.trackSorting)
            adapter.updateItems(tracks, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.tracksPlaceholder.setTextColor(textColor)
        binding.tracksFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.tracksList.adapter as? TracksAdapter
}
