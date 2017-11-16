package intradoc.io.zip;

import intradoc.io.IdcByteCompressor;

public abstract interface IdcZipCompressor extends IdcByteCompressor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72004 $";

  public abstract void init(IdcZipEnvironment paramIdcZipEnvironment);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipCompressor
 * JD-Core Version:    0.5.4
 */