package es.chav.musicplayer.dialogs

import android.app.Activity
import es.chav.musicplayer.R
import es.chav.musicplayer.databinding.DialogRemovePlaylistBinding
import es.chav.musicplayer.models.Playlist
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.viewBinding

class RemovePlaylistDialog(val activity: Activity, val playlist: Playlist? = null, val callback: (deleteFiles: Boolean) -> Unit) {
    private val binding by activity.viewBinding(DialogRemovePlaylistBinding::inflate)

    init {
        binding.removePlaylistDescription.text = getDescriptionText()
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> callback(binding.removePlaylistCheckbox.isChecked) }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.remove_playlist)
            }
    }

    private fun getDescriptionText(): String {
        return if (playlist == null) {
            activity.getString(R.string.remove_playlist_description)
        } else
            String.format(activity.resources.getString(R.string.remove_playlist_description_placeholder), playlist.title)
    }
}
