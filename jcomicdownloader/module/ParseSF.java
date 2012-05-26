/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/3
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  2.02: 1. 新增對www.sky-fire.com的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;

public class ParseSF extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseSF() {
        siteID = Site.SF;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_sf_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_sf_encode_parse_", "html" );

        jsName = "index_sf.js";
        radixNumber = 159371; // default value, not always be useful!!

        baseURL = "http://coldpic.sfacg.com";
    }

    public ParseSF( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );

            int beginIndex = allPageString.indexOf( "content=\"" ) + 9;
            int endIndex = allPageString.indexOf( ",", beginIndex );
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

        // 取得js位址
        int beginIndex = allPageString.indexOf( "src=\"" ) + 1;
        beginIndex = allPageString.indexOf( "src=\"", beginIndex ) + 5;
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        
        String jsBaseURL = webSite.substring( 0, webSite.indexOf( "/AllComic/" ) );
        String jsURL = jsBaseURL + allPageString.substring( beginIndex, endIndex );
        Common.debugPrintln( "JS檔位址：" + jsURL );
        
        // 取得js檔內容
        allPageString = getAllPageString( jsURL );
        
        beginIndex = allPageString.indexOf( "Array()" );
        String tempString = allPageString.substring( beginIndex, allPageString.length() );

        totalPage = tempString.split( "http://" ).length - 1;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        String[] urlTokens = tempString.split( "\"" );
        
        int p = 0; // 目前頁數
        for ( int i = 0 ; i < urlTokens.length && Run.isAlive; i++ ) {
            if ( urlTokens[i].matches( "http://(?s).*" ) ) {
                comicURL[p++] = urlTokens[i]; // 存入每一頁的網頁網址
                //Common.debugPrintln( p + " " + comicURL[p-1]  ); // debug
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

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_sf_", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        return Common.getFileString( SetUp.getTempDirectory(), indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://coldpic.sfacg.com/AllComic/554/030/
        if ( urlString.matches( "(?).*/AllComic/(?).*" ) ) {
            return true;
        } else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://www.citymanga.com/sexy_commando_gaiden_sugoiyo_masaru-san/chapter-71/轉為
        //    http://www.citymanga.com/sexy_commando_gaiden_sugoiyo_masaru-san/

        String allPageString = getAllPageString( volumeURL );

        int beginIndex = allPageString.indexOf( "http://comic.sfacg.com/" ) + 1;
        beginIndex = allPageString.indexOf( "http://comic.sfacg.com/", beginIndex );
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        String mainPageURL = allPageString.substring( beginIndex, endIndex );

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
        int beginIndex = allPageString.indexOf( "<title>" ) + 7;
        int endIndex = allPageString.indexOf( ",", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex );
        
        System.out.println( "XXXX:" + title );

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "class=\"serialise_list Blue_link2\"" );
        int endIndex = allPageString.indexOf( "</ul>", beginIndex );
        
        String tempString = allPageString.substring( beginIndex, endIndex );

        int volumeCount = tempString.split( "href=\"" ).length - 1;

        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for ( int i = 0 ; i < volumeCount ; i++ ) {
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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the SF module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
