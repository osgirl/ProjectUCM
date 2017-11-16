package intradoc.io;

public abstract interface IdcByteHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73088 $";
  public static final int F_SUPPORTS_READ = 1;
  public static final int F_SUPPORTS_WRITE = 2;
  public static final int F_SUPPORTS_RESIZE = 256;
  public static final int F_SUPPORTS_RANDOM_ACCESS = 512;
  public static final int F_SUPPORTS_SHALLOW_CLONE = 65536;
  public static final int F_IS_DIRTY = 268435456;

  public abstract int getSupportedFeatures();

  public abstract IdcByteHandler shallowClone()
    throws IdcByteHandlerException;

  public abstract IdcByteHandler shallowCloneSubrange(long paramLong1, long paramLong2)
    throws IdcByteHandlerException;

  public abstract long getSize();

  public abstract long getPosition();

  public abstract void setSize(long paramLong)
    throws IdcByteHandlerException;

  public abstract void setPosition(long paramLong)
    throws IdcByteHandlerException;

  public abstract int readNext(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IdcByteHandlerException;

  public abstract int readFrom(long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IdcByteHandlerException;

  public abstract int writeNext(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IdcByteHandlerException;

  public abstract int writeTo(long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IdcByteHandlerException;

  public abstract void markIsDirty(boolean paramBoolean)
    throws IdcByteHandlerException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteHandler
 * JD-Core Version:    0.5.4
 */