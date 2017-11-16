/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JToggleButton;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class DynamicComponentExchange
/*     */ {
/*     */   public SystemInterface m_sysInterface;
/*     */   public Vector m_componentMap;
/*     */   public Object m_currentObject;
/*     */   public String m_compName;
/*     */   public String m_compValue;
/*     */   public Component m_component;
/*     */   public Object m_extraInfo;
/*     */   public IdcMessage m_errorMessage;
/*     */   public boolean m_useMsgBox;
/*     */ 
/*     */   public DynamicComponentExchange()
/*     */   {
/*  80 */     this.m_errorMessage = null;
/*  81 */     this.m_compValue = null;
/*  82 */     this.m_compName = null;
/*  83 */     this.m_useMsgBox = true;
/*  84 */     this.m_componentMap = new IdcVector();
/*  85 */     this.m_currentObject = null;
/*     */   }
/*     */ 
/*     */   public void addComponent(String id, Component comp, Object extraInfo)
/*     */   {
/*  91 */     Object[] objs = { id, comp, extraInfo, null };
/*  92 */     this.m_componentMap.addElement(objs);
/*     */   }
/*     */ 
/*     */   public Object[] loadComponent(String id)
/*     */   {
/* 100 */     Object[] objs = findComponent(id, false);
/* 101 */     if (objs == null)
/*     */     {
/* 103 */       return null;
/*     */     }
/* 105 */     this.m_compName = id;
/* 106 */     this.m_component = ((Component)objs[1]);
/* 107 */     this.m_extraInfo = objs[2];
/* 108 */     this.m_compValue = ((String)objs[3]);
/* 109 */     return objs;
/*     */   }
/*     */ 
/*     */   public boolean removeComponent(String id)
/*     */   {
/* 116 */     return findComponent(id, true) != null;
/*     */   }
/*     */ 
/*     */   public boolean replaceComponent(String id, Component comp)
/*     */   {
/* 124 */     Object[] objs = findComponent(id, false);
/* 125 */     if (objs == null)
/*     */     {
/* 127 */       return false;
/*     */     }
/* 129 */     objs[1] = comp;
/* 130 */     return true;
/*     */   }
/*     */ 
/*     */   public Object[] findComponent(String id, boolean remove)
/*     */   {
/* 135 */     int len = this.m_componentMap.size();
/* 136 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 138 */       Object[] objs = (Object[])(Object[])this.m_componentMap.elementAt(i);
/* 139 */       String compId = (String)objs[0];
/* 140 */       if (!compId.equals(id))
/*     */         continue;
/* 142 */       if (remove)
/*     */       {
/* 144 */         this.m_componentMap.removeElementAt(i);
/*     */       }
/* 146 */       return objs;
/*     */     }
/*     */ 
/* 149 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean exchange(ComponentBinder binder, boolean updateComponents)
/*     */   {
/* 164 */     int len = this.m_componentMap.size();
/* 165 */     Object[] objs = null;
/*     */ 
/* 167 */     if (!updateComponents)
/*     */     {
/* 170 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 172 */         objs = loadComponentAt(i);
/* 173 */         exchangeComponent(objs, false);
/* 174 */         if ((binder.validateComponentValue(this)) || 
/* 176 */           (!this.m_useMsgBox))
/*     */           continue;
/* 178 */         if (this.m_errorMessage == null)
/*     */         {
/* 180 */           this.m_errorMessage = this.m_sysInterface.getValidationErrorMessageObject(this.m_compName, this.m_compValue, null);
/*     */ 
/* 182 */           if (this.m_errorMessage == null)
/*     */           {
/* 184 */             this.m_errorMessage = IdcMessageFactory.lc(GuiText.m_invalidValue, new Object[] { this.m_compName });
/*     */           }
/*     */         }
/* 187 */         MessageBox.reportError(this.m_sysInterface, this.m_errorMessage);
/* 188 */         this.m_component.requestFocus();
/* 189 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 196 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 198 */       objs = loadComponentAt(i);
/* 199 */       if (objs[3] == null)
/*     */       {
/* 201 */         objs[3] = "";
/* 202 */         this.m_compValue = "";
/*     */       }
/* 204 */       binder.exchangeComponentValue(this, updateComponents);
/* 205 */       if (updateComponents != true)
/*     */         continue;
/* 207 */       exchangeComponent(objs, true);
/*     */     }
/*     */ 
/* 211 */     return true;
/*     */   }
/*     */ 
/*     */   protected Object[] loadComponentAt(int index)
/*     */   {
/* 217 */     Object[] objs = (Object[])(Object[])this.m_componentMap.elementAt(index);
/* 218 */     this.m_compName = ((String)objs[0]);
/* 219 */     this.m_component = ((Component)objs[1]);
/* 220 */     this.m_extraInfo = objs[2];
/* 221 */     this.m_compValue = ((String)objs[3]);
/* 222 */     return objs;
/*     */   }
/*     */ 
/*     */   public void exchangeComponent(Object[] objs, boolean updateComponent)
/*     */   {
/* 228 */     Component comp = this.m_component;
/*     */ 
/* 230 */     if ((updateComponent) && (this.m_compValue == null))
/*     */     {
/* 232 */       this.m_compValue = "";
/*     */     }
/*     */ 
/* 237 */     if (comp instanceof JButton)
/*     */     {
/* 239 */       JButton btn = (JButton)comp;
/* 240 */       if (updateComponent)
/*     */       {
/* 242 */         btn.setText(this.m_compValue);
/*     */       }
/*     */       else
/*     */       {
/* 246 */         this.m_compValue = btn.getText();
/*     */       }
/*     */     }
/* 249 */     else if (comp instanceof JToggleButton)
/*     */     {
/* 251 */       JToggleButton chkbox = (JToggleButton)comp;
/* 252 */       if (updateComponent)
/*     */       {
/* 254 */         chkbox.setSelected(StringUtils.convertToBool(this.m_compValue, false));
/*     */       }
/*     */       else
/*     */       {
/* 258 */         this.m_compValue = ((chkbox.isSelected() == true) ? "1" : "0");
/*     */       }
/*     */     }
/* 261 */     else if (comp instanceof DisplayChoice)
/*     */     {
/* 263 */       DisplayChoice chc = (DisplayChoice)comp;
/* 264 */       if (updateComponent)
/*     */       {
/* 266 */         String currentValue = chc.getSelectedInternalValue();
/* 267 */         if ((null != this.m_compValue) && (null != currentValue) && (!currentValue.equals(this.m_compValue)))
/*     */         {
/* 270 */           chc.setSelectedItem(this.m_compValue);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 275 */         this.m_compValue = chc.getSelectedInternalValue();
/*     */       }
/*     */     }
/* 278 */     else if (comp instanceof JComboBox)
/*     */     {
/* 280 */       JComboBox chc = (JComboBox)comp;
/* 281 */       if (updateComponent)
/*     */       {
/* 283 */         String currentValue = (String)chc.getSelectedItem();
/* 284 */         if ((null != this.m_compValue) && (null != currentValue) && (!currentValue.equals(this.m_compValue)))
/*     */         {
/* 287 */           chc.setSelectedItem(this.m_compValue);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 292 */         this.m_compValue = ((String)chc.getSelectedItem());
/*     */       }
/*     */     }
/* 295 */     else if (comp instanceof DisplayLabel)
/*     */     {
/* 297 */       DisplayLabel lbl = (DisplayLabel)comp;
/* 298 */       if (updateComponent)
/*     */       {
/* 300 */         lbl.setTextInternal(this.m_compValue);
/*     */       }
/*     */       else
/*     */       {
/* 304 */         this.m_compValue = lbl.getTextInternal();
/*     */       }
/*     */     }
/* 307 */     else if (comp instanceof JLabel)
/*     */     {
/* 309 */       JLabel lbl = (JLabel)comp;
/* 310 */       if (updateComponent)
/*     */       {
/* 312 */         if (this.m_compValue.length() == 0)
/*     */         {
/* 318 */           lbl.setText(" ");
/*     */         }
/*     */         else
/*     */         {
/* 322 */           lbl.setText(this.m_compValue);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 327 */         this.m_compValue = lbl.getText();
/*     */       }
/*     */     }
/* 330 */     else if (comp instanceof JList)
/*     */     {
/* 332 */       JList lst = (JList)comp;
/* 333 */       if (updateComponent)
/*     */       {
/* 335 */         int index = Integer.parseInt(this.m_compValue);
/* 336 */         lst.setSelectedIndex(index);
/*     */       }
/*     */       else
/*     */       {
/* 340 */         this.m_compValue = Integer.toString(lst.getSelectedIndex());
/*     */       }
/*     */     }
/* 343 */     else if (comp instanceof JTextComponent)
/*     */     {
/* 345 */       JTextComponent txc = (JTextComponent)comp;
/* 346 */       if (updateComponent)
/*     */       {
/* 348 */         txc.setText(this.m_compValue);
/*     */       }
/*     */       else
/*     */       {
/* 352 */         this.m_compValue = txc.getText();
/*     */       }
/*     */     }
/* 355 */     else if (comp instanceof TextHandler)
/*     */     {
/* 357 */       TextHandler th = (TextHandler)comp;
/* 358 */       if (updateComponent)
/*     */       {
/* 360 */         th.setText(this.m_compValue);
/*     */       }
/*     */       else
/*     */       {
/* 364 */         this.m_compValue = th.getText();
/*     */       }
/*     */     }
/* 367 */     else if (comp instanceof CheckboxPanel)
/*     */     {
/* 369 */       CheckboxPanel panel = (CheckboxPanel)comp;
/* 370 */       if (updateComponent)
/*     */       {
/* 372 */         panel.setState(this.m_compValue);
/*     */       }
/*     */       else
/*     */       {
/* 376 */         this.m_compValue = panel.getState();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 382 */     objs[3] = this.m_compValue;
/*     */   }
/*     */ 
/*     */   public boolean setComponentValue(String compId, String val)
/*     */   {
/* 390 */     Object[] objs = loadComponent(compId);
/* 391 */     if (objs == null)
/*     */     {
/* 393 */       return false;
/*     */     }
/*     */ 
/* 396 */     this.m_compValue = val;
/* 397 */     exchangeComponent(objs, true);
/* 398 */     return true;
/*     */   }
/*     */ 
/*     */   public String getComponentValue(String compId)
/*     */   {
/* 403 */     Object[] objs = loadComponent(compId);
/* 404 */     if (objs == null)
/*     */     {
/* 406 */       return null;
/*     */     }
/*     */ 
/* 409 */     exchangeComponent(objs, false);
/* 410 */     return this.m_compValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 415 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DynamicComponentExchange
 * JD-Core Version:    0.5.4
 */