/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2012/5/13
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
     5.03: 修復178部分無法下載的問題。
    4.19: 修復178因網址改變而無法下載的問題。
    4.0 : 1. 翻新178的解析方式，直接轉碼而非參照字典檔。
 *  2.17: 1. 修復檔名含有中文就會解析錯誤的bug。
 *  2.16: 1. 修復少數檔名解析錯誤的bug。
 *  2.11: 1. 修復有時候下載網頁發生錯誤的問題。
 *  2.10: 1. 新增manhua.178.com的支援。
 ----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.module.ParseOnlineComicSite;

public class Parse178 extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;
    protected int waitingTime; // 下載錯誤後的等待時間
    protected int retransmissionLimit; // 最高重試下載次數
    // 用於存放網址中已經解碼的編碼字串和解碼字串
    protected List<String> codeList = new ArrayList<String>();
    protected List<String> decodeList = new ArrayList<String>();

    /**

     @author user
     */
    public Parse178() {
        siteID = Site.MANHUA_178;
        siteName = "178";
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_178_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_178_encode_parse_", "html" );

        jsName = "index_178.js";
        radixNumber = 1593771; // default value, not always be useful!!

        baseURL = "http://manhua.178.com/";
        waitingTime = 2000;
        retransmissionLimit = 30;
    }

    public Parse178( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );
        Common.downloadGZIPInputStreamFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );

            int beginIndex = allPageString.indexOf( "g_chapter_name" );
            beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
            int endIndex = allPageString.indexOf( "\"", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex );

            setWholeTitle( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                Common.getTraditionalChinese( tempTitleString.trim() ) ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );
        Common.debugPrint( "開始解析這一集有幾頁 : " );

        // 取得所有位址編碼
        int beginIndex = allPageString.indexOf( "'[" ) + 2;
        int endIndex = allPageString.indexOf( "';", beginIndex ) - 1;

        String allCodeString = allPageString.substring( beginIndex, endIndex );

        totalPage = allCodeString.split( "\",\"" ).length;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        String basePicURL = "http://imgfast.manhua.178.com/";
        String[] codeTokens = allCodeString.split( "\",\"" );
        String firstCode = codeTokens[0].replaceAll( "\"", "" );

        String firstPicURL = "";
        Common.debugPrintln( "第一張編碼：" + firstCode );
        firstPicURL = basePicURL + Common.getFixedChineseURL( getDecodeURL( firstCode ) );
        firstPicURL = firstPicURL.replaceAll( "\\\\", "" );
        
        Common.debugPrintln( "第一張圖片網址：" + firstPicURL );
        
        
        //System.exit( 0 );

        String[] picNames = new String[totalPage];
        for ( int i = 0; i < picNames.length; i++ ) {
            codeTokens[i] = codeTokens[i].replaceAll( "\"", "" );
            beginIndex = codeTokens[i].lastIndexOf( "/" ) + 1;
            endIndex = codeTokens[i].length(); //.lastIndexOf( "\"" );
            //Common.debugPrintln( codeTokens[i] + " " + beginIndex + " " + endIndex );
            picNames[i] = Common.getFixedChineseURL(
                getDecodeURL( codeTokens[i].substring( beginIndex, endIndex ) ) );

            //System.exit( 0 ); // debug
        }

        endIndex = firstPicURL.lastIndexOf( "/" ) + 1;
        String parentPicURL = firstPicURL.substring( 0, endIndex );

        for ( int i = 0; i < codeTokens.length && Run.isAlive; i++ ) {
            comicURL[i] = parentPicURL + picNames[i]; // 存入每一頁的網頁網址
            //Common.debugPrintln( ( i + 1 ) + " " + comicURL[i]  ); // debug

        }

        //System.exit( 0 ); // debug
    }
    
    // 回傳utf8編碼的十進位數字
    // ex. 0030 -> 48
    private char getUtf8Char( String code ) {
        int number = 0; 
        
        // 十六位元數字的字串
        String hexString = code.substring( 2, code.length() ); // 去對前面的\\u

        return (char) Integer.parseInt( hexString,16 );
    }
    
    // 解析網址 
    // ex. 將t\/THREAD\/\u7b2c01\u8bdd\/001.jpg解析為正常網址
    private String getDecodeURL( String code ) {
        
        int beginIndex = 0;
        int endIndex = 0;
        
        char ch = ' ';
        String singleCode = ""; // 單一字的編碼

        while ( true ) {
            beginIndex = code.indexOf( "\\u" );
            if ( beginIndex < 0 ) { 
                break; // 已找不到需要解碼的部份，跳出迴圈
            }
            else {
                endIndex = beginIndex + 6;
                singleCode = code.substring( beginIndex, endIndex );
                ch = getUtf8Char( singleCode ); // 取得utf8原始字

                code = code.replace( singleCode, String.valueOf( ch ) ); // 替代該字
                System.out.println( beginIndex + " " + endIndex + "\t: " + code );
            }
        }

        return code;
    }


    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_178_", "html" );
        Common.downloadGZIPInputStreamFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        return Common.getFileString( SetUp.getTempDirectory(), indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://www.kkkmh.com/manhua/0804/9119/65867.html
        if ( urlString.matches( "(?s).*\\d.shtml" ) ||
             urlString.matches( "(?s).*\\d.html" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://www.178.com/mh/kongjuzhiyuan/16381-2.shtml轉為
        //    http://manhua.178.com/kongjuzhiyuan/

        String allPageString = getAllPageString( volumeURL );

        int beginIndex = allPageString.indexOf( "g_comic_url" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        String mainPageURL = baseURL + allPageString.substring( beginIndex, endIndex );

        Common.debugPrintln( "MAIN_URL: " + mainPageURL );

        return mainPageURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = allPageString.indexOf( "<h1>" ) + 4;
        int endIndex = allPageString.indexOf( "</h1>", beginIndex );
        Common.debugPrintln( "B: " + beginIndex + "  E: " + endIndex );
        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "class=\"cartoon_online_border\"" );
        int endIndex = allPageString.indexOf( "document.write", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );

        int volumeCount = tempString.split( "href=\"" ).length - 1;

        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for ( int i = 0; i < volumeCount; i++ ) {
            // 取得單集位址
            beginIndex = tempString.indexOf( "href=\"", beginIndex ) + 6;
            endIndex = tempString.indexOf( "\"", beginIndex );
            urlList.add( tempString.substring( beginIndex, endIndex ) );

            // 取得單集名稱
            beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
            endIndex = tempString.indexOf( "<", beginIndex );
            volumeTitle = tempString.substring( beginIndex, endIndex );

            volumeList.add( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                Common.getTraditionalChinese( volumeTitle.trim() ) ) ) );

        }

        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, SetUp.getTempDirectory(), Common.tempVolumeFileName );
        Common.outputFile( urlList, SetUp.getTempDirectory(), Common.tempUrlFileName );
    }

    @Override
    public String[] getTempFileNames() {
        return new String[]{indexName, indexEncodeName, jsName};
    }
}
