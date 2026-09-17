package com.damithecreative.sidevol;

import android.media.AudioManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class VolumeUpTileService extends TileService {
    @Override public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override public void onClick() {
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audio != null) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel("SideVol +");
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }
}
