package intradoc.apps.archiver;

import intradoc.data.DataResultSet;
import intradoc.shared.CollectionData;
import intradoc.shared.SharedContext;
import java.util.Properties;
import java.util.Vector;

public abstract interface CollectionContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract SharedContext getSharedContext();

  public abstract String getLocalCollection();

  public abstract CollectionData getCurrentCollection();

  public abstract void loadContext(Properties paramProperties);

  public abstract void loadArchiveData(Properties paramProperties);

  public abstract boolean connectToCollection(int paramInt);

  public abstract DataResultSet getBatchFiles();

  public abstract DataResultSet getBatchMetaSet(boolean paramBoolean, String paramString);

  public abstract DataResultSet getBatchMetaSet(boolean paramBoolean1, String paramString, boolean paramBoolean2);

  public abstract String[][] getBatchFields(boolean paramBoolean, String paramString);

  public abstract String[][] getBatchFields(boolean paramBoolean1, String paramString, boolean paramBoolean2);

  public abstract Vector getBatchValues(String paramString1, String paramString2);

  public abstract Properties getBatchProperties(String paramString);

  public abstract void reportError(String paramString);

  public abstract void reportError(Exception paramException, String paramString);

  public abstract void reportError(Exception paramException);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.CollectionContext
 * JD-Core Version:    0.5.4
 */