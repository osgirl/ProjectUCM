package intradoc.gui.iwt;

public abstract interface UserDrawListItem
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 78444 $";

  public abstract void setSelected(boolean paramBoolean);

  public abstract boolean isSelected();

  public abstract int getCurrentIndex();

  public abstract void setCurrentIndex(int paramInt);

  public abstract Object getData();

  public abstract void setData(Object paramObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UserDrawListItem
 * JD-Core Version:    0.5.4
 */