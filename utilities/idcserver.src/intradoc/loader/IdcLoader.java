package intradoc.loader;

import intradoc.io.zip.IdcZipEnvironment;
import intradoc.io.zip.IdcZipFile;
import intradoc.util.GenericTracingCallback;
import java.util.Map;

public abstract interface IdcLoader extends GenericTracingCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71102 $";

  public abstract IdcZipEnvironment getZipEnvironment();

  public abstract Map<String, IdcZipFile> getZipfiles();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoader
 * JD-Core Version:    0.5.4
 */