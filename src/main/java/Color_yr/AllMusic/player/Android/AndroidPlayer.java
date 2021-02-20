package Color_yr.AllMusic.player.Android;

import Color_yr.AllMusic.player.IPlayer;
import android.media.MediaPlayer;

import java.net.URL;

public class AndroidPlayer implements IPlayer {
    private final MediaPlayer mMediaPlayer = new MediaPlayer();

    public AndroidPlayer() {
        mMediaPlayer.setOnCompletionListener(mp -> {

        });

        mMediaPlayer.setOnPreparedListener(mp -> {

        });
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> false);
    }

    @Override
    public void SetMusic(URL url) throws Exception {
        mMediaPlayer.setDataSource(url.toString());
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void Set(int a) {
        float temp1 = (a == 0) ? -80.0f : ((float) (a * 0.2 - 35.0));
        mMediaPlayer.setVolume(temp1, temp1);
    }

    @Override
    public void play() {
        mMediaPlayer.start();
    }

    @Override
    public void close() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }
}
