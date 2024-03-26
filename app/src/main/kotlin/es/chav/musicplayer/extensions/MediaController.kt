package es.chav.musicplayer.extensions

import android.os.Bundle
import androidx.media3.session.MediaController
import es.chav.musicplayer.playback.CustomCommands

fun MediaController.sendCommand(command: CustomCommands, extras: Bundle = Bundle.EMPTY) = sendCustomCommand(command.sessionCommand, extras)
