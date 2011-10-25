/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;

import java.util.*;

/**
*
* 解析網站的類別(ParseXXX)都會繼承此一類別
*/
abstract public class ParseOnlineComicSite {
    protected int siteID;
    protected String title;
    protected String wholeTitle;
    protected String webSite; // web page
    protected String[] comicURL; // all comic pic url
    protected int totalPage; // how many pages
    protected int totalVolume; // how many volumes
    protected String indexName; // temp stored file
    protected String indexEncodeName; // temp stored file encoding to UTF-8
    protected String downloadDirectory;
    protected int runMode; // 只分析、只下載或分析加下載

    abstract public void setParameters(); // 須取得title和wholeTitle（title可用getTitle()）
    abstract public void parseComicURL(); // 必須解析出下載位址並傳給comicURL

    abstract public void printLogo(); // 顯示目前解析的漫畫網站名稱

    abstract public boolean isSingleVolumePage( String urlString ); // 檢查是否為單集頁面
    abstract public String getAllPageString( String urlString ); // 取得此網址指向的網頁原始碼字串
    abstract public String getTitleOnSingleVolumePage( String urlString ); // 從單集頁面中取得title
    abstract public String getTitleOnMainPage( String urlString, String allPageString ); // 從主頁面中取得title
    abstract public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString );
    abstract public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList );

    abstract public void setURL( String url );
    abstract public void setWholeTitle( String title );
    abstract public void setDownloadDirectory( String downloadDirectory );
    abstract public String[] getTempFileNames(); // 取得暫存檔名稱
    
    public void setTitle( String title ) {
        this.title = title;
    }
    
    public void setRunMode( int runMode ) {
        this.runMode = runMode;
    }
    
    public int getRunMode() {
        return runMode;
    }
    
    public String[] getComicURL() {
        return comicURL;
    }

    public int getSiteID() {
        return siteID;
    }


    public String getTitle() {
        return title;
    }
    public String getWholeTitle() {
        return wholeTitle;
    }
    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public String getDefaultDownloadDirectory() {
        return title + Common.getSlash() + wholeTitle + Common.getSlash();
    }
    
    public void deleteTempFile( String[] tempFileNames ) {
        for ( int i = 0; i < tempFileNames.length; i ++ ) {
            Common.deleteFile( Common.tempDirectory, tempFileNames[i] );
        }
    }

}
