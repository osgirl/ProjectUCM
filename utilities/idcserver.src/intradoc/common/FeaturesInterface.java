package intradoc.common;

import intradoc.util.IdcMessage;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract interface FeaturesInterface extends Cloneable, Iterable<Map>
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73849 $";

  public abstract void init();

  public abstract Object getLevel(String paramString);

  public abstract boolean checkLevel(String paramString1, String paramString2);

  public abstract void require(String paramString1, String paramString2, String paramString3)
    throws ServiceException;

  public abstract void refuse(String paramString1, String paramString2, IdcMessage paramIdcMessage)
    throws ServiceException;

  public abstract List getFeatureComponents(String paramString);

  public abstract List getComponentFeatures(String paramString);

  public abstract void registerFeature(String paramString1, String paramString2);

  public abstract Iterator<Map> iterator();

  public abstract Object clone()
    throws CloneNotSupportedException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FeaturesInterface
 * JD-Core Version:    0.5.4
 */