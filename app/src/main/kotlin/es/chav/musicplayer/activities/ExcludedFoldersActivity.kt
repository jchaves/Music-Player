package es.chav.musicplayer.activities

import android.os.Bundle
import es.chav.musicplayer.adapters.ExcludedFoldersAdapter
import es.chav.musicplayer.databinding.ActivityExcludedFoldersBinding
import es.chav.musicplayer.extensions.config
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.interfaces.RefreshRecyclerViewListener

class ExcludedFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityExcludedFoldersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.excludedFoldersCoordinator, binding.excludedFoldersList, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.excludedFoldersList, binding.excludedFoldersToolbar)
        updateFolders()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.excludedFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        val folders = config.excludedFolders.toMutableList() as ArrayList<String>
        binding.excludedFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())
        }

        val adapter = ExcludedFoldersAdapter(this, folders, this, binding.excludedFoldersList) {}
        binding.excludedFoldersList.adapter = adapter
    }

    override fun refreshItems() {
        updateFolders()
    }
}
