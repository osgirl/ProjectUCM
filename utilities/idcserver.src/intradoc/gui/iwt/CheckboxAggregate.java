/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.gui.AggregateImplementor;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class CheckboxAggregate
/*     */   implements AggregateImplementor
/*     */ {
/*     */   public static final int NO_TYPE = 0;
/*     */   public static final int TEXT_TYPE = 1;
/*     */   public static final int CHOICE_TYPE = 2;
/*     */   public static final int COMBO_TYPE = 3;
/*  48 */   public int m_type = 0;
/*  49 */   public JCheckBox m_checkbox = null;
/*  50 */   public Vector m_buddies = null;
/*  51 */   public JButton m_browseBtn = null;
/*  52 */   public String m_id = "";
/*     */ 
/*     */   public CheckboxAggregate(String caption, int type)
/*     */   {
/*  56 */     init(caption, type, 1, null, false);
/*     */   }
/*     */ 
/*     */   public CheckboxAggregate(String caption, int type, int numBuds)
/*     */   {
/*  61 */     init(caption, type, numBuds, null, false);
/*     */   }
/*     */ 
/*     */   public CheckboxAggregate(String caption, int type, ButtonGroup grp, boolean state)
/*     */   {
/*  66 */     init(caption, type, 1, grp, state);
/*     */   }
/*     */ 
/*     */   public CheckboxAggregate(String caption, int type, int numBuds, ButtonGroup grp, boolean state)
/*     */   {
/*  71 */     init(caption, type, numBuds, grp, state);
/*     */   }
/*     */ 
/*     */   protected void init(String caption, int type, int numBuds, ButtonGroup grp, boolean state)
/*     */   {
/*  76 */     if (grp != null)
/*     */     {
/*  78 */       this.m_checkbox = new JCheckBox(caption, state);
/*  79 */       grp.add(this.m_checkbox);
/*     */     }
/*     */     else
/*     */     {
/*  83 */       this.m_checkbox = new JCheckBox(caption);
/*     */     }
/*     */ 
/*  86 */     this.m_type = type;
/*  87 */     this.m_buddies = new IdcVector();
/*     */ 
/*  89 */     switch (type)
/*     */     {
/*     */     case 1:
/*  92 */       for (int i = 0; i < numBuds; ++i)
/*     */       {
/*  94 */         this.m_buddies.addElement(new CustomTextField(10));
/*     */       }
/*  96 */       break;
/*     */     case 2:
/*  99 */       for (int i = 0; i < numBuds; ++i)
/*     */       {
/* 101 */         this.m_buddies.addElement(new DisplayChoice());
/*     */       }
/* 103 */       break;
/*     */     case 3:
/* 105 */       for (int i = 0; i < numBuds; ++i)
/*     */       {
/* 107 */         this.m_buddies.addElement(new ComboChoice());
/*     */       }
/*     */     }
/*     */ 
/* 111 */     addListener();
/*     */   }
/*     */ 
/*     */   public void initChoice(String[][] display, int index)
/*     */   {
/* 116 */     Object comp = this.m_buddies.elementAt(index);
/* 117 */     switch (this.m_type)
/*     */     {
/*     */     case 2:
/* 120 */       DisplayChoice choice = (DisplayChoice)comp;
/* 121 */       choice.init(display);
/* 122 */       break;
/*     */     case 3:
/* 124 */       ComboChoice combo = (ComboChoice)comp;
/* 125 */       combo.initChoiceList(display);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initChoice(Vector optList, int index)
/*     */   {
/* 132 */     Object comp = this.m_buddies.elementAt(index);
/* 133 */     switch (this.m_type)
/*     */     {
/*     */     case 2:
/* 136 */       DisplayChoice choice = (DisplayChoice)comp;
/* 137 */       choice.init(optList);
/* 138 */       break;
/*     */     case 3:
/* 140 */       ComboChoice combo = (ComboChoice)comp;
/* 141 */       combo.initChoiceList(optList);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addBrowseButton(String caption, String cmd, ActionListener al)
/*     */   {
/* 148 */     if ((caption == null) || (caption.length() == 0))
/*     */     {
/* 150 */       caption = "...";
/*     */     }
/* 152 */     this.m_browseBtn = new JButton(caption);
/* 153 */     this.m_browseBtn.setActionCommand(cmd);
/* 154 */     this.m_browseBtn.addActionListener(al);
/*     */   }
/*     */ 
/*     */   public void addListener()
/*     */   {
/* 159 */     ItemListener checkListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 163 */         if (CheckboxAggregate.this.m_buddies == null)
/*     */         {
/* 165 */           return;
/*     */         }
/*     */ 
/* 168 */         boolean isSelected = false;
/* 169 */         if (CheckboxAggregate.this.m_checkbox.isSelected())
/*     */         {
/* 171 */           isSelected = true;
/*     */         }
/*     */ 
/* 174 */         CheckboxAggregate.this.setEnabled(isSelected);
/*     */       }
/*     */     };
/* 177 */     this.m_checkbox.addItemListener(checkListener);
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean isSelected)
/*     */   {
/* 182 */     int size = this.m_buddies.size();
/* 183 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 185 */       Component buddy = (Component)this.m_buddies.elementAt(i);
/* 186 */       if (buddy instanceof JTextField)
/*     */       {
/* 188 */         JTextField textField = (JTextField)buddy;
/* 189 */         textField.setEditable(isSelected);
/*     */       }
/*     */       else
/*     */       {
/* 193 */         buddy.setEnabled(isSelected);
/*     */       }
/*     */     }
/*     */ 
/* 197 */     if (this.m_browseBtn == null)
/*     */       return;
/* 199 */     this.m_browseBtn.setEnabled(isSelected);
/*     */   }
/*     */ 
/*     */   public void setBuddyValue(boolean isSelected, String value, int index)
/*     */   {
/* 205 */     if (value == null)
/*     */     {
/* 207 */       return;
/*     */     }
/*     */ 
/* 210 */     Object buddy = this.m_buddies.elementAt(index);
/* 211 */     if (buddy instanceof JTextField)
/*     */     {
/* 213 */       JTextField textField = (JTextField)buddy;
/* 214 */       textField.setText(value);
/* 215 */       textField.setEditable(isSelected);
/*     */     }
/* 217 */     else if (buddy instanceof JComboBox)
/*     */     {
/* 219 */       JComboBox choice = (JComboBox)buddy;
/* 220 */       choice.setSelectedItem(value);
/* 221 */       choice.setEnabled(isSelected);
/*     */     } else {
/* 223 */       if (!buddy instanceof ComboChoice)
/*     */         return;
/* 225 */       ComboChoice combo = (ComboChoice)buddy;
/* 226 */       combo.select(value);
/* 227 */       combo.setEnabled(isSelected);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setData(boolean isSelected, String value, int index)
/*     */   {
/* 233 */     this.m_checkbox.setSelected(isSelected);
/* 234 */     if (this.m_buddies == null)
/*     */     {
/* 236 */       return;
/*     */     }
/*     */ 
/* 239 */     setBuddyValue(isSelected, value, index);
/*     */   }
/*     */ 
/*     */   public void setData(boolean isSelected, Vector values)
/*     */   {
/* 244 */     this.m_checkbox.setSelected(isSelected);
/* 245 */     if (this.m_buddies == null)
/*     */     {
/* 247 */       return;
/*     */     }
/*     */ 
/* 250 */     int size = this.m_buddies.size();
/* 251 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 253 */       String value = "";
/* 254 */       if (values != null)
/*     */       {
/* 256 */         value = (String)values.elementAt(i);
/*     */       }
/* 258 */       setBuddyValue(isSelected, value, i);
/*     */     }
/*     */ 
/* 261 */     if (this.m_browseBtn == null)
/*     */       return;
/* 263 */     this.m_browseBtn.setEnabled(isSelected);
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener il)
/*     */   {
/* 269 */     this.m_checkbox.addItemListener(il);
/*     */   }
/*     */ 
/*     */   public String getId()
/*     */   {
/* 274 */     return this.m_id;
/*     */   }
/*     */ 
/*     */   public void setId(String id)
/*     */   {
/* 279 */     this.m_id = id;
/*     */   }
/*     */ 
/*     */   public Component getComponent()
/*     */   {
/* 287 */     return this.m_checkbox;
/*     */   }
/*     */ 
/*     */   public Component getBuddy(int index)
/*     */   {
/* 292 */     Component buddy = (Component)this.m_buddies.elementAt(index);
/* 293 */     return buddy;
/*     */   }
/*     */ 
/*     */   public Vector getBuddies()
/*     */   {
/* 298 */     return this.m_buddies;
/*     */   }
/*     */ 
/*     */   public void removeBuddy()
/*     */   {
/* 303 */     this.m_buddies = null;
/*     */   }
/*     */ 
/*     */   public void addBuddy(Component cmp)
/*     */   {
/* 308 */     this.m_buddies.addElement(cmp);
/*     */   }
/*     */ 
/*     */   public JButton getBrowseButton()
/*     */   {
/* 313 */     return this.m_browseBtn;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 318 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.CheckboxAggregate
 * JD-Core Version:    0.5.4
 */