package intradoc.resource;

import intradoc.common.ParseOutput;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Properties;

public abstract interface DynamicDataMerger
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void parse(Reader paramReader, String paramString)
    throws DataException;

  public abstract void write(String paramString1, ParseOutput paramParseOutput, String paramString2)
    throws IOException;

  public abstract Hashtable extractRules();

  public abstract void updateRules(Hashtable paramHashtable);

  public abstract void mergeInto(DataBinder paramDataBinder, Hashtable paramHashtable)
    throws DataException;

  public abstract void mergeFrom(DataBinder paramDataBinder, Properties paramProperties, Hashtable paramHashtable)
    throws ServiceException;

  public abstract String getSourceEncoding();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.DynamicDataMerger
 * JD-Core Version:    0.5.4
 */