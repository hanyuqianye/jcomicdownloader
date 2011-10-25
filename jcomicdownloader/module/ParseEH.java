/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.text.*;

public class ParseEH extends ParseOnlineComicSite {
    private String[] lines;
    private String[] comicPageURL;
    private int onePagePicCount;
    private int pageCount;
    
    private boolean needCookie; // 是否要設定cookie
    private String cookieString; // cookie要設定的參數

    /**
 *
 * @author user
 */
    public ParseEH() {
        siteID = Site.EH;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_e_Hentai_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_e_Hentai_encode_parse_", "html" );
        onePagePicCount = 20; // 20 pics on every page
        
        needCookie = false;
        cookieString = "";
    }

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

    public ParseEH( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
        //Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );
    }

    public void setParameters() { // let all the non-set attributes get values

        Common.slowDownloadFile( webSite, Common.tempDirectory, indexName, 1000, false, "" );
        
        String allPageString = Common.getFileString( Common.tempDirectory, indexName );
        
        
        if ( allPageString.matches( "(?s).*Content Warning(?s).*View Gallery(?s).*Never Warn Me Again(?s).*" ) ) {
            needCookie = true;
            Common.debugPrintln( "是警告畫面網頁，前去下載真實網頁" );
            cookieString = "nw=session";
            Common.slowDownloadFile( webSite, Common.tempDirectory, indexName, 1000, true, cookieString );
        }
        
        lines = Common.getFileStrings( Common.tempDirectory, indexName );

        Common.debugPrintln( "開始解析各參數 :" );
        Common.debugPrintln( "作品名稱(title) : " + title );
        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + wholeTitle );


        int beginIndex = 0;
        int endIndex = 0;
        for ( int i = 0; i < lines.length; i ++ ) {
            // ex. Showing 41 - 60 of 192 images
            if ( lines[i].matches( "(?s).*Showing(?s).*of(?s).*images(?s).*" ) ) {
                beginIndex = lines[i].indexOf( "Showing", 1 );
                beginIndex = lines[i].indexOf( "of", beginIndex ) + 2;
                endIndex = lines[i].indexOf( "images", beginIndex );

                totalPage = Integer.valueOf( lines[i].substring( beginIndex, endIndex ).replaceAll( ",", "" ).trim() );
                Common.debugPrintln( "總共頁數 : " + totalPage );
            }
        }

        if ( totalPage % onePagePicCount == 0 )
            pageCount = totalPage / onePagePicCount;
        else
            pageCount = totalPage / onePagePicCount + 1;
        Common.debugPrintln( "共分幾個頁面 : " + pageCount );

        comicPageURL = new String [totalPage];
        comicURL = new String [totalPage]; // totalPage = amount of comic pic
        SetUp.setWholeTitle( wholeTitle );

