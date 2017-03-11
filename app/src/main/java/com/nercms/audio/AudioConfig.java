package com.nercms.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioConfig {

    /**
     * Recorder Configure
     */
    public static final int SAMPLERATE = 8000;// 8KHZ
    public static final int PLAYER_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Recorder Configure
     */
    public static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.MIC;
    public static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
}
