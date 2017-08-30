package com.forweaver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNFormatUtil;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;

@Component
public class AnnotationHandler implements ISVNAnnotateHandler {
    private boolean myIsUseMergeHistory;
    private boolean myIsVerbose;
    private ISVNOptions myOptions;
    
    public Map<String, Object> result;
    private List<Map<String, Object>>blameinfolist;
    
	public void setMyIsUseMergeHistory(boolean myIsUseMergeHistory) {
		this.myIsUseMergeHistory = myIsUseMergeHistory;
	}

	public void setMyIsVerbose(boolean myIsVerbose) {
		this.myIsVerbose = myIsVerbose;
	}

	public void setMyOptions(ISVNOptions myOptions) {
		this.myOptions = myOptions;
	}
	
	public void setInit(boolean myIsUseMergeHistory, boolean myIsVerbose, ISVNOptions myOptions){
		this.myIsUseMergeHistory = myIsUseMergeHistory;
		this.myIsVerbose = myIsVerbose;
		this.myOptions = myOptions;
		
		result = new HashMap<String, Object>();
    	blameinfolist = new ArrayList<Map<String, Object>>();
    	
    	System.out.println("data init");
	}
	
	public List<Map<String, Object>> getResult() {
		return this.blameinfolist;
	}

	/**
     * Deprecated.
     */
    public void handleLine(Date date, long revision, String author, String line) throws SVNException {
        handleLine(date, revision, author, line, null, -1, null, null, 0);
    }

    /**
     * Formats per line information and prints it out to the console.
     */
    public void handleLine(Date date, long revision, String author, String line, Date mergedDate, 
            long mergedRevision, String mergedAuthor, String mergedPath, int lineNumber) throws SVNException {
        String totalcontent = "";
        
        String mergedStr = "";
        if(myIsUseMergeHistory) {
            if (revision != mergedRevision) {
                mergedStr = "G ";
            } else {
                mergedStr = "  ";
            }

            date = mergedDate;
            revision = mergedRevision;
            author = mergedAuthor;
        } 
           
        String revStr = revision >= 0 ? SVNFormatUtil.formatString(Long.toString(revision), 6, false) : "     -";
        String authorStr = author != null ? SVNFormatUtil.formatString(author, 10, false) : "         -";
        
        result.put("userName", authorStr);
        result.put("userEmail", authorStr);
        result.put("commitID", revStr);
        
        if (myIsVerbose) {
            String dateStr = ""; 
            if (date != null) {
                String tempDate = SVNDate.formatRFC1123Date(date);
                int i=0;
                //냔,월,일 만 가져올 수 있도록 파싱//
                StringTokenizer tokenizer = new StringTokenizer(tempDate, " ");
                
                while(tokenizer.hasMoreTokens()) { 
                	String str = tokenizer.nextToken();
                	
                	if(i == 1 || i == 2 || i ==3){
                		dateStr += str + " ";
                	} 
                	
                	i++;
                }

                System.out.println("date human: " + dateStr);
            }
            
            result.put("commitTime", dateStr);

            totalcontent += mergedStr + revStr + " " + authorStr + " " + dateStr + " ";
            //System.out.print(mergedStr + revStr + " " + authorStr + " " + dateStr + " ");
            if (myIsUseMergeHistory && mergedPath != null) {
                String pathStr = SVNFormatUtil.formatString(mergedPath, 14, true);
                
                totalcontent += pathStr + " ";
                //System.out.print(pathStr + " ");
            }
            
            totalcontent += line;
            //System.out.println(line);
        } else {
        	totalcontent += mergedStr + revStr + " " + authorStr + " " + line;
            //System.out.println(mergedStr + revStr + " " + authorStr + " " + line);
        }
        
        System.out.println("info: " + totalcontent);
        
        blameinfolist.add(result);
    }

    public boolean handleRevision(Date date, long revision, String author, File contents) throws SVNException {
        /* We do not want our file to be annotated for each revision of the range, but only for the last 
         * revision of it, so we return false  
         */
        return false;
    }

    public void handleEOF() {
    }
    
}
