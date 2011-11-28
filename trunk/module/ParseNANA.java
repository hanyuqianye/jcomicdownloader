/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/2
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  2.01: 1. 修復無法解析粗體字集數名稱的bug。
 *  2.0 : 1. 新增新增對www.nanadm.com的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;

public class ParseNANA extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseNANA() {
        siteID = Site.NANA;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_nana_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_nana_encode_parse_", "html" );

        jsName = "index_nana.js";
        radixNumber = 18522371; // default value, not always be useful!!

        baseURL = "http://www.nanadm.com";
    }

    public ParseNANA( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String allPageString = Common.getFileString( Common.tempDirectory, indexName );

            int beginIndex = allPageString.indexOf( "<h2>" ) + 4;
            int endIndex = allPageString.indexOf( "</h2>", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex );

            setWholeTitle( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( tempTitleString.trim() ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = Common.getFileString( Common.tempDirectory, indexName );
        Common.debugPrint( "開始解析這一集有幾頁 : " );

        int beginIndex = allPageString.indexOf( "class=\"pagelist\"" );
        beginIndex = allPageString.indexOf( "|", beginIndex );
        beginIndex = allPageString.indexOf( ": ", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "页", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex ).trim();

        totalPage = Integer.parseInt( tempString ) - 1; // 因為網頁騙人，少一頁....
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        // 開始取得第一頁網址 
        beginIndex = allPageString.lastIndexOf( "src=\"" ) + 5;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        String firstPageURL = getFixedChineseURL( allPageString.substring( beginIndex, endIndex ) );

        String extensionName = firstPageURL.split( "\\." )[firstPageURL.split( "\\." ).length - 1]; // 取得檔案副檔名
        NumberFormat formatter = new DecimalFormat( "000" ); // 預設000.jpg ~ xxx.jpg

        for ( int p = 1 ; p <= totalPage ; p++ ) {
            String fileNameBefore = formatter.format( p - 1 ) + "." + extensionName;
            String fileName = formatter.format( p ) + "." + extensionName;
            firstPageURL = comicURL[p - 1] = firstPageURL.replaceAll( fileNameBefore, fileName );
            //System.out.println( fileName + " " + fileNameBefore + " " +  comicURL[p-1] ); // debug
        }
        //System.exit(0); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_nana_", "html" );

        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );

        return Common.getFileString( Common.tempDirectory, indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*/\\d+\\.html(?s).*" ) ) // ex. http://www.nanadm.com/fgw/3151/32021.html
        {
            return true;
        } else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://www.nanadm.com/fgw/3151/32021.html轉為
        //     http://www.nanadm.com/fgw/3151/

        int endIndex = volumeURL.lastIndexOf( "/" ) + 1;
        String mainPageURL = volumeURL.substring( 0, endIndex );;

        return mainPageURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = allPageString.indexOf( "id=\"nryhw\"" );
        beginIndex = allPageString.indexOf( "</b>", beginIndex ) + 4;
        int endIndex = allPageString.indexOf( "</li>", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex ).replaceAll( "：", "" ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "id=\"zaixianmanhua\"" );
        beginIndex = allPageString.indexOf( "<ul>", beginIndex );
        int endIndex = allPageString.indexOf( "</ul>", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex );
        String[] tokens = tempString.split( ">|<|'" );

        int volumeCount = 0;

        for ( int i = 0 ; i < tokens.length ; i++ ) {
            if ( tokens[i].matches( "(?s).*\\.html(?s).*" )  ) {
                urlList.add( baseURL + tokens[i] );

                // 取得單集名稱
                String volumeTitle = tokens[i + 2];
                
                if ( volumeTitle.equals( "" ) ) // 中間插了一個<strong>
                    volumeTitle = tokens[i + 4];
                
                volumeList.add( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( volumeTitle.trim() ) ) );

                volumeCount++;
            }
        }

        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, Common.tempDirectory, Common.tempVolumeFileName );
        Common.outputFile( urlList, Common.tempDirectory, Common.tempUrlFileName );
    }

    @Override
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            |" );
        System.out.println( "| Run the NANA module:     |" );
        System.out.println( "|_______________________________|\n" );
    }
}
