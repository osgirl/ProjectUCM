package intradoc.common;

import java.util.Map;

public abstract interface OSSettingsHelper
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95962 $";
  public static final String F_NO_SEARCH = "no_search";
  public static final String TYPE_DIRECTORY = "type_directory";
  public static final String TYPE_GENERIC = "type_generic";
  public static final String TYPE_EXECUTABLE = "type_executable";
  public static final String TYPE_SHARED_OBJECT = "type_sharedobject";
  public static final String TYPE_JNI_OBJECT = "type_jniobject";
  public static final String TYPE_WEBSERVER_OBJECT = "type_webserverobject";
  public static final String BASE_DIRECTORY = "base_directory";

  public abstract Map normalizeOSPath(String paramString, Map paramMap)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.OSSettingsHelper
 * JD-Core Version:    0.5.4
 */