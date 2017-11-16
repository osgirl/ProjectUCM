package intradoc.resource;

import intradoc.data.DataException;

public abstract interface ResourceCreator
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract Object createResourceObject(String[] paramArrayOfString, int[] paramArrayOfInt)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceCreator
 * JD-Core Version:    0.5.4
 */