package intradoc.taskmanager;

public abstract interface TaskWork
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82510 $";

  public abstract void finishWork(TaskInfo paramTaskInfo);

  public abstract void cleanUp();

  public abstract String[] startWork();

  public abstract TaskInfo stopCommand(TaskInfo paramTaskInfo);

  public abstract boolean isSuccessful(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskWork
 * JD-Core Version:    0.5.4
 */