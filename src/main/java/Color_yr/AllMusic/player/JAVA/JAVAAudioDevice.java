/*
 * 11/26/04		Buffer size modified to support JRE 1.5 optimizations.
 *              (CPU usage < 1% under P4/2Ghz, RAM < 12MB).
 *              jlayer@javazoom.net
 * 11/19/04		1.0 moved to LGPL.
 * 06/04/01		Too fast playback fixed. mdm@techie.com
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package Color_yr.AllMusic.player.JAVA;

import Color_yr.AllMusic.AllMusic;
import Color_yr.AllMusic.decoder.Decoder;
import Color_yr.AllMusic.decoder.JavaLayerException;

import javax.sound.sampled.*;

/**
 * The <code>JavaSoundAudioDevice</code> implements an audio
 * device by using the JavaSound API.
 *
 * @author Mat McGowan
 * @since 0.0.8
 */
public class JAVAAudioDevice {
    public boolean startplay = false;
    private SourceDataLine source = null;
    private boolean open = false;
    private Decoder decoder = null;
    private JAVAAudioFormat fmt = null;

    private byte[] byteBuf = new byte[4096];

    private FloatControl volctrl;

    public JAVAAudioDevice() {
        try {
            Throwable t = null;
            try {
                fmt = new JAVAAudioFormat(48000,
                        16,
                        2,
                        true,
                        false);
                source = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
            } catch (IllegalArgumentException ignored) {
                Mixer.Info[] list = AudioSystem.getMixerInfo();
                if (list.length == 0) {
                    AllMusic.logger.info("没有音频设备");
                    return;
                }
                for (Mixer.Info item : list) {
                    AllMusic.logger.info(item.toString());
                }
                Mixer.Info item = list[0];
                fmt = new JAVAAudioFormat(8000,
                        16,
                        2,
                        true,
                        true);
                Mixer item1 = AudioSystem.getMixer(item);
                Line[] list1 = item1.getSourceLines();
                Line item2 = list1[0];
                source = (SourceDataLine) item2;
            } catch (RuntimeException | LinkageError | LineUnavailableException ex) {
                t = ex;
            }
            if (source == null) throw new JavaLayerException("cannot obtain source audio line", t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FloatControl getVolctrl() {
        return volctrl;
    }

    public synchronized void open(Decoder decoder) {
        this.decoder = decoder;
        open = true;
    }

    public synchronized void close() {
        flush();
        decoder = null;
        open = false;
    }

    public void write(short[] samples, int offs, int len) {
        if (open) {
            try {
                writeImpl(samples, offs, len);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public void flush() {
        if (source != null) {
            source.close();
            startplay = false;
        }
    }

    private void writeImpl(short[] samples, int offs, int len) throws LineUnavailableException {
        if (!startplay) {
            fmt.setSampleRate(decoder.getOutputFrequency());
            fmt.setChannels(decoder.getOutputChannels());
            source.open(fmt);
            source.start();
            volctrl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
            startplay = true;
        }
        byte[] b = toByteArray(samples, offs, len);
        source.write(b, 0, len * 2);
    }

    private byte[] getByteArray(int length) {
        if (byteBuf.length < length) {
            byteBuf = new byte[length + 1024];
        }
        return byteBuf;
    }

    private byte[] toByteArray(short[] samples, int offs, int len) {
        byte[] b = getByteArray(len * 2);
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            b[idx++] = (byte) s;
            b[idx++] = (byte) (s >>> 8);
        }
        return b;
    }
}
