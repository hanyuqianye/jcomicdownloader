/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2012/5/27
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  4.03: 1. 新增對xxbh的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Encoding;
import jcomicdownloader.encode.Zhcode;

public class ParseXXBH extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
    
    @author user
     */
    public ParseXXBH() {
        siteID = Site.XXBH;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_xxbh_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_xxbh_encode_parse_", "html" );

        jsName = "index_xxbh.js";
        radixNumber = 1591371; // default value, not always be useful!!

        baseURL = "http://comic.xxbh.net";
    }

    public ParseXXBH( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );
        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.GB2312 );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            // 因為正常解析不需要用到單集頁面，所以給此兩行放進來

            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

            int beginIndex = allPageString.indexOf( "</a>->" ) + 1;
            beginIndex = allPageString.indexOf( "</a>->", beginIndex ) + 6;
            int endIndex = allPageString.indexOf( "->", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex ).replaceAll( "&nbsp;", "" );

            setWholeTitle( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( tempTitleString.trim() ) ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        int beginIndex = 0;
        int endIndex = 0;
        String tempString = "";
        String allJSPageString = "";

        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );


        // 找出全部的伺服器位址
        Common.debugPrintln( "開始解析全部的伺服器位址" );
        // 首先要下載第二份js檔
        beginIndex = Common.getIndexOfOrderKeyword( allPageString, " src=", 5 );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        String jsURL2 = allPageString.substring( beginIndex, endIndex );

        // 開始解析js檔案
        allJSPageString = getAllPageString( jsURL2 );
        beginIndex = allJSPageString.indexOf( "Array()" );
        beginIndex = allJSPageString.indexOf( ";", beginIndex ) + 1;
        endIndex = allJSPageString.indexOf( "img_svr_eff", beginIndex );
        tempString = allJSPageString.substring( beginIndex, endIndex );

        // 取出全部的前面位址（伺服器位址+資料夾位置）
        int serverAmount = tempString.split( "http://" ).length - 1;
        String[] frontPicURLs = new String[serverAmount];

        beginIndex = endIndex = 0;
        for ( int i = 0 ; i < serverAmount ; i++ ) {
            beginIndex = tempString.indexOf( "http://", beginIndex );
            endIndex = tempString.indexOf( "\"", beginIndex );
            frontPicURLs[i] = tempString.substring( beginIndex, endIndex );
            beginIndex = endIndex;
        }

        Common.debugPrint( "開始解析這一集有幾頁 : " );

        // 首先要下載js檔
        beginIndex = Common.getIndexOfOrderKeyword( allPageString, " src=", 3 );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        String jsURL = allPageString.substring( beginIndex, endIndex );

        // 開始解析js檔案
        Common.debugPrintln( "開始解析後面部份的位址" );
        String referURL = webSite + "?page=1";
        Common.simpleDownloadFile( jsURL, SetUp.getTempDirectory(), indexName, referURL );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.GB2312 );
        allJSPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

        beginIndex = allJSPageString.indexOf( " msg" );
        beginIndex = allJSPageString.indexOf( "'", beginIndex ) + 1;
        endIndex = allJSPageString.indexOf( "'", beginIndex );
        tempString = allJSPageString.substring( beginIndex, endIndex );

        // 取得每張圖片網址的後面部份
        String[] backPicURLs = tempString.split( "\\|" );

        // 看有幾張圖片
        totalPage = backPicURLs.length;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        Common.debugPrintln( "開始解析前面部份的位址" );
        beginIndex = allJSPageString.indexOf( " img_s" );
        beginIndex = allJSPageString.indexOf( "=", beginIndex ) + 1;
        endIndex = allJSPageString.indexOf( ";", beginIndex );
        tempString = allJSPageString.substring( beginIndex, endIndex );
        int serverId = Integer.parseInt( tempString.trim() );

        Common.debugPrintln( "第一張圖片位址：" + frontPicURLs[serverId - 1] + backPicURLs[0] );

        beginIndex = endIndex = 0;
        for ( int p = 0 ; p < totalPage && Run.isAlive ; p++ ) {

            comicURL[p] = frontPicURLs[serverId - 1] + backPicURLs[p];

            //使用最簡下載協定，加入refer始可下載
            referURL = webSite + "?page=" + (p + 1);
            singlePageDownloadUsingSimple( getTitle(), getWholeTitle(),
                    comicURL[p], totalPage, p + 1, referURL );

            //Common.debugPrintln( ( p + 1 ) + " " + comicURL[p] + " " + referURL ); // debug
        }
        //System.exit( 0 ); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_xxbh_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_xxbh_encode_", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.GB2312 );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://comic.xxbh.net/201205/223578.html
        if ( Common.getAmountOfString( urlString, "/" ) > 3 ) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://comic.xxbh.net/201205/223578.html轉為
        //    http://comic.xxbh.net/colist_223560.html

        String allPageString = getAllPageString( volumeURL );

        int beginIndex = allPageString.indexOf( "<h1>" );
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
        int beginIndex = allPageString.indexOf( "<h1>" );
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "</h1>", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "class=\"ar_list_col\"" );
        int endIndex = allPageString.indexOf( "class=\"ass_list\"", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );

        int volumeCount = tempString.split( " href=" ).length - 1;
        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        String volumeURL = "";
        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for ( int i = 0 ; i < volumeCount ; i++ ) {
            // 取得單集位址
            beginIndex = tempString.indexOf( " href=", beginIndex );
            beginIndex = tempString.indexOf( "\"", beginIndex ) + 1;
            endIndex = tempString.indexOf( "\"", beginIndex );
            volumeURL = tempString.substring( beginIndex, endIndex );
            
            if ( !volumeURL.matches( ".*/s/.*" ) ) { // 代表有非集數的網址在亂入
                urlList.add( baseURL + volumeURL );
                // 取得單集名稱
                beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
                endIndex = tempString.indexOf( "</a>", beginIndex );
                volumeTitle = tempString.substring( beginIndex, endIndex );
                volumeTitle = volumeTitle.replaceAll( "<.*>", "" );
                volumeList.add( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( volumeTitle.trim() ) ) ) );
            }
        }

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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the xxbh module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
