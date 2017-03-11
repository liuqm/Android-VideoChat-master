package com.nercms.audio;

public class AudioData
{
    int size;
    short[] realData;

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public short[] getRealData()
    {
        return realData;
    }

    public void setRealData(short[] realData)
    {
        this.realData = realData;
    }
}
