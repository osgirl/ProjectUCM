/*    */ package intradoc.gui.iwt.event;
/*    */ 
/*    */ import java.awt.AWTEvent;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.Point;
/*    */ 
/*    */ public class IwtEvent extends AWTEvent
/*    */ {
/*    */   public static final int RAW = 0;
/*    */   public static final int PROCESSED = 1;
/*    */   public static final int HOVER_START = 2000;
/*    */   public static final int HOVER_STOP = 2001;
/*    */   public static final int PAINT = 2002;
/*    */   public static final int POPUP = 2003;
/*    */   public static final int MOUSE_EVENT = 2004;
/*    */   protected int m_category;
/*    */   protected Object m_controllingObject;
/*    */   public Point m_point;
/*    */   public int m_clickCount;
/*    */   public long m_when;
/*    */   public int m_modifiers;
/*    */   public Dimension m_dimension;
/*    */ 
/*    */   public IwtEvent(Object my_source, Object controlling, int category, int type)
/*    */   {
/* 63 */     super(my_source, type);
/* 64 */     this.m_category = category;
/* 65 */     this.m_controllingObject = controlling;
/*    */   }
/*    */ 
/*    */   public int getCategory()
/*    */   {
/* 70 */     return this.m_category;
/*    */   }
/*    */ 
/*    */   public Object getControllingObject()
/*    */   {
/* 75 */     return this.m_controllingObject;
/*    */   }
/*    */ 
/*    */   public boolean isConsumed()
/*    */   {
/* 81 */     return super.isConsumed();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.event.IwtEvent
 * JD-Core Version:    0.5.4
 */