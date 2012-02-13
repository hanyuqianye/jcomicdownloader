/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/27
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  2.13: 1. 新增對mh.emland.net的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Encoding;

public class ParseCK extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseCK() {
        siteID = Site.CK;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_encode_parse_", "html" );

        jsName = "index_ck.js";
        radixNumber = 151261471; // default value, not always be useful!!

        baseURL = "http://comic.ck101.com";
    }

    public ParseCK( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );
        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        
        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            Common.debugPrintln( "開始解析title和wholeTitle :" );
            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

            int beginIndex = allPageString.indexOf( "alt=" );
            beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
            int endIndex = allPageString.indexOf( "\"", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex ).trim();

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

        int beginIndex = 0, endIndex = 0;

        totalPage = allPageString.split( "<option " ).length / 2;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        String picURL = "";
        int p = 0; // 目前頁數
        for ( int i = 0 ; i < totalPage ; i++ ) {
            beginIndex = allPageString.indexOf( "<img id" );
            beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
            endIndex = allPageString.indexOf( "\"", beginIndex );

            comicURL[p++] = allPageString.substring( beginIndex, endIndex );
            Common.debugPrintln( p + " " + comicURL[p - 1] ); // debug

            // 每解析一個網址就下載一張圖
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, 0 );
            
            //Common.downloadFile( comicURL[p - 1], "", p + ".jpg", false, "" );
            
            
            if ( p < totalPage ) {
                beginIndex = allPageString.indexOf( "</select>" );
                beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
                endIndex = allPageString.indexOf( "\"", beginIndex );

                String nextPageURL = baseURL + allPageString.substring( beginIndex, endIndex );
                
                Common.downloadFile( nextPageURL, SetUp.getTempDirectory(), indexName, false, "" );
                allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );
            }
        }

        //System.exit( 0 ); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override // 因為原檔就是utf8了，所以無須轉碼
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_ck_", "html" );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        return Common.getFileString( SetUp.getTempDirectory(), indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://comic.ck101.com/page/1749372
        String allPageString = getAllPageString( urlString );

        if ( urlString.matches( ".*/page/.*" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://comic.ck101.com/page/1749372轉為
        //    http://comic.ck101.com/comic/7039

        String allPageString = getAllPageString( volumeURL );

        int beginIndex = allPageString.lastIndexOf( "class=\"page_title\"" );
        beginIndex = allPageString.lastIndexOf( "href=", beginIndex );
        beginIndex = allPageString.lastIndexOf( "\"", beginIndex ) + 1;
        int endIndex = allPageString.lastIndexOf( "\"", beginIndex );
        String mainPageURL = baseURL + volumeURL.substring( beginIndex, endIndex ).trim();

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
        int beginIndex = allPageString.indexOf( "<strong>" );
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "</strong>", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex ).split( "/" )[0].trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        String tempString = "";
        int beginIndex, endIndex;

        beginIndex = allPageString.indexOf( "class=\"page\"" ) + 1;
        endIndex = allPageString.indexOf( "</div>", beginIndex );
        // 存放一整頁面集數資訊的字串
        tempString = allPageString.substring( beginIndex, endIndex );

        String[] pageStrings = tempString.split( "\"" );

        String lastPageURL = "";

        for ( int i = 0 ; i < pageStrings.length - 2 ; i++ ) {
            if ( pageStrings[i].matches( ".* href=.*" )
                    && !pageStrings[i + 2].matches( "(?s).*title=(?s).*" ) ) {
                lastPageURL = baseURL + pageStrings[i + 1];
            }
        }

        beginIndex = lastPageURL.lastIndexOf( "/" ) + 1;
        endIndex = lastPageURL.length();
        // ex. http://comic.ck101.com/comic/170/0/0/14 -> 14
        int lastPage;
        
        if ( tempString.matches( "(?s).*href=(?s).*" ) )
            lastPage = Integer.parseInt( lastPageURL.substring( beginIndex, endIndex ) );
        else
            lastPage = 1;
        
        // ex. http://comic.ck101.com/comic/170/0/0/2
        String basePageURL = lastPageURL.substring( 0, beginIndex );
        
        Common.debugPrint( "基本頁數位址: " + basePageURL );
        Common.debugPrintln( "   共有 " + lastPage + " 頁" );

        int totalVolumeCount = 0; // 每一頁加總起來的集數總量

        int p = 2;
        while ( true ) {
            beginIndex = allPageString.indexOf( "<ul>" ) + 1;
            endIndex = allPageString.indexOf( "</ul>", beginIndex );

            // 存放集數頁面資訊的字串
            tempString = allPageString.substring( beginIndex, endIndex );

            int volumeCount = tempString.split( "<a href=" ).length - 1; // 單一頁面的集數
            totalVolumeCount += volumeCount;

            String volumeTitle = "";
            beginIndex = endIndex = 0;
            for ( int i = 0 ; i < volumeCount ; i++ ) {
                // 取得單集位址
                beginIndex = tempString.indexOf( "<a href=", beginIndex );
                beginIndex = tempString.indexOf( "\"", beginIndex ) + 1;
                endIndex = tempString.indexOf( "\"", beginIndex );
                urlList.add( baseURL + tempString.substring( beginIndex, endIndex ) );

                // 取得單集名稱
                beginIndex = tempString.indexOf( "title=", beginIndex );
                beginIndex = tempString.indexOf( "\"", beginIndex ) + 1;
                endIndex = tempString.indexOf( "\"", beginIndex );
                volumeTitle = tempString.substring( beginIndex, endIndex );

                volumeList.add( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( volumeTitle.trim() ) ) ) );

            }
            
            if ( p <= lastPage ) {
                allPageString = getAllPageString( basePageURL + p );
                p ++;
            }
            else
                break;
        }

        totalVolume = totalVolumeCount;
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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the CK101 module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
