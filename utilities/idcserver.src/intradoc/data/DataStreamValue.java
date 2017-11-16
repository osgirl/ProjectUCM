package intradoc.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public abstract interface DataStreamValue
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract InputStream getDataStream(String paramString)
    throws DataException, IOException;

  public abstract Reader getCharacterReader(String paramString)
    throws DataException, IOException;

  public abstract Object updateBlob(String paramString, File paramFile)
    throws DataException, IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataStreamValue
 * JD-Core Version:    0.5.4
 */