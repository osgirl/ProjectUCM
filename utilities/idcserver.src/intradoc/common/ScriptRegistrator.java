package intradoc.common;

public abstract interface ScriptRegistrator
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void registerEvalFunction(ScriptInfo paramScriptInfo);

  public abstract void registerEvalVariable(ScriptInfo paramScriptInfo);

  public abstract void registerExtension(ScriptExtensions paramScriptExtensions);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptRegistrator
 * JD-Core Version:    0.5.4
 */