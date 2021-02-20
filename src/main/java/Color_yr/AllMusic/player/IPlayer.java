package Color_yr.AllMusic.player;

import java.net.URL;

public interface IPlayer {
    void SetMusic(URL url) throws Exception;
    void Set(int a);
    void play() throws Exception;
    void close() throws Exception;
}
