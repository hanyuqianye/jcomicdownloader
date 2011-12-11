/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/11
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  2.06 : 1. 新增對baidu的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Zhcode;

public class ParseBAIDU extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;
    protected String firstServerURL = "http://comic.jmydm.net/"; // 電信一
    protected String secondServerURL = "http://zj.jmydm.net/"; // 電信二
    protected String thirdServerURL = "http://wt.jmydm.net:2012/"; // 網通

    /**
     *
     * @author user
     */
    public ParseBAIDU() {
        siteID = Site.BAIDU;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_baidu_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_baidu_encode_parse_", "html" );

        jsName = "index_baidu.js";
        radixNumber = 1844271; // default value, not always be useful!!

        baseURL = "http://tieba.baidu.com";
    }

    public ParseBAIDU( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            Common.errorReport( "不可能有這種情形" );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString;
        if ( existsTempFile() ) {
            allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );
        } else {
            allPageString = getAllPageString( webSite );
        }

        Common.debugPrint( "開始解析這一貼子有幾個分頁 : " );

        int beginIndex = allPageString.indexOf( "class=\"l_pager\"" );
        int endIndex = allPageString.indexOf( "</li>", beginIndex );
        
        String tempString = "";
        String basePageURL = "";
        int pageAmount = 0; 
        if ( beginIndex > 0 ) { // 有很多頁
            tempString = allPageString.substring( beginIndex, endIndex );

            beginIndex = tempString.lastIndexOf( "href=\"" ) + 6;
            endIndex = tempString.indexOf( "\"", beginIndex );
            System.out.println( beginIndex + " " + endIndex + " " + tempString.substring( beginIndex, endIndex ) );
            basePageURL = baseURL + tempString.substring( beginIndex, endIndex ).split( "=" )[0] + "=";
            pageAmount = Integer.parseInt( tempString.substring( beginIndex, endIndex ).split( "=" )[1] );
        }
        else 
            pageAmount = 1; // 只有一頁
        
        Common.debugPrintln( "此貼共有" + pageAmount + "頁" );

        int pageCount = 0;
        String tempComicURL = "";
        for ( int i = 0 ; i < pageAmount ; i++ ) {
            if ( i > 0 ) {
                allPageString = getAllPageString( basePageURL + (i + 1) );
            }

            // 第一頁之後若少於五張圖就不下載了
            if ( i > 0 && allPageString.split( "\"BDE_Image\"" ).length < 5 ) {
                break;
            }

            String[] tokens = allPageString.split( "\"" );
            for ( int j = 0 ; j < tokens.length ; j++ ) {
                if ( tokens[j].matches( "BDE_Image" ) ) {
                    while ( !tokens[j].matches( "(?s).*src=\\s*" ) ) {
                        j++;
                    }
                    tempComicURL += tokens[j + 1] + "####";
                }
            }

            String[] comicURL = tempComicURL.split( "####" );
            Common.debugPrintln( "此頁面解析得" + comicURL.length + "張圖" );
            int tempTotalPage = comicURL.length + pageCount;
            for ( int j = 0 ; j < comicURL.length ; j++ ) {
                singlePageDownload( getTitle(), getWholeTitle(), comicURL[j], tempTotalPage, pageCount + 1, 0 );

                pageCount++;
            }

            Common.debugPrintln( "第" + (i + 1) + "頁解析完畢" );
            Common.debugPrintln( "目前共下載" + pageCount + "頁" );
            tempComicURL = ""; // 歸零，讓下一頁使用

            //System.out.println( "TEMP_URL: " + tempComicURL ); // debug
        }

        comicURL = tempComicURL.split( "####" );
        totalPage = comicURL.length;//allPageString.split( "\"BDE_Image\"" ).length - 1; 
        Common.debugPrintln( "共 " + totalPage + " 頁" );

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
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_baidu_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_baidu_encode_", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Zhcode.GBK );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );
    }

    private boolean existsTempFile() {
        System.out.println( SetUp.getTempDirectory() + indexName );
        if ( new File( SetUp.getTempDirectory() + indexName ).exists() ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // baidu貼子都判斷為主頁面
        return false;
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        return volumeURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = 0;
        int endIndex = 0; 
        
        String title = "";
        if ( urlString.matches( "(?s).*/p/(?s).*" ) ) {
            beginIndex = allPageString.indexOf( "_" ) + 1;
            endIndex = allPageString.indexOf( "_", beginIndex );
            title = allPageString.substring( beginIndex, endIndex ).trim();
        }
        else {
            beginIndex = allPageString.indexOf( "<title>" ) + 7;
            endIndex = allPageString.indexOf( "_", beginIndex );
            title = allPageString.substring( beginIndex, endIndex ).trim();
        }

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( "[百度]" + title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = 0;
        int endIndex = 0;

        if ( urlString.matches( "(?s).*/p/(?s).*" ) ) { // 單一貼子
            totalVolume = 1;
            
            beginIndex = allPageString.indexOf( "<title>" ) + 7;
            endIndex = allPageString.indexOf( "_", beginIndex );
            String volumeTitle = allPageString.substring( beginIndex, endIndex ).trim();

            urlList.add( urlString );

            volumeList.add( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( volumeTitle ) ) );
        }
        else { // 貼子列表
            totalVolume = allPageString.split( "class=\"thread_title\"" ).length - 1;
            beginIndex = 0;
            String tempURL = "";
            String volumeTitle = "";
            for ( int i = 0; i < totalVolume; i ++ ) {
                beginIndex = allPageString.indexOf( "class=\"thread_title\"", beginIndex );
                beginIndex = allPageString.indexOf( "href=\"", beginIndex ) + 6;
                endIndex = allPageString.indexOf( "\"", beginIndex );
                tempURL = allPageString.substring( beginIndex, endIndex );
                
                if ( Common.isLegalURL( tempURL ) )
                    urlList.add( tempURL );
                else
                    urlList.add( baseURL + tempURL );
                
                beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
                endIndex = allPageString.indexOf( "<", beginIndex );
                volumeTitle = allPageString.substring( beginIndex, endIndex );
                
                volumeList.add( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( volumeTitle ) ) );
            }
        }

        
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
        System.out.println( "| Run the BAIDU module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
