/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DisplayChoice extends CustomChoice
/*     */ {
/*  30 */   protected Vector m_keys = new IdcVector();
/*  31 */   protected Vector m_displayStrings = new IdcVector();
/*  32 */   public String m_defaultDisplayStr = null;
/*     */ 
/*     */   public DisplayChoice()
/*     */   {
/*  37 */     setEnabled(false);
/*     */   }
/*     */ 
/*     */   public DisplayChoice(String defaultDisplayStr)
/*     */   {
/*  42 */     setEnabled(false);
/*  43 */     this.m_defaultDisplayStr = defaultDisplayStr;
/*     */   }
/*     */ 
/*     */   public void init(String[][] display)
/*     */   {
/*  48 */     removeAllItems();
/*     */ 
/*  50 */     if (display == null)
/*     */     {
/*  52 */       return;
/*     */     }
/*     */ 
/*  55 */     int size = display.length;
/*  56 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  58 */       addItem(display[i][1], display[i][0]);
/*     */     }
/*     */ 
/*  61 */     boolean isEnabled = size > 0;
/*  62 */     setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void init(Vector options)
/*     */   {
/*  67 */     removeAllItems();
/*     */ 
/*  69 */     if (options == null)
/*     */     {
/*  71 */       return;
/*     */     }
/*     */ 
/*  74 */     int size = options.size();
/*  75 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  77 */       addItem(options.elementAt(i));
/*     */     }
/*     */ 
/*  80 */     boolean isEnabled = size > 0;
/*  81 */     setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean isEnabled)
/*     */   {
/*  87 */     super.setEnabled(isEnabled);
/*     */ 
/*  89 */     if ((getItemCount() != 0) || (this.m_defaultDisplayStr == null))
/*     */       return;
/*  91 */     addItem(this.m_defaultDisplayStr, "");
/*     */   }
/*     */ 
/*     */   public void addItem(Object displayString)
/*     */   {
/*  98 */     addItem(displayString, displayString);
/*     */   }
/*     */ 
/*     */   public void addItem(Object displayString, Object key)
/*     */   {
/* 103 */     super.addItem(displayString);
/* 104 */     this.m_keys.addElement(key);
/* 105 */     this.m_displayStrings.addElement(displayString);
/*     */   }
/*     */ 
/*     */   public void select(String key)
/*     */   {
/* 112 */     String displayValue = key;
/* 113 */     int index = this.m_keys.indexOf(key);
/* 114 */     if (index >= 0)
/*     */     {
/* 116 */       super.setSelectedItem(this.m_displayStrings.elementAt(index));
/*     */     }
/*     */     else
/*     */     {
/* 120 */       super.setSelectedItem(displayValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setSelectedItem(Object anObject)
/*     */   {
/* 127 */     String key = (String)anObject;
/* 128 */     int index = this.m_keys.indexOf(key);
/* 129 */     if (index >= 0)
/*     */     {
/* 131 */       super.setSelectedItem(this.m_displayStrings.elementAt(index));
/*     */     }
/*     */     else
/*     */     {
/* 135 */       super.setSelectedItem(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void selectIgnoreCase(String key)
/*     */   {
/* 142 */     for (int i = 0; i < this.m_keys.size(); ++i)
/*     */     {
/* 144 */       if (!((String)this.m_keys.get(i)).equalsIgnoreCase(key))
/*     */         continue;
/* 146 */       super.setSelectedItem(this.m_displayStrings.elementAt(i));
/* 147 */       return;
/*     */     }
/*     */ 
/* 152 */     for (int i = 0; i < this.m_displayStrings.size(); ++i)
/*     */     {
/* 154 */       if (!((String)this.m_displayStrings.get(i)).equalsIgnoreCase(key))
/*     */         continue;
/* 156 */       super.setSelectedItem(this.m_displayStrings.elementAt(i));
/* 157 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getSelectedInternalValue()
/*     */   {
/* 168 */     if (this.m_keys.size() == 0)
/*     */     {
/* 170 */       return null;
/*     */     }
/*     */ 
/* 174 */     String value = null;
/*     */ 
/* 179 */     int index = getSelectedIndex();
/* 180 */     if (index >= 0)
/*     */     {
/* 182 */       value = (String)this.m_keys.elementAt(index);
/*     */     }
/*     */     else
/*     */     {
/* 186 */       value = (String)super.getSelectedItem();
/*     */     }
/*     */ 
/* 189 */     return value;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void removeAll()
/*     */   {
/* 196 */     removeAllItems();
/*     */   }
/*     */ 
/*     */   public void removeAllItems()
/*     */   {
/* 202 */     super.removeAllItems();
/* 203 */     this.m_keys.removeAllElements();
/* 204 */     this.m_displayStrings.removeAllElements();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 209 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97523 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DisplayChoice
 * JD-Core Version:    0.5.4
 */