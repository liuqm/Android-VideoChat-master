package org.sipdroid.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.widget.Toast;

//blog.csdn.net/zgyulongfei  
//Email: zgyulongfei@gmail.com  

public class AmrAudioEncoder {
    private static final String TAG = "ArmAudioEncoder";

    private static AmrAudioEncoder amrAudioEncoder = null;  //录制音频的管理类

    private Activity activity;  //当前activity

    private MediaRecorder audioRecorder;  //录制音视频的类

    private boolean isAudioRecording;  //判断是否正在录制音频

    private LocalServerSocket lss;
    private LocalSocket sender, receiver;  //接收和发送socket

    private AmrAudioEncoder() {  //单例模式
    }

    public static AmrAudioEncoder getArmAudioEncoderInstance() {  //对象初始化
        if (amrAudioEncoder == null) {
            synchronized (AmrAudioEncoder.class) {
                if (amrAudioEncoder == null) {
                    amrAudioEncoder = new AmrAudioEncoder();
                }
            }
        }
        return amrAudioEncoder;
    }

    public void initArmAudioEncoder(Activity activity) {//初始化相关参数
        this.activity = activity;
        isAudioRecording = false;
    }

    public void start() {  //开始录制
        if (activity == null) {
            showToastText("音频编码器未初始化，请先执行init方法");
            return;
        }

        if (isAudioRecording) {
            showToastText("音频已经开始编码，无需再次编码");
            return;
        }

        if (!initLocalSocket()) {
            showToastText("本地服务开启失败");
            releaseAll();
            return;
        }

        if (!initAudioRecorder()) {
            showToastText("音频编码器初始化失败");
            releaseAll();
            return;
        }

        this.isAudioRecording = true;
        startAudioRecording();
    }

    private boolean initLocalSocket() {
        boolean ret = true;
        try {
            releaseLocalSocket();

            String serverName = "armAudioServer";
            final int bufSize = 1024;

            lss = new LocalServerSocket(serverName);

            receiver = new LocalSocket();
            receiver.connect(new LocalSocketAddress(serverName));
            receiver.setReceiveBufferSize(bufSize);
            receiver.setSendBufferSize(bufSize);

            sender = lss.accept();
            sender.setReceiveBufferSize(bufSize);
            sender.setSendBufferSize(bufSize);
        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }

    private boolean initAudioRecorder() {
        if (audioRecorder != null) {
            audioRecorder.reset();
            audioRecorder.release();
        }
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        final int mono = 1;
        audioRecorder.setAudioChannels(mono);
        audioRecorder.setAudioSamplingRate(8000);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioRecorder.setOutputFile(sender.getFileDescriptor());

        boolean ret = true;
        try {
            audioRecorder.prepare();
            audioRecorder.start();
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
            showToastText("手机不支持录音此功能");
            ret = false;
        }
        return ret;
    }

    private void startAudioRecording() {  //开始录制
        new Thread(new AudioCaptureAndSendThread()).start();
    }

    public void stop() {
        if (isAudioRecording) {
            isAudioRecording = false;
        }
        releaseAll();
    }

    private void releaseAll() {
        releaseMediaRecorder();
        releaseLocalSocket();
        amrAudioEncoder = null;
    }

    private void releaseMediaRecorder() {
        try {
            if (audioRecorder == null) {
                return;
            }
            if (isAudioRecording) {
                audioRecorder.stop();
                isAudioRecording = false;
            }
            audioRecorder.reset();
            audioRecorder.release();
            audioRecorder = null;
        } catch (Exception err) {
            Log.d(TAG, err.toString());
        }
    }

    private void releaseLocalSocket() {
        try {
            if (sender != null) {
                sender.close();
            }
            if (receiver != null) {
                receiver.close();
            }
            if (lss != null) {
                lss.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sender = null;
        receiver = null;
        lss = null;
    }

    private boolean isAudioRecording() {
        return isAudioRecording;
    }

    private void showToastText(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    private class AudioCaptureAndSendThread implements Runnable {
        public void run() {
            try {
                sendAmrAudio();
            } catch (Exception e) {
                Log.e(TAG, "sendAmrAudio() 出错");
            }
        }

        private void sendAmrAudio() throws Exception {
            DatagramSocket udpSocket = new DatagramSocket();
            DataInputStream dataInput = new DataInputStream(receiver.getInputStream());
            skipAmrHead(dataInput);

            final int SEND_FRAME_COUNT_ONE_TIME = 10;// 每次发送10帧的数据，1帧大约32B  
            // AMR格式见博客：http://blog.csdn.net/dinggo/article/details/1966444  
            final int BLOCK_SIZE[] = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0};

            byte[] sendBuffer = new byte[1024];
            while (isAudioRecording()) {
                int offset = 0;
                for (int index = 0; index < SEND_FRAME_COUNT_ONE_TIME; ++index) {
                    if (!isAudioRecording()) {
                        break;
                    }
                    dataInput.read(sendBuffer, offset, 1);
                    int blockIndex = (int) (sendBuffer[offset] >> 3) & 0x0F;
                    int frameLength = BLOCK_SIZE[blockIndex];
                    readSomeData(sendBuffer, offset + 1, frameLength, dataInput);
                    offset += frameLength + 1;
                }
                udpSend(udpSocket, sendBuffer, offset);
            }
            udpSocket.close();
            dataInput.close();
            releaseAll();
        }

        private void skipAmrHead(DataInputStream dataInput) {
            final byte[] AMR_HEAD = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};
            int result = -1;
            int state = 0;
            try {
                while (-1 != (result = dataInput.readByte())) {
                    if (AMR_HEAD[0] == result) {
                        state = (0 == state) ? 1 : 0;
                    } else if (AMR_HEAD[1] == result) {
                        state = (1 == state) ? 2 : 0;
                    } else if (AMR_HEAD[2] == result) {
                        state = (2 == state) ? 3 : 0;
                    } else if (AMR_HEAD[3] == result) {
                        state = (3 == state) ? 4 : 0;
                    } else if (AMR_HEAD[4] == result) {
                        state = (4 == state) ? 5 : 0;
                    } else if (AMR_HEAD[5] == result) {
                        state = (5 == state) ? 6 : 0;
                    }

                    if (6 == state) {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "read mdat error...");
            }
        }

        private void readSomeData(byte[] buffer, int offset, int length, DataInputStream dataInput) {
            int numOfRead = -1;
            while (true) {
                try {
                    numOfRead = dataInput.read(buffer, offset, length);
                    if (numOfRead == -1) {
                        Log.d(TAG, "amr...no data get wait for data coming.....");
                        Thread.sleep(100);
                    } else {
                        offset += numOfRead;
                        length -= numOfRead;
                        if (length <= 0) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "amr..error readSomeData");
                    break;
                }
            }
        }

        private void udpSend(DatagramSocket udpSocket, byte[] buffer, int sendLength) {
            try {
                InetAddress ip = InetAddress.getByName("192.168.1.104");
                int port = 19888;

                byte[] sendBuffer = new byte[sendLength];
                System.arraycopy(buffer, 0, sendBuffer, 0, sendLength);
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendLength);
                packet.setAddress(ip);
                packet.setPort(port);
                udpSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}