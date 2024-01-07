package com.example.musicdemo.Utils.CustomAudioViews;

class Atom {
    private Atom[] mChildren;
    private byte[] mData;
    private int mFlags;
    private int mSize;
    private int mType;
    private byte mVersion;

    public Atom(String str) {
        this.mSize = 8;
        this.mType = getTypeInt(str);
        this.mData = null;
        this.mChildren = null;
        this.mVersion = (byte) -1;
        this.mFlags = 0;
    }

    public Atom(String str, byte b, int i) {
        this.mSize = 12;
        this.mType = getTypeInt(str);
        this.mData = null;
        this.mChildren = null;
        this.mVersion = b;
        this.mFlags = i;
    }

    private void setSize() {
        int i = this.mVersion >= 0 ? 12 : 8;
        byte[] bArr = this.mData;
        if (bArr != null) {
            i += bArr.length;
        } else {
            Atom[] atomArr = this.mChildren;
            if (atomArr != null) {
                for (Atom atom : atomArr) {
                    i += atom.getSize();
                }
            }
        }
        this.mSize = i;
    }

    public int getSize() {
        return this.mSize;
    }

    private int getTypeInt(String str) {
        return ((byte) str.charAt(3)) | 0 | (((byte) str.charAt(0)) << 24) | (((byte) str.charAt(1)) << 16) | (((byte) str.charAt(2)) << 8);
    }

    public int getTypeInt() {
        return this.mType;
    }

    public String getTypeStr() {
        return ((("" + ((char) ((byte) ((this.mType >> 24) & 255)))) + ((char) ((byte) ((this.mType >> 16) & 255)))) + ((char) ((byte) ((this.mType >> 8) & 255)))) + ((char) ((byte) (this.mType & 255)));
    }

    public boolean setData(byte[] bArr) {
        if (this.mChildren != null || bArr == null) {
            return false;
        }
        this.mData = bArr;
        setSize();
        return true;
    }

    public byte[] getData() {
        return this.mData;
    }

    public boolean addChild(Atom atom) {
        if (this.mData != null || atom == null) {
            return false;
        }
        Atom[] atomArr = this.mChildren;
        int length = atomArr != null ? atomArr.length + 1 : 1;
        Atom[] atomArr2 = new Atom[length];
        if (atomArr != null) {
            System.arraycopy(atomArr, 0, atomArr2, 0, atomArr.length);
        }
        atomArr2[length - 1] = atom;
        this.mChildren = atomArr2;
        setSize();
        return true;
    }

    public Atom getChild(String str) {
        Atom[] atomArr;
        if (this.mChildren == null) {
            return null;
        }
        String[] split = str.split("\\.", 2);
        for (Atom atom : this.mChildren) {
            if (atom.getTypeStr().equals(split[0])) {
                return split.length == 1 ? atom : atom.getChild(split[1]);
            }
        }
        return null;
    }

    public byte[] getBytes() {
        int i = this.mSize;
        byte[] bArr = new byte[i];
        bArr[0] = (byte) ((i >> 24) & 255);
        bArr[1] = (byte) ((i >> 16) & 255);
        bArr[2] = (byte) ((i >> 8) & 255);
        bArr[3] = (byte) (i & 255);
        int i2 = this.mType;
        bArr[4] = (byte) ((i2 >> 24) & 255);
        bArr[5] = (byte) ((i2 >> 16) & 255);
        bArr[6] = (byte) ((i2 >> 8) & 255);
        bArr[7] = (byte) (i2 & 255);
        byte b = this.mVersion;
        int i3 = 8;
        if (b >= 0) {
            bArr[8] = b;
            int i4 = this.mFlags;
            bArr[9] = (byte) ((i4 >> 16) & 255);
            bArr[10] = (byte) ((i4 >> 8) & 255);
            bArr[11] = (byte) (i4 & 255);
            i3 = 12;
        }
        byte[] bArr2 = this.mData;
        if (bArr2 != null) {
            System.arraycopy(bArr2, 0, bArr, i3, bArr2.length);
        } else {
            Atom[] atomArr = this.mChildren;
            if (atomArr != null) {
                for (Atom atom : atomArr) {
                    byte[] bytes = atom.getBytes();
                    System.arraycopy(bytes, 0, bArr, i3, bytes.length);
                    i3 += bytes.length;
                }
            }
        }
        return bArr;
    }

    public String toString() {
        byte[] bytes = getBytes();
        String str = "";
        for (int i = 0; i < bytes.length; i++) {
            int i2 = i % 8;
            if (i2 == 0 && i > 0) {
                str = str + '\n';
            }
            str = str + String.format("0x%02X", Byte.valueOf(bytes[i]));
            if (i < bytes.length - 1) {
                str = str + ',';
                if (i2 < 7) {
                    str = str + ' ';
                }
            }
        }
        return str + '\n';
    }
}
