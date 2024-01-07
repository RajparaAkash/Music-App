package com.example.musicdemo.Utils.CustomAudioViews;


public class WAVHeader {
    private int mChannels;
    private byte[] mHeader = null;
    private int mNumBytesPerSample;
    private int mNumSamples;
    private int mSampleRate;

    public WAVHeader(int i, int i2, int i3) {
        this.mSampleRate = i;
        this.mChannels = i2;
        this.mNumSamples = i3;
        this.mNumBytesPerSample = i2 * 2;
        setHeader();
    }

    public byte[] getWAVHeader() {
        return this.mHeader;
    }

    public static byte[] getWAVHeader(int i, int i2, int i3) {
        return new WAVHeader(i, i2, i3).mHeader;
    }

    public String toString() {
        byte[] bArr = this.mHeader;
        String str = "";
        if (bArr == null) {
            return "";
        }
        int i = 0;
        for (byte b : bArr) {
            boolean z = i > 0 && i % 32 == 0;
            boolean z2 = i > 0 && i % 4 == 0 && !z;
            if (z) {
                str = str + '\n';
            }
            if (z2) {
                str = str + ' ';
            }
            str = str + String.format("%02X", Byte.valueOf(b));
            i++;
        }
        return str;
    }

    private void setHeader() {
        byte[] bArr = new byte[46];
        System.arraycopy(new byte[]{82, 73, 70, 70}, 0, bArr, 0, 4);
        int i = (this.mNumSamples * this.mNumBytesPerSample) + 36;
        bArr[4] = (byte) (i & 255);
        bArr[5] = (byte) ((i >> 8) & 255);
        bArr[6] = (byte) ((i >> 16) & 255);
        bArr[7] = (byte) ((i >> 24) & 255);
        System.arraycopy(new byte[]{87, 65, 86, 69}, 0, bArr, 8, 4);
        System.arraycopy(new byte[]{102, 109, 116, 32}, 0, bArr, 12, 4);
        System.arraycopy(new byte[]{16, 0, 0, 0}, 0, bArr, 16, 4);
        System.arraycopy(new byte[]{1, 0}, 0, bArr, 20, 2);
        int i2 = this.mChannels;
        bArr[22] = (byte) (i2 & 255);
        bArr[23] = (byte) ((i2 >> 8) & 255);
        int i3 = this.mSampleRate;
        bArr[24] = (byte) (i3 & 255);
        bArr[25] = (byte) ((i3 >> 8) & 255);
        bArr[26] = (byte) ((i3 >> 16) & 255);
        bArr[27] = (byte) ((i3 >> 24) & 255);
        int i4 = this.mNumBytesPerSample;
        int i5 = i3 * i4;
        bArr[28] = (byte) (i5 & 255);
        bArr[29] = (byte) ((i5 >> 8) & 255);
        bArr[30] = (byte) ((i5 >> 16) & 255);
        bArr[31] = (byte) ((i5 >> 24) & 255);
        bArr[32] = (byte) (i4 & 255);
        bArr[33] = (byte) ((i4 >> 8) & 255);
        System.arraycopy(new byte[]{16, 0}, 0, bArr, 34, 2);
        System.arraycopy(new byte[]{100, 97, 116, 97}, 0, bArr, 36, 4);
        int i6 = this.mNumSamples * this.mNumBytesPerSample;
        bArr[40] = (byte) (i6 & 255);
        bArr[41] = (byte) ((i6 >> 8) & 255);
        bArr[42] = (byte) ((i6 >> 16) & 255);
        bArr[43] = (byte) ((i6 >> 24) & 255);
        this.mHeader = bArr;
    }
}
