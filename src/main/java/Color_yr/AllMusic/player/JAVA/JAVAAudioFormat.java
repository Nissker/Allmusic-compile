package Color_yr.AllMusic.player.JAVA;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class JAVAAudioFormat extends AudioFormat {
    public JAVAAudioFormat(int outputFrequency, int i, int outputChannels, boolean b, boolean b1) {
        super(outputFrequency, i, outputChannels, b, b1);
    }

    public void setChannels(int channels) {
        this.channels = channels;
        this.frameSize = (channels == AudioSystem.NOT_SPECIFIED || sampleSizeInBits == AudioSystem.NOT_SPECIFIED) ? AudioSystem.NOT_SPECIFIED : ((sampleSizeInBits + 7) / 8) * channels;
    }

    public void setSampleRate(int sampleRate) {
        this.frameRate = this.sampleRate = sampleRate;
    }
}
