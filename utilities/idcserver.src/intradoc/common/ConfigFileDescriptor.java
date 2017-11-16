package intradoc.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public abstract interface ConfigFileDescriptor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";

  public abstract File getCfgFile(String paramString1, String paramString2);

  public abstract File getCfgFile(String paramString1, String paramString2, boolean paramBoolean);

  public abstract RandomAccessConfigFile getCfgRandomAccess(File paramFile, String paramString)
    throws FileNotFoundException;

  public abstract RandomAccessConfigFile getCfgRandomAccess(String paramString1, String paramString2)
    throws FileNotFoundException;

  public abstract InputStream getCfgInputStream(String paramString)
    throws IOException;

  public abstract InputStream getCfgInputStream(File paramFile)
    throws IOException;

  public abstract Reader getCfgReader(String paramString)
    throws IOException;

  public abstract Reader getCfgReader(File paramFile)
    throws IOException;

  public abstract OutputStream getCfgOutputStream(String paramString1, String paramString2)
    throws FileNotFoundException;

  public abstract OutputStream getCfgOutputStream(String paramString1, String paramString2, boolean paramBoolean)
    throws FileNotFoundException;

  public abstract OutputStream getCfgOutputStream(File paramFile)
    throws FileNotFoundException;

  public abstract OutputStream getCfgOutputStream(File paramFile, boolean paramBoolean)
    throws FileNotFoundException;

  public abstract Writer getCfgWriter(File paramFile)
    throws IOException;

  public abstract Writer getCfgWriter(File paramFile, boolean paramBoolean)
    throws IOException;

  public abstract Writer getCfgWriter(String paramString1, String paramString2)
    throws IOException;

  public abstract Writer getCfgWriter(String paramString1, String paramString2, boolean paramBoolean)
    throws IOException;

  public abstract String getCfgDirectory(String paramString);

  public abstract String getCfgParent(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ConfigFileDescriptor
 * JD-Core Version:    0.5.4
 */