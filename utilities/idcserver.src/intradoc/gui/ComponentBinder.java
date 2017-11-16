package intradoc.gui;

public abstract interface ComponentBinder
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";

  public abstract void exchangeComponentValue(DynamicComponentExchange paramDynamicComponentExchange, boolean paramBoolean);

  public abstract boolean validateComponentValue(DynamicComponentExchange paramDynamicComponentExchange);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.ComponentBinder
 * JD-Core Version:    0.5.4
 */