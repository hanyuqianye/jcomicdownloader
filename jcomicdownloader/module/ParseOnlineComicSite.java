/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/10/25
----------------------------------------------------------------------------------------------------
ChangeLog:
2.06: 修復集數名稱數字格式化的bug。
2.04: 修改集數名稱命名機制，將裡面的數字格式化（ex. 第3回 -> 第003回），以方便排序。
2.02: 拿掉轉網址碼的編碼修正
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import jcomicdownloader.ComicDownGUI;
import jcomicdownloader.SetUp;
import jcomicdownloader.tools.Common;
import jcomicdownloader.tools.CommonGUI;

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

    abstract public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ); // 從主頁面取得所有集數名稱和位址

    abstract public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList );

    abstract public String[] getTempFileNames(); // 取得暫存檔名稱

    public void setURL( String url ) {
        this.webSite = url;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public void setWholeTitle( String wholeTitle ) {
        this.wholeTitle = wholeTitle;
    }

    public void setDownloadDirectory( String downloadDirectory ) {
        this.downloadDirectory = downloadDirectory;
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
        for ( int i = 0 ; i < tempFileNames.length ; i++ ) {
            Common.deleteFile( SetUp.getTempDirectory(), tempFileNames[i] );
        }
    }

    public String fixSpecialCase( String url ) {
        /* 拿掉修正似乎就沒有問題了......奇怪當初怎麼需要修正勒.....
        //  第一數（%E6%95%B8）要改成第一話（%E8%A9%B1）...不曉得是否為特例...
        url = url.replaceAll("%E6%95%B8", "%E8%A9%B1");
        // 話（%E6%95%B8）要改成?（%E8%AF%9D）...不曉得是否為特例...
        url = url.replaceAll("%A9%B1", "%AF%9D");
        //  石黑正?（%EF%BF%BD）要改成石黑正數（%E6%95%B8）...不曉得是否為特例...
        url = url.replaceAll("%EF%BF%BD", "%E6%95%B8");  // ex. http://kukudm.com/comiclist/1247/23363/1.htm
        
        // 數數E6%95%B8%E6%95%B8% 改成繪漢E7%B9%AA%E6%BC%A2%
        url = url.replaceAll("E6%95%B8%E6%95%B8%", "E7%B9%AA%E6%BC%A2%");
        
        // 數瞄（%E6%95%B8%E7%9E%84）改成掃瞄（%E6%8E%83%E7%9E%84）
        url = url.replaceAll("%E6%95%B8%E7%9E%84", "%E6%8E%83%E7%9E%84");
        
        // 117话改為117話 ex. http://kukudm.com/comiclist/774/21545/1.htm
        //url = url.replaceAll("%E8%AF%9D", "%E8%A9%B1");
         */
        return url;
    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, int delayTime ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, delayTime, false, "" );
    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber,
            int delayTime, boolean needCookie, String cookieString ) {
        if ( wholeTitle == null ) {
            CommonGUI.stateBarMainMessage = title + " : ";
        } else {
            CommonGUI.stateBarMainMessage = title + "/" + wholeTitle + " : ";
        }
        CommonGUI.stateBarDetailMessage = "共" + totalPage + "頁，第" + (nowPageNumber) + "頁下載中";

        if ( SetUp.getShowDoneMessageAtSystemTray() && Common.withGUI() ) {
            ComicDownGUI.trayIcon.setToolTip( CommonGUI.stateBarMainMessage
                    + CommonGUI.stateBarDetailMessage );
        }

        String extensionName = "";
        if ( url.matches( "(?s).*\\.\\w+" ) ) {
            extensionName = url.split( "\\." )[url.split( "\\." ).length - 1]; // 取得圖片附檔名
        } else {
            extensionName = "jpg"; // 因應WY沒有附檔名，只好都給jpg
        }
        NumberFormat formatter = new DecimalFormat( Common.getZero() );
        String fileName = formatter.format( nowPageNumber ) + "." + extensionName;
        String nextFileName = formatter.format( nowPageNumber + 1 ) + "." + extensionName;

        CommonGUI.stateBarDetailMessage += ": [" + fileName + "]";

        // 下載第n張之前，先檢查第n+1張圖是否存在，若是則跳下一張
        if ( !new File( getDownloadDirectory() + fileName ).exists() ||
             !new File( getDownloadDirectory() + nextFileName ).exists()) {
            if ( delayTime == 0 ) {
                Common.downloadFile( url, getDownloadDirectory(), fileName, needCookie, cookieString );
            } else {
                Common.slowDownloadFile( url, getDownloadDirectory(), fileName, delayTime, needCookie, cookieString );
            }
        }

    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）,圖片名稱指定
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName, int delayTime, boolean fastMode ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, fileName, delayTime, false, "", fastMode );
    }

    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName,
            int delayTime, boolean needCookie, String cookieString ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, fileName, delayTime, false, "", false );
    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）,圖片名稱指定
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName,
            int delayTime, boolean needCookie, String cookieString, boolean fastMode ) {
        if ( wholeTitle == null ) {
            CommonGUI.stateBarMainMessage = title + " : ";
        } else {
            CommonGUI.stateBarMainMessage = title + "/" + wholeTitle + " : ";
        }
        CommonGUI.stateBarDetailMessage = "共" + totalPage + "頁，第" + (nowPageNumber) + "頁下載中";

        if ( SetUp.getShowDoneMessageAtSystemTray() && Common.withGUI() ) {
            ComicDownGUI.trayIcon.setToolTip( CommonGUI.stateBarMainMessage
                    + CommonGUI.stateBarDetailMessage );
        }

        CommonGUI.stateBarDetailMessage += ": [" + fileName + "]";

        if ( delayTime == 0 ) {
            Common.downloadFile( url, getDownloadDirectory(), fileName, needCookie, cookieString, 
                    fastMode, SetUp.getRetryTimes(), false, false );
        } else {
            Common.slowDownloadFile( url, getDownloadDirectory(), fileName, delayTime, needCookie, cookieString );
        }

    }

    // 將集數名稱的數字部份格式化（ex. 第3回 -> 第003回），方便排序之用
    protected String getVolumeWithFormatNumber( String volume ) {
        String formatVolume = "";

        try {

            int beginIndex = -1;
            for ( int i = 0 ; i < volume.length() ; i++ ) {
                if ( volume.substring( i, i + 1 ).matches( "\\d" ) ) {
                    beginIndex = i;
                    break;
                }
            }
            //System.out.println( beginIndex + " -> " + volume );

            int endIndex = volume.length();
            for ( int i = beginIndex ; i < volume.length() && beginIndex >= 0; i++ ) {
                if ( volume.substring( i, i + 1 ).matches( "\\D" ) ) {
                    endIndex = i;
                    break;
                }
                else {
                    //System.out.println( volume.substring(i,i+1) + " " );
                }
            }

            if ( endIndex < 0 || beginIndex < 0 ) {
                //System.out.println( "無法格式化: " + volume + " " + beginIndex + " " + endIndex );
                formatVolume = volume;
            } else {
                //System.out.println( volume + " " + beginIndex + " " + endIndex + " 數字部份：" + volume.substring( beginIndex, endIndex ) );

                String originalNumber = volume.substring( beginIndex, endIndex );
                NumberFormat formatter = new DecimalFormat( "000" );
                String formatNumber = formatter.format( Integer.parseInt( originalNumber ) );

                formatVolume = volume.replaceFirst( originalNumber, formatNumber );
            }

        } catch ( Exception ex ) {
            formatVolume = volume;
            Common.errorReport( "集數名稱的數字規格化處理發生錯誤！" );
            ex.printStackTrace();
        }

        return formatVolume;
    }
}
// http://ascrsbdfdb.kukudm.net:81/kuku8comic8/201110/20111029/%E9%BC%A0%E7%B9%AA%E6%BC%A2%E5%8C%96%E7%BE%8E%E9%A3%9F163/Comic.kukudm.com_0103S.jpg
// http://cc.kukudm.com/kuku8comic8/201110/20111029/%E9%BC%A0%E6%95%B8%E6%95%B8%E5%8C%96%E7%BE%8E%E9%A3%9F163/Comic.kukudm.com_0103S.jpg
