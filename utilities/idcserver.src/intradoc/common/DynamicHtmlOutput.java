package intradoc.common;

import java.io.IOException;
import java.io.Writer;

public abstract interface DynamicHtmlOutput
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract DynamicHtml getAndRedirectHtmlResource(String paramString, HtmlChunk paramHtmlChunk)
    throws ParseSyntaxException;

  public abstract void setBackRedirectHtmlResource(String paramString, DynamicHtml paramDynamicHtml, HtmlChunk paramHtmlChunk)
    throws ParseSyntaxException;

  public abstract DynamicData getDynamicDataResource(String paramString, HtmlChunk paramHtmlChunk)
    throws ParseSyntaxException;

  public abstract boolean checkCondition(HtmlChunk paramHtmlChunk, GrammarElement paramGrammarElement)
    throws IOException, ParseSyntaxException;

  public abstract boolean loadNextRow(HtmlChunk paramHtmlChunk, String paramString)
    throws IOException;

  public abstract void endActiveResultSet();

  public abstract void substituteVariable(HtmlChunk paramHtmlChunk, GrammarElement paramGrammarElement, Writer paramWriter)
    throws IOException, ParseSyntaxException;

  public abstract String evaluateVariable(HtmlChunk paramHtmlChunk, GrammarElement paramGrammarElement)
    throws IOException, ParseSyntaxException;

  public abstract Object getScriptObject(String paramString, Object paramObject);

  public abstract void setScriptObject(String paramString, Object paramObject);

  public abstract void evaluateBreakpoint(HtmlChunk paramHtmlChunk);

  public abstract void registerMerger();

  public abstract void unregisterMerger();

  public abstract void doBreakpoint(HtmlChunk paramHtmlChunk);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicHtmlOutput
 * JD-Core Version:    0.5.4
 */