        setDownloadDirectory( getDefaultDownloadDirectory() );
    }


    public void parseComicURL() { // parse URL and save all URLs in comicURL
        int beginIndex = 0;
        int endIndex = 0;

        int pageUrlCount = 0; // for page url

        // get URLs of pic page
        for ( int pageNumber = 0; pageNumber <= pageCount && Run.isAlive; pageNumber ++ ) {
            if ( pageNumber >= 1 ) {
                Common.slowDownloadFile( webSite + "?p=" + pageNumber, Common.tempDirectory, indexName, 1000, true, cookieString );
                lines = Common.getFileStrings( Common.tempDirectory, indexName );
            }



            int i = 0;
            for ( i = 0; i < lines.length; i ++ ) {
                // ex. <div class="gdtm" style="height:170px">
                if ( lines[i].matches( "(?s).*class=\"gdtm\"(?s).*" ) ) {
                    beginIndex = lines[i].indexOf( "class=\"gdtm\"", 1 );
                    break;
                }
            }

            System.out.println( "----------------" );
            for ( int count = 0; count < onePagePicCount && Run.isAlive; count ++ ) {
                beginIndex = lines[i].indexOf( "http://g.e-hentai.org", beginIndex );
                endIndex = lines[i].indexOf( "\"><img alt=\"", beginIndex );

                //System.out.println( count + " = " + beginIndex + "   "  + endIndex );
                if ( beginIndex > 0 && endIndex > 0 && pageUrlCount < totalPage ) {
                    comicPageURL[pageUrlCount] = lines[i].substring( beginIndex, endIndex );
                    Common.debugPrintln( "第" + ( pageUrlCount + 1 ) + "頁網址：" + comicPageURL[pageUrlCount] );
                    pageUrlCount ++;
                }
                beginIndex = endIndex;
            }

        }


        // get URLs of real pic
        for ( int i = 0; i < totalPage && Run.isAlive; i ++ ) {
            Common.slowDownloadFile( comicPageURL[i], Common.tempDirectory, indexName, 1000, false, "" );
            String line = Common.getFileString( Common.tempDirectory, indexName );

            lines = line.split( "\"" );
            for ( int j = 0; j < lines.length; j ++ ) {

                if ( lines[j].matches( "(?s).*http://\\d+.\\d+.\\d+.\\d+(?s).*" ) ) {

                    comicURL[i] = lines[j].replaceAll( "amp;", "" );
                    System.out.println( comicURL[i] );

                    // Not waiting for url analysing done, start to download
                    String[] tempStrings = comicURL[i].split( "/|\\." );
                    String extensionName = tempStrings[tempStrings.length-1];
                    NumberFormat formatter = new DecimalFormat( Common.getZero() );
                    String fileName = formatter.format( i + 1 ) + "." + extensionName;
                    String nextFileName = formatter.format( i + 2 ) + "." + extensionName;

                    //SetUp.setDownloadDirectory( getDefaultDownloadDirectory() );

                    // set stateBar and direct download
                    CommonGUI.stateBarMainMessage = title + " : ";
                    CommonGUI.stateBarDetailMessage = "共" + totalPage + "頁，第" + ( i + 1 ) + "頁下載中";

                    if ( SetUp.getShowDoneMessageAtSystemTray() && Common.withGUI() )
                        ComicDownGUI.trayIcon.setToolTip( CommonGUI.stateBarMainMessage +
                                                          CommonGUI.stateBarDetailMessage );

                    CommonGUI.stateBarDetailMessage += ": [" + fileName + "]";

                    // 下載第n張之前，先檢查第n+1張圖是否存在，若是則跳下一張
                    if ( !new File( getDownloadDirectory() + nextFileName ).exists() )
                        Common.slowDownloadFile( comicURL[i], getDownloadDirectory(), fileName, 2000, false, "" );

                    break;
                }

            }

        }

        if ( Flag.downloadingFlag && Common.withGUI() )
            ComicDownGUI.stateBar.setText( title + "下載完畢 !!" );

    }


    public void showParameters() { // for debug

    }

    public String getDefaultDownloadDirectory() {
        String path = title + Common.getSlash();
        //Common.debugPrintln( "目前下載路徑：" + path );
        return path;
    }


    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_EH_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_EH_encode_", "html" );

        Common.slowDownloadFile( urlString, Common.tempDirectory, indexName, 1000, false, "" );

        return Common.getFileString( Common.tempDirectory, indexName );
    }

    // 預設EH沒有單集頁面，只有主頁面。這樣可以讓單集也能加入任務
    public boolean isSingleVolumePage( String urlString ) {
        return false;
    }

    // 判斷是否為單集主頁面（就是有單集內容預覽圖的頁面）
    public boolean isRealSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*g.e-hentai.org/g/\\d+(?s).*" ) )
            return true;
        else
            return false;
    }

    public String getTitleOnSingleVolumePage( String urlString ) {

        String allPageString = getAllPageString( urlString );
        String[] lines = allPageString.split( "\n" );
        int beginIndex = 0;
        int endIndex = 0;
        String titleString = "";

        for ( int i = 0; i < lines.length; i ++ ) {
            if ( lines[i].matches( "(?s).*<title>(?s).*" ) ) {
                System.out.println( lines[i] );
                beginIndex = lines[i].indexOf( "title", 0 ) + 6;
                endIndex = lines[i].indexOf( "E-Hentai", beginIndex ) - 3;

                titleString = lines[i].substring( beginIndex, endIndex );

                break;
            }
        }

        return Common.getStringRemovedIllegalChar( titleString );
    }
    public String getTitleOnSingleVolumePageByAllPageString( String allPageString ) {

        String[] lines = allPageString.split( "\n" );
        int beginIndex = 0;
        int endIndex = 0;
        String titleString = "";

        for ( int i = 0; i < lines.length; i ++ ) {
            if ( lines[i].matches( "(?s).*<title>(?s).*" ) ) {
                System.out.println( lines[i] );
                beginIndex = lines[i].indexOf( "title", 0 ) + 6;
                endIndex = lines[i].indexOf( "E-Hentai", beginIndex ) - 3;

                titleString = lines[i].substring( beginIndex, endIndex );

                break;
            }
        }

        return Common.getStringRemovedIllegalChar( titleString );
    }


    public int getTitleCountOnMainPage( String allPageString  ) {
        return allPageString.split( "gtr\\d" ).length - 1;
    }

    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        if ( isRealSingleVolumePage( urlString ) ) {
            // 網址連結到的是單集主頁面（就是有單集內容預覽圖的頁面）

            urlList.add( urlString );
            volumeList.add( getTitleOnSingleVolumePageByAllPageString( allPageString ) );
        }
        else { // 網址連結到的是搜索頁面或是標籤頁面（就是只有很多作品標題的頁面）
            int beginIndex = allPageString.indexOf( "Published", 0 );
            int titleCount = getTitleCountOnMainPage( allPageString );
            String[] tokenStrings = allPageString.substring( beginIndex, allPageString.length() ).split( "\"|>|<" );

            int mainPageIndex = 0;
            for ( int i = 0; i < titleCount; i ++ ) {
                while ( !tokenStrings[mainPageIndex].matches( "http://g.e-hentai.org/g/\\d+/(?s).*" ) )
                    mainPageIndex ++;

                urlList.add( tokenStrings[mainPageIndex] );

                mainPageIndex ++;

                while ( !tokenStrings[mainPageIndex].equals( urlList.get( i ) ) )
                    mainPageIndex ++;

                volumeList.add( tokenStrings[mainPageIndex+2] );

                Common.debugPrint( i + " 標題 : " + volumeList.get( i ) );
                Common.debugPrintln( "     網址 : " + urlList.get( i ) );

                mainPageIndex ++;
            }
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, Common.tempDirectory, Common.tempVolumeFileName );
        Common.outputFile( urlList, Common.tempDirectory, Common.tempUrlFileName );
    }

    public String getTitleOnMainPage( String urlString, String allPageString ) {
        //setTitle( "E-Hentai" );
        if ( isRealSingleVolumePage( urlString ) ) {
            String title = getTitleOnSingleVolumePageByAllPageString( allPageString );
            setWholeTitle( title );
            return title;
        }
        else {
            String title = "E-Hentai_Collection";
            setTitle( title );
            return title;
        }
    }
    
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName };
    }

    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            |" );
        System.out.println( "| Run the e-Hentai module: |" );
        System.out.println( "|_______________________________|\n" );
    }
}