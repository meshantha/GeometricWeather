package wangdaye.com.geometricweather.service;

import android.os.Build;
import android.support.annotation.RequiresApi;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.TileHelper;

/**
 * Geo tile service.
 * */

@RequiresApi(api = Build.VERSION_CODES.N)
public class TileService extends android.service.quicksettings.TileService {

    @Override
    public void onTileAdded() {
        TileHelper.setEnable(this, true);
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onTileRemoved() {
        TileHelper.setEnable(this, false);
    }

    @Override
    public void onStartListening () {
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onStopListening () {
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onClick () {
        IntentHelper.startMainActivity(this);
    }
}