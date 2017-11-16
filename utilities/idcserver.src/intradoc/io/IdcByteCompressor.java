package intradoc.io;

public abstract interface IdcByteCompressor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72004 $";

  public abstract long compress(IdcByteHandler paramIdcByteHandler1, IdcByteHandler paramIdcByteHandler2)
    throws IdcByteHandlerException;

  public abstract long decompress(IdcByteHandler paramIdcByteHandler1, IdcByteHandler paramIdcByteHandler2)
    throws IdcByteHandlerException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteCompressor
 * JD-Core Version:    0.5.4
 */