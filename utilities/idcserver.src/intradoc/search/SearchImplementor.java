package intradoc.search;

import intradoc.common.ExecutionContext;
import intradoc.data.DataBinder;
import java.util.Vector;

public abstract interface SearchImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86052 $";

  public abstract void init(CommonSearchConnection paramCommonSearchConnection);

  public abstract boolean prepareUse(ExecutionContext paramExecutionContext);

  public abstract boolean initCollection(Vector paramVector);

  public abstract String doQuery(DataBinder paramDataBinder);

  public abstract String retrieveHighlightInfo(DataBinder paramDataBinder, int paramInt, String paramString1, String paramString2);

  public abstract String viewDoc(DataBinder paramDataBinder, int paramInt);

  public abstract String retrieveDocInfo(String paramString1, String paramString2, int paramInt);

  public abstract String getResult();

  public abstract DataBinder getResultAsBinder();

  public abstract void closeSession();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchImplementor
 * JD-Core Version:    0.5.4
 */