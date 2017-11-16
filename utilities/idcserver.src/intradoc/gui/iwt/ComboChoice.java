/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TextHandler;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.TextListener;
/*     */ import java.util.List;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ComboChoice extends PanePanel
/*     */   implements TextHandler
/*     */ {
/*     */   protected JPanel m_panel;
/*     */   protected MultiComboBox m_comboBox;
/*  44 */   protected int m_textWidth = 20;
/*  45 */   protected int m_numVisible = 0;
/*  46 */   protected String m_defaultDisplayStr = null;
/*     */ 
/*  49 */   protected String m_valueSeparator = ", ";
/*  50 */   protected boolean m_isMultiSelect = false;
/*     */ 
/*     */   public ComboChoice()
/*     */   {
/*  55 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(boolean multiSelect)
/*     */   {
/*  60 */     this.m_isMultiSelect = multiSelect;
/*  61 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(int width)
/*     */   {
/*  66 */     this.m_textWidth = width;
/*  67 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(int width, boolean multiSelect)
/*     */   {
/*  73 */     this.m_textWidth = width;
/*  74 */     this.m_isMultiSelect = multiSelect;
/*  75 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(int width, int numVisible)
/*     */   {
/*  80 */     this.m_textWidth = width;
/*  81 */     this.m_numVisible = numVisible;
/*  82 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(int width, int numVisible, boolean multiSelect)
/*     */   {
/*  87 */     this.m_textWidth = width;
/*  88 */     this.m_numVisible = numVisible;
/*  89 */     this.m_isMultiSelect = multiSelect;
/*  90 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr)
/*     */   {
/*  95 */     this.m_defaultDisplayStr = defaultDisplayStr;
/*  96 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr, boolean multiSelect)
/*     */   {
/* 101 */     this.m_isMultiSelect = multiSelect;
/* 102 */     this.m_defaultDisplayStr = defaultDisplayStr;
/* 103 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr, int width)
/*     */   {
/* 108 */     this.m_defaultDisplayStr = defaultDisplayStr;
/* 109 */     this.m_textWidth = width;
/* 110 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr, int width, boolean multiSelect)
/*     */   {
/* 115 */     this.m_isMultiSelect = multiSelect;
/* 116 */     this.m_defaultDisplayStr = defaultDisplayStr;
/* 117 */     this.m_textWidth = width;
/* 118 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr, int width, int numVisible)
/*     */   {
/* 123 */     this.m_defaultDisplayStr = defaultDisplayStr;
/* 124 */     this.m_textWidth = width;
/* 125 */     this.m_numVisible = numVisible;
/* 126 */     init();
/*     */   }
/*     */ 
/*     */   public ComboChoice(String defaultDisplayStr, int width, int numVisible, boolean multiSelect)
/*     */   {
/* 131 */     this.m_isMultiSelect = multiSelect;
/* 132 */     this.m_defaultDisplayStr = defaultDisplayStr;
/* 133 */     this.m_textWidth = width;
/* 134 */     this.m_numVisible = numVisible;
/* 135 */     init();
/*     */   }
/*     */ 
/*     */   protected void init()
/*     */   {
/* 144 */     this.m_comboBox = new MultiComboBox();
/* 145 */     this.m_comboBox.setEditable(true);
/* 146 */     this.m_comboBox.setText(this.m_defaultDisplayStr);
/*     */ 
/* 148 */     this.m_comboBox.setMultipleSectionMode(this.m_isMultiSelect);
/* 149 */     if (this.m_numVisible > 0)
/*     */     {
/* 151 */       this.m_comboBox.setMaximumRowCount(this.m_numVisible);
/*     */     }
/* 153 */     this.m_comboBox.setAlignmentX(0.0F);
/*     */ 
/* 156 */     this.m_panel = new PanePanel();
/* 157 */     GridBagLayout layout = new GridBagLayout();
/* 158 */     this.m_panel.setLayout(layout);
/* 159 */     setLayout(layout);
/* 160 */     GridBagConstraints constraints = new GridBagConstraints();
/* 161 */     constraints.fill = 2;
/* 162 */     constraints.weightx = 1.0D;
/* 163 */     layout.setConstraints(this.m_comboBox, constraints);
/* 164 */     layout.setConstraints(this.m_panel, constraints);
/*     */ 
/* 166 */     this.m_panel.add(this.m_comboBox);
/* 167 */     add(this.m_panel);
/*     */   }
/*     */ 
/*     */   public void initChoiceList(String[][] display)
/*     */   {
/* 174 */     this.m_comboBox.setupNewModelWithHiddens(display);
/*     */   }
/*     */ 
/*     */   public void initChoiceList(List options)
/*     */   {
/*     */     String[] items;
/*     */     String[] items;
/* 180 */     if (null == options)
/*     */     {
/* 182 */       items = new String[0];
/*     */     }
/*     */     else
/*     */     {
/* 186 */       int numItems = options.size();
/* 187 */       items = new String[numItems];
/*     */ 
/* 189 */       for (int i = 0; i < numItems; ++i)
/*     */       {
/* 191 */         items[i] = ((String)options.get(i));
/*     */       }
/*     */     }
/* 194 */     this.m_comboBox.setupNewBasicModel(items);
/*     */   }
/*     */ 
/*     */   public void add(String item)
/*     */   {
/* 199 */     addItem(item);
/*     */   }
/*     */ 
/*     */   public void addItem(String item)
/*     */   {
/* 204 */     if (SystemUtils.m_verbose)
/*     */     {
/* 206 */       Report.debug("applet", "addItem(" + item + ")", null);
/*     */     }
/* 208 */     int numItems = this.m_comboBox.getItemCount();
/* 209 */     this.m_comboBox.insertItemAt(item, numItems);
/*     */   }
/*     */ 
/*     */   public String getItem(int index)
/*     */   {
/* 214 */     return (String)this.m_comboBox.getItemAt(index);
/*     */   }
/*     */ 
/*     */   public int getItemCount()
/*     */   {
/* 219 */     return this.m_comboBox.getItemCount();
/*     */   }
/*     */ 
/*     */   public int getSelectedIndex()
/*     */   {
/* 224 */     return this.m_comboBox.getSelectedIndex();
/*     */   }
/*     */ 
/*     */   public String getSelectedItem()
/*     */   {
/* 229 */     return (String)this.m_comboBox.getSelectedItem();
/*     */   }
/*     */ 
/*     */   public void insert(String str, int index)
/*     */   {
/* 234 */     if (SystemUtils.m_verbose)
/*     */     {
/* 236 */       Report.debug("applet", "insert(" + str + ") at " + String.valueOf(index), null);
/*     */     }
/* 238 */     this.m_comboBox.insertItemAt(str, index);
/*     */   }
/*     */ 
/*     */   public void remove(String item)
/*     */   {
/* 243 */     this.m_comboBox.removeItem(item);
/*     */   }
/*     */ 
/*     */   public void remove(int index)
/*     */   {
/* 251 */     this.m_comboBox.removeItemAt(index);
/*     */   }
/*     */ 
/*     */   public void removeAll()
/*     */   {
/* 257 */     this.m_comboBox.removeAllItems();
/*     */   }
/*     */ 
/*     */   public void removeAllItems()
/*     */   {
/* 262 */     this.m_comboBox.removeAllItems();
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean enable)
/*     */   {
/* 268 */     this.m_comboBox.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   public int getNumVisible()
/*     */   {
/* 275 */     return this.m_numVisible;
/*     */   }
/*     */ 
/*     */   public void setNumVisible(int numVisible)
/*     */   {
/* 280 */     this.m_numVisible = numVisible;
/* 281 */     this.m_comboBox.setMaximumRowCount((this.m_numVisible > 0) ? this.m_numVisible : 8);
/*     */   }
/*     */ 
/*     */   public Component getComboBox()
/*     */   {
/* 286 */     return this.m_comboBox;
/*     */   }
/*     */ 
/*     */   public void select(int pos)
/*     */   {
/* 291 */     this.m_comboBox.setSelectedIndex(pos);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected void selectListItem()
/*     */   {
/* 303 */     String text = this.m_comboBox.getText();
/* 304 */     this.m_comboBox.setSelectedItem(text);
/*     */   }
/*     */ 
/*     */   public void select(String str)
/*     */   {
/* 312 */     this.m_comboBox.setText(str);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener actionListener)
/*     */   {
/* 318 */     this.m_comboBox.addActionListener(actionListener);
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener itemListener)
/*     */   {
/* 323 */     this.m_comboBox.addItemListener(itemListener);
/*     */   }
/*     */ 
/*     */   public void addTextListener(TextListener tl)
/*     */   {
/* 329 */     this.m_comboBox.addTextListener(tl);
/*     */   }
/*     */ 
/*     */   public void removeTextListener(TextListener tl)
/*     */   {
/* 334 */     this.m_comboBox.removeTextListener(tl);
/*     */   }
/*     */ 
/*     */   public String getText()
/*     */   {
/* 341 */     return this.m_comboBox.getText();
/*     */   }
/*     */ 
/*     */   public void setText(String str)
/*     */   {
/* 346 */     this.m_comboBox.setText(str);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 352 */     return "releaseInfo=dev,releaseRevision=$Rev: 83278 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.ComboChoice
 * JD-Core Version:    0.5.4
 */