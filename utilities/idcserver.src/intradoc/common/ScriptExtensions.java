package intradoc.common;

public abstract interface ScriptExtensions
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void load(ScriptRegistrator paramScriptRegistrator);

  public abstract boolean evaluateFunction(ScriptInfo paramScriptInfo, Object[] paramArrayOfObject, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract boolean evaluateValue(ScriptInfo paramScriptInfo, boolean[] paramArrayOfBoolean, String[] paramArrayOfString, ExecutionContext paramExecutionContext, boolean paramBoolean)
    throws ServiceException;

  public abstract String[] getFunctionTable();

  public abstract int[][] getFunctionDefinitionTable();

  public abstract String[] getVariableTable();

  public abstract int[][] getVariableDefinitionTable();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptExtensions
 * JD-Core Version:    0.5.4
 */