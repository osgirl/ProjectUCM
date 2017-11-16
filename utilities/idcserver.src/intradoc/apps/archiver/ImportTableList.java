/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.SimpleResultSetFilter;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.RefreshItem;
/*     */ import intradoc.shared.gui.ValueNode;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Dimension;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTree;
/*     */ import javax.swing.event.TreeExpansionEvent;
/*     */ import javax.swing.event.TreeExpansionListener;
/*     */ import javax.swing.tree.DefaultTreeModel;
/*     */ import javax.swing.tree.DefaultTreeSelectionModel;
/*     */ import javax.swing.tree.TreePath;
/*     */ 
/*     */ public class ImportTableList extends PanePanel
/*     */   implements RefreshItem, TreeExpansionListener
/*     */ {
/*  46 */   protected JScrollPane m_scrollPane = null;
/*  47 */   protected JTree m_tree = null;
/*  48 */   protected DefaultTreeModel m_treeModel = null;
/*  49 */   protected DefaultTreeSelectionModel m_treeSelectionModel = null;
/*  50 */   protected boolean m_isInitialized = false;
/*  51 */   protected String m_archiverName = null;
/*  52 */   protected CollectionContext m_context = null;
/*  53 */   protected Vector m_tables = null;
/*  54 */   protected String m_batchFile = null;
/*  55 */   protected Properties m_props = null;
/*  56 */   protected String m_treeDirs = "|";
/*     */ 
/*  59 */   public static final String[][] KEYS = { { "aTableName", "tableName" }, { "aCreateTimeStamp", "createTimeStamp" }, { "aModifiedTimeStamp", "modifyTimeStamp" }, { "aUseSourceID", "useSourceID" }, { "aIsCreateNewTable", "isCreateTable" }, { "aIsCreateNewField", "isSyncTableDesign" }, { "aParentTables", "parentTables" }, { "aTableRelations", "tableRelations" }, { "aIsReplicateDeletedRows", "isReplicateDeletedRows" }, { "aUseParentTS", "useParentTS" }, { "aRemoveExistingChildren", "removeExistingChildren" }, { "aDeleteParentOnlyWhenNoChild", "removeOnlyWhenNoChild" }, { "aAllowDeleteParentRows", "allowDeleteParentRows" } };
/*     */ 
/*     */   public void init(SystemInterface sys, CollectionContext cxt)
/*     */   {
/*  75 */     init(sys, cxt, null);
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, CollectionContext cxt, RefreshItem refresher)
/*     */   {
/*  80 */     this.m_context = cxt;
/*     */ 
/*  82 */     ValueNode root = new ValueNode();
/*  83 */     this.m_tree = new JTree(root);
/*  84 */     this.m_treeModel = ((DefaultTreeModel)this.m_tree.getModel());
/*  85 */     this.m_treeSelectionModel = ((DefaultTreeSelectionModel)this.m_tree.getSelectionModel());
/*  86 */     this.m_scrollPane = new JScrollPane(this.m_tree);
/*  87 */     this.m_tree.setRootVisible(false);
/*  88 */     this.m_tree.addTreeExpansionListener(this);
/*  89 */     this.m_scrollPane.setPreferredSize(new Dimension(300, 180));
/*  90 */     add(this.m_scrollPane);
/*     */   }
/*     */ 
/*     */   public void refreshList(Properties props)
/*     */   {
/*  95 */     String archiverName = props.getProperty("aArchiveName");
/*  96 */     if ((archiverName == null) || (this.m_archiverName == null) || (!archiverName.equals(this.m_archiverName)))
/*     */     {
/*  98 */       ValueNode root = (ValueNode)this.m_treeModel.getRoot();
/*  99 */       for (int i = root.getChildCount() - 1; i >= 0; --i)
/*     */       {
/* 101 */         ValueNode node = (ValueNode)root.getChildAt(i);
/* 102 */         this.m_treeModel.removeNodeFromParent(node);
/*     */       }
/*     */ 
/* 105 */       this.m_treeDirs = "|";
/*     */     }
/* 107 */     this.m_archiverName = archiverName;
/*     */ 
/* 109 */     if (this.m_archiverName == null)
/*     */       return;
/* 111 */     populateTree(props);
/*     */   }
/*     */ 
/*     */   public void populateTree(Properties props)
/*     */   {
/* 117 */     DataResultSet drset = getBatchFiles();
/* 118 */     if (drset == null)
/*     */     {
/* 120 */       return;
/*     */     }
/*     */ 
/* 123 */     Vector trees = populateTree(props, drset, 0, null, null);
/* 124 */     ValueNode root = (ValueNode)this.m_treeModel.getRoot();
/* 125 */     int size = trees.size();
/* 126 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 128 */       ValueNode item = (ValueNode)trees.elementAt(i);
/* 129 */       if (item == null)
/*     */         continue;
/* 131 */       this.m_treeModel.insertNodeInto(item, root, root.getChildCount());
/*     */     }
/*     */ 
/* 135 */     TreePath path = new TreePath(root);
/* 136 */     if (this.m_tree.isExpanded(path))
/*     */       return;
/* 138 */     this.m_tree.expandPath(path);
/*     */   }
/*     */ 
/*     */   public Vector populateTree(Properties props, DataResultSet drset, int index, Hashtable keyMap, Vector trees)
/*     */   {
/* 145 */     if (trees == null)
/*     */     {
/* 147 */       trees = new IdcVector();
/*     */     }
/* 149 */     if (keyMap == null)
/*     */     {
/* 151 */       keyMap = new Hashtable();
/*     */     }
/* 153 */     FieldInfo fi = new FieldInfo();
/* 154 */     drset.getFieldInfo("aBatchFile", fi);
/*     */ 
/* 156 */     if ((index >= drset.getNumRows()) || (fi.m_index < 0))
/*     */     {
/* 158 */       return trees;
/*     */     }
/*     */ 
/* 161 */     ValueNode root = (ValueNode)this.m_treeModel.getRoot();
/* 162 */     if (this.m_treeModel.getChildCount(root) < 1)
/*     */     {
/* 164 */       ValueNode node = new ValueNode();
/* 165 */       node.m_id = "Archiver";
/* 166 */       node.m_value = props.getProperty("aArchiveName");
/* 167 */       node.m_props = new Properties();
/* 168 */       node.m_typeId = "Directory";
/* 169 */       node.m_parentIndex = -1;
/*     */ 
/* 171 */       trees.addElement(node);
/* 172 */       keyMap.put("Archiver", node);
/*     */     }
/*     */ 
/* 175 */     for (drset.setCurrentRow(index); drset.isRowPresent(); drset.next())
/*     */     {
/* 177 */       String value = drset.getStringValue(fi.m_index);
/* 178 */       int slash = value.indexOf(47);
/* 179 */       if (slash <= 0)
/*     */         continue;
/* 181 */       int uScore = value.indexOf("_arTable", slash);
/* 182 */       if (uScore < 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 187 */       String dir = value.substring(0, slash);
/* 188 */       String table = value.substring(slash + 1, uScore);
/*     */ 
/* 190 */       String combo = value.substring(0, uScore);
/* 191 */       ValueNode dirItem = null;
/* 192 */       if ((dirItem = (ValueNode)keyMap.get(dir)) == null)
/*     */       {
/* 194 */         if (this.m_treeDirs.indexOf(dir) > 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 198 */         this.m_treeDirs = (this.m_treeDirs + dir + '|');
/*     */ 
/* 200 */         ValueNode node = new ValueNode();
/* 201 */         node.m_id = "Directory";
/* 202 */         node.m_value = dir;
/* 203 */         node.m_typeId = "Directory";
/* 204 */         node.m_props = new Properties();
/* 205 */         node.m_parentIndex = 1;
/* 206 */         dirItem = node;
/*     */ 
/* 208 */         ValueNode parentItem = (ValueNode)keyMap.get("Archiver");
/* 209 */         if (parentItem == null)
/*     */         {
/* 211 */           parentItem = (ValueNode)this.m_treeModel.getRoot();
/*     */         }
/* 213 */         this.m_treeModel.insertNodeInto(dirItem, parentItem, parentItem.getChildCount());
/* 214 */         keyMap.put(dir, dirItem);
/*     */       }
/* 216 */       if (keyMap.get(combo) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 221 */       ValueNode node = new ValueNode();
/* 222 */       node.m_id = "Table";
/* 223 */       node.m_value = table;
/* 224 */       node.m_props = new Properties();
/* 225 */       node.m_typeId = "Item";
/* 226 */       node.m_parentIndex = 1;
/*     */ 
/* 228 */       this.m_treeModel.insertNodeInto(node, dirItem, dirItem.getChildCount());
/* 229 */       keyMap.put(combo, node);
/*     */     }
/*     */ 
/* 233 */     return trees;
/*     */   }
/*     */ 
/*     */   protected DataResultSet getBatchFiles()
/*     */   {
/* 238 */     DataResultSet drset = this.m_context.getBatchFiles();
/* 239 */     if (drset == null)
/*     */     {
/* 241 */       return null;
/*     */     }
/* 243 */     SimpleResultSetFilter filter = new SimpleResultSetFilter("*_arTable~*");
/* 244 */     filter.m_isWildcard = true;
/* 245 */     DataResultSet batchFiles = new DataResultSet();
/* 246 */     batchFiles.copyFiltered(drset, "aBatchFile", filter);
/* 247 */     return batchFiles;
/*     */   }
/*     */ 
/*     */   public String getSelectedItemName()
/*     */   {
/* 252 */     return getSelectedItemName(false);
/*     */   }
/*     */ 
/*     */   public String getSelectedItemName(boolean isCategoryName)
/*     */   {
/* 257 */     String value = "";
/* 258 */     TreePath path = this.m_treeSelectionModel.getSelectionPath();
/* 259 */     if (path != null)
/*     */     {
/* 261 */       ValueNode node = (ValueNode)path.getLastPathComponent();
/* 262 */       if (node != null)
/*     */       {
/* 264 */         if (isCategoryName)
/*     */         {
/* 266 */           value = node.m_id;
/*     */         }
/*     */         else
/*     */         {
/* 270 */           value = node.m_value;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 275 */     return value;
/*     */   }
/*     */ 
/*     */   public void expandItem(ValueNode item)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void treeExpanded(TreeExpansionEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void treeCollapsed(TreeExpansionEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public ValueNode getSelectedItem()
/*     */   {
/* 301 */     TreePath path = this.m_treeSelectionModel.getSelectionPath();
/* 302 */     if (path != null)
/*     */     {
/* 304 */       ValueNode node = (ValueNode)path.getLastPathComponent();
/* 305 */       return node;
/*     */     }
/*     */ 
/* 308 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 313 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ImportTableList
 * JD-Core Version:    0.5.4
 */