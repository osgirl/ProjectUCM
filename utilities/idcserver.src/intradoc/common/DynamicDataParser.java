package intradoc.common;

import java.io.IOException;
import java.io.Reader;

public abstract interface DynamicDataParser
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int CONTINUE = 0;
  public static final int FINISHED = 1;

  public abstract int parseResource(Reader paramReader, DynamicHtml paramDynamicHtml, DynamicData paramDynamicData, ParseOutput paramParseOutput)
    throws IOException, ParseSyntaxException;

  public abstract boolean mergeDataFromPriorScript(DynamicHtml paramDynamicHtml, DynamicData paramDynamicData, DynamicDataMergeInfo paramDynamicDataMergeInfo);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataParser
 * JD-Core Version:    0.5.4
 */