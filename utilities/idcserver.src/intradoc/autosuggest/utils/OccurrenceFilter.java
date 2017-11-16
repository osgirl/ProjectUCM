package intradoc.autosuggest.utils;

import intradoc.autosuggest.records.OccurrenceInfo;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import java.util.Map;

public abstract interface OccurrenceFilter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98407 $";

  public abstract void init(Map<String, Object> paramMap)
    throws DataException, ServiceException;

  public abstract boolean validate(OccurrenceInfo paramOccurrenceInfo)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.OccurrenceFilter
 * JD-Core Version:    0.5.4
 */