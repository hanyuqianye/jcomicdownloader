/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/10/25
 ----------------------------------------------------------------------------------------------------
 ChangeLog:

----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.text.*;

public class ParseCC extends ParseOnlineComicSite {
    private int radixNumber; // use to figure out the name of pic
    private String volpic; // pic url: http://~/2011/111/dagui/06/
    private int tpf2; // pic url: http://pic"tpf2".89890.com/~
    private int tpf; // length of pic name: (tpf+1)*2
    private String jsName;
    private String indexName;
    private String indexEncodeName;

    /**
 *
 * @author user
 */
    public ParseCC() {
        siteID = Site.CC;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_kuku_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_kuku_encode_parse_", "html" );

        jsName = "index_cc.js";
        radixNumber = 18527; // 大部份的值，後來也發現有少數19527
    }

    public ParseCC( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }


    @Override
    public void setParameters() { // let all the non-set attributes get values
        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        Common.downloadFile( webSite + "index.js", Common.tempDirectory, jsName, false, "" );
        Common.debugPrintln( "開始解析各參數 :" );
        try {
            BufferedReader br = Common.getBufferedReader( Common.tempDirectory + jsName );

            String line = br.readLine(); // it is only one line

            Scanner scanner = new Scanner( line ).useDelimiter("\\s*=+\\s*'*\\s*|\\s+|'|;");

            while ( scanner.hasNext() ) {
                //Common.debugPrintln( scanner.next() );

                String str = scanner.next();
                if ( str.equals( "volpic" ) )
                    volpic = scanner.next();
                else if ( str.equals( "total" ) )
                    totalPage = scanner.nextInt();
                else if ( str.equals( "tpf" ) )
                    tpf = scanner.nextInt();
                else if ( str.equals( "tpf2" ) )
                    tpf2 = scanner.nextInt();
            }

            br.close();
        } catch ( IOException e ) { e.printStackTrace(); }

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.debugPrintln( "作品名稱(title) : " + title );


        String tempStr = Common.getFileString( Common.tempDirectory, indexEncodeName );
        String[] lines = tempStr.split( "\n" );

        int wholeTitleIndex = 0;
        while ( !lines[wholeTitleIndex].matches( "(?s).*<title>(?s).*" ) )
            wholeTitleIndex ++;

        int beginIndex = lines[wholeTitleIndex].indexOf( ">", 1 ) + 1;
        int endIndex = lines[wholeTitleIndex].indexOf( "_", beginIndex );

        if ( getWholeTitle() == null || getWholeTitle().equals(  "" ) ) {
            String wholeTitle = Common.getTraditionalChinese( lines[wholeTitleIndex].substring( beginIndex, endIndex ) );
            setWholeTitle( Common.getStringRemovedIllegalChar( wholeTitle ) );
        }
        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + getWholeTitle() );

        comicURL = new String [totalPage]; // totalPage = amount of comic pic
        SetUp.setWholeTitle( wholeTitle );

    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL

        String picDir = "http://pic" + tpf2 + ".89890.com/" + volpic;

        String zeroNumber = "";
        for ( int i = 0; i < tpf + 1; i ++ )
            zeroNumber += "0";

        NumberFormat formatter = new DecimalFormat( zeroNumber );

        if ( !Common.urlIsOK( picDir + formatter.format( radixNumber ) + formatter.format( 1 ) + ".jpg" ) )
            radixNumber = 19527; // 18527不行就換19527
        if ( !Common.urlIsOK( picDir + formatter.format( radixNumber ) + formatter.format( 1 ) + ".jpg" ) )
            radixNumber = 18828; // 19527不行就換18828
            
        for ( int i = 1; i <= totalPage; i ++ ) {
            String frontName = formatter.format( i * radixNumber );
            String rearName = formatter.format( i );
            String picName = frontName + rearName + ".jpg";

            comicURL[i-1] = picDir + picName;
            Common.debugPrintln( comicURL[i-1] );
        }
        //System.exit( 1 );
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "volpic = " + volpic );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "tpf  = " + tpf );
        Common.debugPrintln( "tpf2  = " + tpf2 );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_CC_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_CC_encode_", "html" );

        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        return Common.getFileString( Common.tempDirectory, indexEncodeName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.split( "/" ).length == 6  ) // ex. http://www.89890.com/comic/7953/
            return true;
        else
            return false;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String[] splitURL = urlString.split( "/" );

        String newUrlString = "";
        for ( int i = 0; i < 5; i ++ )
            newUrlString += splitURL[i] + "/";

        return getTitleOnMainPage( urlString, getAllPageString( newUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {


        String[] lines = allPageString.split( "\n" );

        int titleIndex = 0;
        while ( !lines[titleIndex].matches( "(?s).*title(?s).*" ) )
            titleIndex ++;

        int beginIndex = lines[titleIndex].indexOf( "<title>", 1 );
        int endIndex = lines[titleIndex].indexOf( "_", beginIndex );

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( lines[titleIndex].substring( beginIndex + 8, endIndex ) ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        String[] lines = allPageString.split( "\n" );

        int urlIndex = 0;
        while ( !lines[urlIndex].matches( "(?s).*href = '(?s).*" ) )
            urlIndex ++;

        int beginIndex = 0;
        int endIndex = 0;

        boolean over = false;
        while ( !over ) {
            beginIndex = lines[urlIndex].indexOf( "href = '", endIndex );
            endIndex = lines[urlIndex].indexOf( "'", beginIndex + 8 );

            if ( beginIndex == -1 )
                break;

            String htmFileName = Common.getTraditionalChinese( lines[urlIndex].substring( beginIndex + 8, endIndex ) );
            urlList.add( urlString + htmFileName );
            //Common.debugPrint( urlString + htmFileName + " " );

            beginIndex = lines[urlIndex].indexOf( ">", endIndex );
            endIndex = lines[urlIndex].indexOf( "<", beginIndex );

            String volumeTitle = Common.getTraditionalChinese( lines[urlIndex].substring( beginIndex + 1, endIndex ) );
            volumeList.add( getVolumeWithFormatNumber( volumeTitle ) );
            Common.debugPrint( volumeTitle + "  " );
        }

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
        System.out.println( " ________________________" );
        System.out.println( "|                      |" );
        System.out.println( "| Run the CC module: |" );
        System.out.println( "|_________________________|\n" );
    }
}
