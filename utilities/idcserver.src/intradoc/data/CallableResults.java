package intradoc.data;

import java.io.InputStream;
import java.io.Reader;

public abstract interface CallableResults
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80660 $";

  public abstract String getString(String paramString)
    throws DataException;

  public abstract int getInteger(String paramString)
    throws DataException;

  public abstract long getLong(String paramString)
    throws DataException;

  public abstract InputStream getBinaryInputStream(String paramString)
    throws DataException;

  public abstract Reader getReader(String paramString)
    throws DataException;

  public abstract Object getObject(String paramString)
    throws DataException;

  public abstract ResultSet getResultSet()
    throws DataException;

  public abstract Object next();

  public abstract void close();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.CallableResults
 * JD-Core Version:    0.5.4
 */