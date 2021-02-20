package Color_yr.AllMusic;

import Color_yr.AllMusic.Hud.Hud;
import Color_yr.AllMusic.player.Android.AndroidPlayer;
import Color_yr.AllMusic.player.IPlayer;
import Color_yr.AllMusic.player.JAVA.JAVAPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Mod(modid = AllMusic.MODID, version = AllMusic.VERSION, acceptedMinecraftVersions = "[1.12,)")
public class AllMusic {
    static final String MODID = "allmusic";
    static final String VERSION = "2.4.0";
    public static int v = -1;
    public static boolean isPlay = false;
    private static URL nowURL;
    private IPlayer nowPlaying;
    public static Logger logger;

    public final Thread thread = new Thread(() -> {
        while (true) {
            try {
                int nowV = (int) (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS) *
                        Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) * 100);
                if (v != nowV) {
                    nowPlaying.Set(nowV);
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public static URL Get(URL url) {
        if (url.toString().contains("https://music.163.com/song/media/outer/url?id=")
                || url.toString().contains("http://music.163.com/song/media/outer/url?id=")) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4 * 1000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36 Edg/84.0.522.52");
                connection.setRequestProperty("Host", "music.163.com");
                connection.connect();
                if (connection.getResponseCode() == 302) {
                    return new URL(connection.getHeaderField("Location"));
                }
                return connection.getURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        try {
            Class.forName("android.media.MediaPlayer");
            nowPlaying = new AndroidPlayer();
        } catch (ClassNotFoundException e) {
            nowPlaying = new JAVAPlayer();
        }
        MinecraftForge.EVENT_BUS.register(this);
        thread.start();
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel").register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent e) {
        if (!isPlay)
            return;
        SoundCategory data = e.getSound().getCategory();
        if(data == null)
            return;
        switch (data) {
            case MUSIC:
            case RECORDS:
                e.setResultSound(null);
        }
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        try {
            stopPlaying();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Hud.Lyric = Hud.Info = Hud.List = "";
        Hud.save = null;
    }

    @SubscribeEvent
    public void onClicentPacket(final FMLNetworkEvent.ClientCustomPacketEvent evt) {
        new Thread(() -> {
            try {
                final ByteBuf directBuf = evt.getPacket().payload();
                byte[] array = new byte[directBuf.readableBytes()];
                directBuf.getBytes(directBuf.readerIndex(), array);
                array[0] = 0;
                String message = new String(array, StandardCharsets.UTF_8).substring(1);
                if (message.equals("[Stop]")) {
                    stopPlaying();
                } else if (message.startsWith("[Play]")) {
                    Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.MUSIC);
                    Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.RECORDS);
                    nowURL = new URL(message.replace("[Play]", ""));
                    nowURL = Get(nowURL);
                    if (nowURL == null)
                        return;
                    stopPlaying();
                    nowPlaying.SetMusic(nowURL);
                    nowPlaying.play();
                } else if (message.startsWith("[Lyric]")) {
                    Hud.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    Hud.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    Hud.List = message.substring(6);
                } else if (message.equalsIgnoreCase("[clear]")) {
                    Hud.Lyric = Hud.Info = Hud.List = "";
                } else if (message.startsWith("{")) {
                    Hud.Set(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SubscribeEvent
    public void onRed(final TickEvent.RenderTickEvent e) {
        Hud.update();
    }

    private void stopPlaying() throws Exception {
        nowPlaying.close();
    }
}
