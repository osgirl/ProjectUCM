package infomentum.ontology.loader.protege;


import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import intradoc.apps.shared.AppLauncher;
import intradoc.common.Browser;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Parameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JButton;


public class ProtegeApplet extends JApplet implements Parameters {
	String m_metadata;
	 String m_cgiURL;
     String m_imagesURL;
     String m_helpURL;
     String m_gifName;
     String m_appName;
     static URL m_cgiURLObject;
	 

    

      public void init() {
	        try {
	        	  String s = getParameter("CGI-URL");
			        if (s != null) {
			            m_cgiURL = s;
	                }
			        s = getParameter("GIF-NAME");
			        if (s != null) {
			            m_gifName = s;
	                }
			        s = getParameter("IMAGES-URL");
			        if (s != null) {
			            m_imagesURL = s;
	                }
			        s = getParameter("HELP-URL");
			        if (s != null) {
			            m_helpURL = s;
	                }
			        
			        s = getParameter("METADATA");
			        if (s != null) {
			            m_metadata = s;
	                }
			       
			        URL url = null;
			        URL url1 = null;
			        URL url2 = getCodeBase();
			        try {
			            m_cgiURLObject = new URL(url2, m_cgiURL);
			            url = new URL(url2, m_imagesURL);
			            url1 = new URL(url2, m_helpURL);
	                }
			        catch (Exception exception) {
			            if (SystemUtils.m_verbose) {
			                SystemUtils.dumpException("system", exception);
	                    }
	                }
			        Browser.setAppletContext(getAppletContext(), url2);
			        AppLauncher.setAppParameters(this);
			        AppLauncher.init(null, false, m_cgiURLObject);
			        
	            SystemUtilities.initGraphics();
	            SystemUtilities.setApplet(true);
	            System.out.println("Applet init: " + m_metadata);
	      //      URL urld = this.getDocumentBase();
	        //    URL url = this.getCodeBase();
	         //   System.out.println("Doc Base: " + urld.getPath());
	         //   System.out.println("URL  Base: " + url);
	            setup( url2 );
	         //   DataBinder databinder = new DataBinder();
	          //  AppLauncher.executeService("PING_SERVER", databinder);
	          //  System.out.println("Status: " + databinder);
          }
	        catch (Exception exception) {
	        	exception.printStackTrace();
	            Log.getLogger().severe(Log.toString(exception));
	            SystemUtilities.pause();
          }
      }
/*
      public static void main(String args[]) {
	        JFrame jframe = new JFrame();
	        jframe.getContentPane().setLayout(new BorderLayout());
	        ProtegeApplet applet = new ProtegeApplet();
	        jframe.getContentPane().add(applet);
	        applet.setup(getURL(args), args[1]);
	        jframe.pack();
	        jframe.setVisible(true);
      }
*/
      private static URL getURL(String as[]) {
	        URL url = null;
	        try {
	            url = new URL(as[0]);
          }
	        catch (MalformedURLException malformedurlexception) {
	            Log.getLogger().severe(Log.toString(malformedurlexception));
          }
	        return url;
      }
      

      private void setup(final URL compPath) {
	        JButton button = new JButton(Icons.getLogoBannerIcon());
	        button.setFocusPainted(false);
	        button.addActionListener(new ActionListener() {	 
	            public void actionPerformed(ActionEvent actionevent) {
	                String as[];
			        if (m_metadata == null) {
			            as = new String[0];
	                } else {
			            as = (new String[] {m_metadata, compPath.toString()}
	                );
	            }
			    
			    (new ProtegeStandaloneTab(as, m_cgiURL)).loadTab(getAppletContext());
	            }
	        });
	        getContentPane().add(button);
      }



      public String get(String s) throws DataException {
	        return getParameter(s);
      }

      public String getSystem(String s) throws DataException {
	        return getParameter(s);
      }





}
