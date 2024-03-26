package es.chav.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.google.gson.Gson
import es.chav.musicplayer.R
import es.chav.musicplayer.activities.SimpleActivity
import es.chav.musicplayer.activities.TracksActivity
import es.chav.musicplayer.adapters.GenresAdapter
import es.chav.musicplayer.databinding.FragmentGenresBinding
import es.chav.musicplayer.dialogs.ChangeSortingDialog
import es.chav.musicplayer.extensions.audioHelper
import es.chav.musicplayer.extensions.config
import es.chav.musicplayer.extensions.mediaScanner
import es.chav.musicplayer.extensions.viewBinding
import es.chav.musicplayer.helpers.GENRE
import es.chav.musicplayer.helpers.TAB_GENRES
import es.chav.musicplayer.models.Genre
import es.chav.musicplayer.models.sortSafely
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.areSystemAnimationsEnabled
import org.fossify.commons.extensions.beGoneIf
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.helpers.ensureBackgroundThread

class GenresFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var genres = ArrayList<Genre>()
    private val binding by viewBinding(FragmentGenresBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            val cachedGenres = activity.audioHelper.getAllGenres()
            activity.runOnUiThread {
                gotGenres(activity, cachedGenres)
            }
        }
    }

    private fun gotGenres(activity: BaseSimpleActivity, cachedGenres: ArrayList<Genre>) {
        genres = cachedGenres
        activity.runOnUiThread {
            val scanning = activity.mediaScanner.isScanning()
            binding.genresPlaceholder.text = if (scanning) {
                context.getString(R.string.loading_files)
            } else {
                context.getString(org.fossify.commons.R.string.no_items_found)
            }

            binding.genresPlaceholder.beVisibleIf(genres.isEmpty())

            val adapter = binding.genresList.adapter
            if (adapter == null) {
                GenresAdapter(activity, genres, binding.genresList) {
                    activity.hideKeyboard()
                    Intent(activity, TracksActivity::class.java).apply {
                        putExtra(GENRE, Gson().toJson(it as Genre))
                        activity.startActivity(this)
                    }
                }.apply {
                    binding.genresList.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    binding.genresList.scheduleLayoutAnimation()
                }
            } else {
                val oldItems = (adapter as GenresAdapter).items
                if (oldItems.sortedBy { it.id }.hashCode() != genres.sortedBy { it.id }.hashCode()) {
                    adapter.updateItems(genres)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = genres.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Genre>
        getAdapter()?.updateItems(filtered, text)
        binding.genresPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(genres)
        binding.genresPlaceholder.beGoneIf(genres.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_GENRES) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            genres.sortSafely(activity.config.genreSorting)
            adapter.updateItems(genres, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.genresPlaceholder.setTextColor(textColor)
        binding.genresFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.genresList.adapter as? GenresAdapter
}
