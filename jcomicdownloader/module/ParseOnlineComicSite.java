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
import jcomicdownloader.enums.FileFormatEnum;
import jcomicdownloader.enums.Site;
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
    protected String textFilePath; // 小說檔案完整目錄位置，包含檔名（只有下載小說時才有用）

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
    protected void singlePageDownload( String title, String wholeTitle, String url, 
        int totalPage, int nowPageNumber, int delayTime ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, 
            delayTime, false, "", "", false );
    }
    
    // 需要用到refer檔頭
    protected void singlePageDownloadUsingRefer( String title, String wholeTitle, String url, 
        int totalPage, int nowPageNumber, int delayTime, String referURL ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, 
            delayTime, false, "", referURL, false );
    }
    
    // 用最簡單的方式下載 使用Common.simpleDownloadFile
    protected void singlePageDownloadUsingSimple( String title, String wholeTitle, String url, 
        int totalPage, int nowPageNumber, String referURL ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, 
            0, false, "", referURL, true );
    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）
    protected void singlePageDownload( String title, String wholeTitle, String url, 
        int totalPage, int nowPageNumber, int delayTime, boolean needCookie, 
        String cookieString, String referURL, boolean simpleDownload ) {
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
            if ( this.siteID == Site.CK_NOVEL || 
                    this.siteID == Site.CK_NOVEL ||
                 this.siteID == Site.MYBEST || 
                    this.siteID == Site.BLOGSPOT  || 
                    this.siteID == Site.PIXNET_BLOG || 
                    this.siteID == Site.XUITE_BLOG  || 
                    this.siteID == Site.YAM_BLOG ) {
                extensionName = "html"; // 因為是網頁，所以副檔名給html
            }
            else {
                extensionName = "jpg"; // 因應WY沒有附檔名，只好都給jpg
            }
        }
        NumberFormat formatter = new DecimalFormat( Common.getZero() );
        String fileName = formatter.format( nowPageNumber ) + "." + extensionName;
        String nextFileName = formatter.format( nowPageNumber + 1 ) + "." + extensionName;

        CommonGUI.stateBarDetailMessage += ": [" + fileName + "]";

        // 下載第n張之前，先檢查第n+1張圖是否存在，若是則跳下一張
        
        if ( Run.isAlive && 
            ( !new File( getDownloadDirectory() + fileName ).exists() ||
             !new File( getDownloadDirectory() + nextFileName ).exists() ) ) {
            Common.debugPrint( nowPageNumber + " " );
            
            if ( simpleDownload ) {
                Common.simpleDownloadFile( url, getDownloadDirectory(), fileName, referURL );
            }
            else if ( delayTime == 0 ) {
                //Common.print( url, getDownloadDirectory(), fileName, needCookie + "", cookieString );
                Common.downloadFile( url, getDownloadDirectory(), fileName, needCookie, cookieString, referURL );
            } else {
                Common.slowDownloadFile( url, getDownloadDirectory(), fileName, delayTime, needCookie, cookieString );
            }
        }

    }

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）,圖片名稱指定
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName, int delayTime, boolean fastMode ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, fileName, delayTime, false, "", "", fastMode );
    }
    
    // 需要設定refer才能下載
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName,
            int delayTime, boolean needCookie, String cookieString, String referURL ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, fileName, delayTime, false, "", referURL, false );
    }

    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName,
            int delayTime, boolean needCookie, String cookieString ) {
        singlePageDownload( title, wholeTitle, url, totalPage, nowPageNumber, fileName, delayTime, false, "", "", false );
    }
    
    

    // 只下載單一張圖片（因應部份網站無法一次解得所有圖片網址，只能每下載一張網頁，從中解析得到網址才下載）,圖片名稱指定
    protected void singlePageDownload( String title, String wholeTitle, String url, int totalPage, int nowPageNumber, String fileName,
            int delayTime, boolean needCookie, String cookieString, String referURL, boolean fastMode ) {
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
            Common.downloadFile( url, getDownloadDirectory(), fileName, needCookie, cookieString, referURL, 
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
            Common.hadleErrorMessage( ex, "集數名稱的數字規格化處理發生錯誤" );
        }

        return formatVolume;
    }
    
    //  將numeric character references全部還原
    public String replaceNCR( String text ) {
        int beginIndex = 0;
        int endIndex = 0;
        
        // 先將非數字的character references進行替換
        text = text.replaceAll( "&nbsp;", " " );
        text = text.replaceAll( "&quot;", "\"" );
        text = text.replaceAll( "&amp;", "&" );
        text = text.replaceAll( "&hellip;", "..." );
        text = text.replaceAll( "&gt;", ">" );
        text = text.replaceAll( "&lt;", "<" );

        String ncrString = ""; // 存放numeric character references字串 ex. &#65289;
        String numberString = ""; // 存放numeric character references的數字部份 ex. 65289
        String decode = ""; // 存放已經解碼的字元 ex. ）

        while ( true ) {
            beginIndex = text.indexOf( "&#", beginIndex );
            endIndex = text.indexOf( ";", beginIndex ) + 1;

            if ( beginIndex >= 0 && endIndex >= 0 && beginIndex < endIndex ) {
                ncrString = text.substring( beginIndex, endIndex );
                numberString = ncrString.substring( 2, ncrString.length() - 1 );
                //Common.debugPrint( "轉換前：" + ncrString );

                if ( numberString.matches( "\\d+" ) ) {
                    char decodeChar = (char) Integer.parseInt( numberString );
                    decode = String.valueOf( decodeChar );
                    //Common.debugPrintln( "　轉換後：" + decode );
                    
                    text = text.replaceAll( ncrString, decode );
                }
            }
            else {
                break;
            }

        }
        
        return text;
    }
    
    // 將換行tag轉換為換行字元
    public String replaceNewLine( String text ) {
        text = text.replaceAll( "\r\n", "" ); // 若原始檔為big5編碼，可能換行是採用\r\n的格式（windows換行機制）
        text = text.replaceAll( "\n", "" ); // 拿掉非windows換行機制的換行字元
        
        // 開始替換
        //text = text.replaceAll( "<br />", "\r\n" );
        //text = text.replaceAll( "<br>", "\r\n" );
        text = text.replaceAll( "<br[^<>]+>", "\r\n" );
        text = text.replaceAll( "</p>", "\r\n" );
        text = text.replaceAll( "</h1>", "\r\n" );
        
        return text;
    }
    
    // 將<img.*>標籤拿掉，只保留其中的圖片網址
    public String replaceImg( String text ) {
        int start = 0;
        int beginIndex = 0;
        int endIndex = 0;
        String picURL = ""; // 圖片網址
        String picName = ""; // 圖片名稱
        while( true ) {
            start = beginIndex= text.indexOf( "<img ", beginIndex );
            if ( beginIndex >= 0 ) {
                beginIndex = text.indexOf( "src=", beginIndex );
                beginIndex = text.indexOf( "http", beginIndex );
                endIndex = text.indexOf( " ", beginIndex ) - 1;

                if ( beginIndex > 0 && endIndex > 0 ) {
                    picURL = text.substring( beginIndex, endIndex );
                    System.out.println( picURL + " -> " + picName );
                
                    text = text.substring( 0, start ) + "\n" + picURL + "\n" + text.substring( start, text.length() );
                    beginIndex = endIndex + picURL.length();
                }
                else {
                    beginIndex = start += 10;
                }
            }
            else {
                break;
            }
            
        }
        
        return text;
    }
    
     // 拿掉<script 到 </script>之間的內容
    public String replaceJS( String text ) {
        int beginIndex = 0;
        int endIndex = 0;
        while ( true ) {
            beginIndex = text.indexOf( "<script", beginIndex );
            endIndex = text.indexOf( "</script>", beginIndex );
            endIndex = text.indexOf( ">", endIndex ) + 1;

            if ( beginIndex > 0 && endIndex > 0 ) { // 拿掉中間的部份
                text = text.substring( 0, beginIndex ) + text.substring( endIndex, text.length() );
            }
            else {
                break;
            }
        }
        return text;
    }
    
     // 拿掉<style 到 </style>之間的內容
    public String replaceStyle( String text ) {
        int beginIndex = 0;
        int endIndex = 0;
        while ( true ) {
            beginIndex = text.indexOf( "<style", beginIndex );
            endIndex = text.indexOf( "</style>", beginIndex );
            endIndex = text.indexOf( ">", endIndex ) + 1;

            if ( beginIndex > 0 && endIndex > 0 ) { // 拿掉中間的部份
                text = text.substring( 0, beginIndex ) + text.substring( endIndex, text.length() );
            }
            else {
                break;
            }
        }
        return text;
    }

    // 將html的tag拿掉，且將numeric character references還原回原本的字元。
    public String replaceProcessToText( String text ) {
        text = replaceNCR( text ); //  將numeric character references全部還原
        text = replaceNewLine( text ); // 將換行tag轉換為換行字元
        text = replaceImg( text ); // 將圖片標籤拿掉，只保留圖片網址
        //text = text.replaceAll( "<script[^(scrpit)]+[(/script>)]{1}", "" ); // 拿掉js
        text = replaceJS( text ); // 拿掉JS
        text = replaceStyle( text ); // 拿掉style
        text = text.replaceAll( "<[^<>]+>", "" ); // 將所有標籤去除

        return text;
    }
    
    // 由純文字轉為html格式
    public String replaceProcessToHtml( String text ) {
        text = replaceNCR( text ); //  將numeric character references全部還原
        text = replaceNewLine( text ); // 將換行tag轉換為換行字元
       text = replaceJS( text ); // 拿掉JS
       text = replaceStyle( text ); // 拿掉style
        //text = text.replaceAll( "<[^(img)|^(a)|^(/a)|^(b)|(/b)]{1}[^<>]+>", "" ); // 將所有標籤去除，只保留圖片標籤和超連結
        //text = text.replaceAll( "<[^(img)|^(a)|^(/a)]{1}[^<>]+>", "" ); // 將所有標籤去除，只保留圖片標籤和超連結
        //text = text.replaceAll( "</span>|</div>|</wbr>", "" ); // 將多餘的標籤去除
        text = text.replaceAll( "\n", "<br>" ); // 將換行符號還原回換行標籤
        
        return text;
    }
    
    // 取得文章前言，提供標題和網址的資訊
    public String getInformation( String title, String url ) {
        String aheadText = ""; // 文章前言，提供標題和網址的資訊
        
        if ( SetUp.getDefaultTextOutputFormat() == FileFormatEnum.HTML ) {
            aheadText = "<meta content='text/html; charset=UTF-8' http-equiv='Content-Type'/>";
            aheadText += "原文標題：" + title.replaceAll( "\\.html", "" ) + "\n";
            aheadText += "原文地址：" + "<a href=\"" + url + "\" target=_blank>" + url + "</a>";
            aheadText += "<hr>\n";
            aheadText = aheadText.replaceAll( "\n", "<br>" );
        }
        else {
            aheadText += "原文標題：" + title.replaceAll( "\\.txt", "" ) + "\r\n";
            aheadText += "原文地址：" + url + "\r\n\r\n";
        }
        
        return aheadText;
    }
}
// http://ascrsbdfdb.kukudm.net:81/kuku8comic8/201110/20111029/%E9%BC%A0%E7%B9%AA%E6%BC%A2%E5%8C%96%E7%BE%8E%E9%A3%9F163/Comic.kukudm.com_0103S.jpg
// http://cc.kukudm.com/kuku8comic8/201110/20111029/%E9%BC%A0%E6%95%B8%E6%95%B8%E5%8C%96%E7%BE%8E%E9%A3%9F163/Comic.kukudm.com_0103S.jpg
