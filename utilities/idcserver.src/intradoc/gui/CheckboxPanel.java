/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JRadioButton;
/*     */ 
/*     */ public class CheckboxPanel extends PanePanel
/*     */   implements ItemListener
/*     */ {
/*     */   protected Object[][] m_options;
/*     */   protected JRadioButton[] m_checkboxes;
/*     */   protected CheckboxPanel[] m_subPanels;
/*     */   protected boolean[] m_enabledFlags;
/*     */   protected boolean[] m_visibleFlags;
/*     */   protected boolean[] m_defaultOnFlags;
/*     */   protected char m_separator;
/*     */   protected Hashtable m_checkboxGroups;
/*     */   protected Vector m_checkboxGroupsList;
/*     */   protected SystemInterface m_sysInterface;
/*     */ 
/*     */   public CheckboxPanel()
/*     */   {
/*  46 */     this.m_separator = ':';
/*  47 */     this.m_checkboxGroups = new Hashtable();
/*  48 */     this.m_checkboxGroupsList = new IdcVector();
/*  49 */     this.m_sysInterface = null;
/*     */   }
/*     */ 
/*     */   public void init(Object[][] options, boolean horizontal, SystemInterface sysInterface)
/*     */   {
/*  58 */     this.m_options = options;
/*  59 */     this.m_checkboxes = new JRadioButton[this.m_options.length];
/*  60 */     this.m_enabledFlags = new boolean[this.m_options.length];
/*  61 */     this.m_visibleFlags = new boolean[this.m_options.length];
/*  62 */     this.m_defaultOnFlags = new boolean[this.m_options.length];
/*  63 */     this.m_subPanels = new CheckboxPanel[this.m_options.length];
/*  64 */     this.m_sysInterface = sysInterface;
/*  65 */     int rowCount = 0;
/*     */ 
/*  67 */     GridBagHelper helper = new GridBagHelper();
/*  68 */     helper.useGridBag(this);
/*     */ 
/*  70 */     for (int i = 0; i < this.m_options.length; ++i)
/*     */     {
/*  72 */       Object[] boxRow = this.m_options[i];
/*  73 */       boolean checked = false;
/*  74 */       boolean horizontalChildren = false;
/*     */ 
/*  76 */       this.m_enabledFlags[i] = true;
/*  77 */       this.m_visibleFlags[i] = true;
/*  78 */       this.m_defaultOnFlags[i] = false;
/*  79 */       if ((boxRow.length > 2) && (boxRow[2] != null))
/*     */       {
/*  81 */         String flags = (String)boxRow[2];
/*  82 */         this.m_enabledFlags[i] = ((flags.indexOf("D") < 0) ? 1 : false);
/*  83 */         this.m_visibleFlags[i] = ((flags.indexOf("I") < 0) ? 1 : false);
/*  84 */         checked = flags.indexOf("C") >= 0;
/*  85 */         this.m_defaultOnFlags[i] = checked;
/*  86 */         horizontalChildren = flags.indexOf("H") >= 0;
/*     */       }
/*     */ 
/*  89 */       String label = (this.m_sysInterface != null) ? this.m_sysInterface.localizeMessage((String)boxRow[1]) : LocaleResources.localizeMessage((String)boxRow[1], null);
/*     */ 
/*  91 */       JRadioButton box = new JRadioButton(label, checked);
/*  92 */       box.addItemListener(this);
/*  93 */       this.m_checkboxes[i] = box;
/*  94 */       JPanel wrapperPanel = new PanePanel();
/*  95 */       FlowLayout layout = new FlowLayout();
/*  96 */       layout.setAlignment(0);
/*  97 */       wrapperPanel.setLayout(layout);
/*  98 */       wrapperPanel.add(box);
/*  99 */       if (horizontal)
/*     */       {
/* 101 */         helper.prepareAddRowElement(17, 1);
/*     */       }
/*     */       else
/*     */       {
/* 105 */         helper.prepareAddRowElement(17, 0);
/*     */       }
/*     */ 
/* 108 */       if (this.m_visibleFlags[i] != 0)
/*     */       {
/* 110 */         add(box, helper.m_gc);
/*     */       }
/* 112 */       ++rowCount;
/*     */ 
/* 114 */       if ((boxRow.length > 3) && (boxRow[3] != null))
/*     */       {
/* 116 */         String groupName = (String)boxRow[3];
/* 117 */         Object[] groupArray = (Object[])(Object[])this.m_checkboxGroups.get(groupName);
/* 118 */         if (groupArray == null)
/*     */         {
/* 120 */           groupArray = new Object[2];
/* 121 */           groupArray[0] = new ButtonGroup();
/* 122 */           groupArray[1] = new IdcVector();
/* 123 */           this.m_checkboxGroups.put(groupName, groupArray);
/* 124 */           this.m_checkboxGroupsList.addElement(groupArray);
/*     */         }
/* 126 */         ((ButtonGroup)groupArray[0]).add(box);
/* 127 */         ((Vector)groupArray[1]).addElement(box);
/*     */       }
/*     */ 
/* 130 */       if ((boxRow.length > 4) && (boxRow[4] != null))
/*     */       {
/* 132 */         CheckboxPanel subPanel = new CheckboxPanel();
/* 133 */         subPanel.init((Object[][])(Object[][])boxRow[4], horizontalChildren, this.m_sysInterface);
/* 134 */         this.m_subPanels[i] = subPanel;
/* 135 */         Insets insets = helper.m_gc.insets;
/* 136 */         helper.m_gc.insets = new Insets(0, 20, 0, 1);
/* 137 */         if (this.m_visibleFlags[i] != 0)
/*     */         {
/* 139 */           add(subPanel, helper.m_gc);
/*     */         }
/* 141 */         helper.m_gc.insets = insets;
/* 142 */         ++rowCount;
/*     */       }
/*     */ 
/* 145 */       if (this.m_enabledFlags[i] != 0)
/*     */         continue;
/* 147 */       box.setEnabled(false);
/* 148 */       if (this.m_subPanels[i] == null)
/*     */         continue;
/* 150 */       this.m_subPanels[i].setEnabled(false);
/*     */     }
/*     */ 
/* 155 */     int size = this.m_checkboxGroupsList.size();
/* 156 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 158 */       Object[] array = (Object[])(Object[])this.m_checkboxGroupsList.elementAt(i);
/* 159 */       Vector list = (Vector)array[1];
/* 160 */       int listSize = list.size();
/* 161 */       boolean hasSelected = false;
/* 162 */       for (int j = 0; j < listSize; ++j)
/*     */       {
/* 164 */         JRadioButton box = (JRadioButton)list.elementAt(j);
/* 165 */         if (!box.isSelected())
/*     */           continue;
/* 167 */         hasSelected = true;
/* 168 */         break;
/*     */       }
/*     */ 
/* 172 */       if (hasSelected)
/*     */         continue;
/* 174 */       JRadioButton box = (JRadioButton)list.elementAt(0);
/* 175 */       box.setSelected(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public char getSeperator()
/*     */   {
/* 182 */     return this.m_separator;
/*     */   }
/*     */ 
/*     */   public void setSeperator(char ch)
/*     */   {
/* 187 */     this.m_separator = ch;
/* 188 */     for (int i = 0; i < this.m_subPanels.length; ++i)
/*     */     {
/* 190 */       if (this.m_subPanels[i] == null)
/*     */         continue;
/* 192 */       this.m_subPanels[i].setSeperator(ch);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getState()
/*     */   {
/* 199 */     StringBuffer state = new StringBuffer();
/* 200 */     for (int i = 0; i < this.m_options.length; ++i)
/*     */     {
/* 202 */       if (this.m_checkboxes[i].isSelected())
/*     */       {
/* 204 */         state.append((String)this.m_options[i][0]);
/*     */       }
/* 206 */       if (this.m_subPanels[i] == null)
/*     */         continue;
/* 208 */       String subState = this.m_subPanels[i].getState();
/* 209 */       state.append(subState);
/*     */     }
/*     */ 
/* 212 */     String retVal = state.toString();
/* 213 */     retVal = optimizeStateString(retVal);
/*     */ 
/* 215 */     return retVal;
/*     */   }
/*     */ 
/*     */   public String optimizeStateString(String state)
/*     */   {
/* 223 */     if (state.length() == 0)
/*     */     {
/* 225 */       return "";
/*     */     }
/* 227 */     if (state.charAt(0) != this.m_separator)
/*     */     {
/* 229 */       state = this.m_separator + state;
/*     */     }
/* 231 */     if (state.charAt(state.length() - 1) != this.m_separator)
/*     */     {
/* 233 */       state = state + this.m_separator;
/*     */     }
/* 235 */     Hashtable alreadyFoundFlags = new Hashtable();
/* 236 */     String[] flags = getFlags(state);
/* 237 */     StringBuffer out = new StringBuffer();
/* 238 */     for (int i = 0; i < flags.length; ++i)
/*     */     {
/* 240 */       if (alreadyFoundFlags.get(flags[i]) != null)
/*     */         continue;
/* 242 */       if (out.length() == 0)
/*     */       {
/* 244 */         out.append(this.m_separator);
/*     */       }
/* 246 */       out.append(flags[i]);
/* 247 */       out.append(this.m_separator);
/* 248 */       alreadyFoundFlags.put(flags[i], "1");
/*     */     }
/*     */ 
/* 251 */     return out.toString();
/*     */   }
/*     */ 
/*     */   public String[] getFlags(String field)
/*     */   {
/* 256 */     Vector v = new IdcVector();
/* 257 */     int length = field.length();
/* 258 */     int start = 0;
/* 259 */     boolean onSeparator = true;
/* 260 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 262 */       char c = field.charAt(i);
/* 263 */       if (c == this.m_separator)
/*     */       {
/* 265 */         if (onSeparator)
/*     */           continue;
/* 267 */         String flag = field.substring(start, i);
/* 268 */         if (flag.length() > 0)
/*     */         {
/* 270 */           v.addElement(flag);
/*     */         }
/* 272 */         onSeparator = true;
/*     */       }
/*     */       else
/*     */       {
/* 277 */         if (!onSeparator)
/*     */           continue;
/* 279 */         start = i;
/* 280 */         onSeparator = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 285 */     String[] list = new String[v.size()];
/* 286 */     v.copyInto(list);
/* 287 */     return list;
/*     */   }
/*     */ 
/*     */   public void setState(String state)
/*     */   {
/* 292 */     boolean isEmpty = state.length() == 0;
/* 293 */     for (int i = 0; i < this.m_options.length; ++i)
/*     */     {
/* 295 */       if (this.m_enabledFlags[i] == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 299 */       boolean isChecked = false;
/* 300 */       if (((isEmpty) && (this.m_visibleFlags[i] != 0) && (this.m_defaultOnFlags[i] != 0)) || (state.indexOf((String)this.m_options[i][0]) >= 0))
/*     */       {
/* 302 */         isChecked = true;
/*     */       }
/* 304 */       this.m_checkboxes[i].setSelected(isChecked);
/*     */ 
/* 306 */       if (this.m_subPanels[i] == null)
/*     */         continue;
/* 308 */       this.m_subPanels[i].setState(state);
/* 309 */       this.m_subPanels[i].setEnabled(isChecked);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean state)
/*     */   {
/* 317 */     for (int i = 0; i < this.m_options.length; ++i)
/*     */     {
/*     */       boolean newState;
/*     */       boolean newState;
/* 320 */       if (this.m_enabledFlags[i] == 0)
/*     */       {
/* 322 */         newState = false;
/*     */       }
/*     */       else
/*     */       {
/* 326 */         newState = state;
/*     */       }
/* 328 */       this.m_checkboxes[i].setEnabled(newState);
/* 329 */       if (this.m_subPanels[i] == null)
/*     */         continue;
/* 331 */       this.m_subPanels[i].setEnabled(newState);
/*     */     }
/*     */ 
/* 334 */     super.setEnabled(state);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 339 */     Object item = event.getItemSelectable();
/* 340 */     if (!item instanceof JRadioButton)
/*     */       return;
/* 342 */     JRadioButton box = (JRadioButton)item;
/* 343 */     for (int i = 0; i < this.m_checkboxes.length; ++i)
/*     */     {
/* 345 */       if (this.m_checkboxes[i] != box)
/*     */         continue;
/* 347 */       if (this.m_subPanels[i] == null)
/*     */         return;
/* 349 */       this.m_subPanels[i].setEnabled(box.isSelected()); return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 359 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80422 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CheckboxPanel
 * JD-Core Version:    0.5.4
 */