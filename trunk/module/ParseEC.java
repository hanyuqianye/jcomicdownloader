/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/10/25
----------------------------------------------------------------------------------------------------
ChangeLog:
2.09: 新增對6comic.com的支援。
1.17: 修復集數名稱後面數字會消失的bug。
1.08: 增加對於8comic的支援，包含免費漫畫和圖庫
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.text.*;

public class ParseEC extends ParseOnlineComicSite {

    protected int radixNumber; // use to figure out the name of pic
    protected String volpic; // pic url: http://~/2011/111/dagui/06/
    protected int tpf2; // pic url: http://pic"tpf2".89890.com/~
    protected int tpf; // length of pic name: (tpf+1)*2
    protected String jsName;
    protected String indexName;
    protected String indexEncodeName;
    private String volumeNoString; // 每一集都有數字編號
    private String itemid; // 每本漫畫的編號

    /**
     *
     * @author user
     */
    public ParseEC() {
        siteID = Site.EIGHT_COMIC;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_encode_parse_", "html" );

        jsName = "index_8comic.js";
        radixNumber = 18527345; // default value, not always be useful!!
        volumeNoString = "";
        itemid = "";
    }

    public ParseEC( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() { // let all the non-set attributes get values
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexName );

        // ex. http://www.8comic.com/love/drawing-8170.html?ch=3
        volumeNoString = webSite.split( "/|=" )[webSite.split( "/|=" ).length - 1];

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            setWholeTitle( getTitle() + volumeNoString );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得前面的下載伺服器網址
        String[] lines = Common.getFileStrings( SetUp.getTempDirectory(), indexName );

        int itemidIndex = 0;
        int index = 0;
        for ( int i = 0 ; i < lines.length ; i++ ) {
            if ( lines[i].matches( "(?s).*var itemid=(?s).*" ) ) {
                itemidIndex = i;
            } else if ( lines[i].matches( "(?s).*var codes=(?s).*" ) ) {
                index = i;
            }
        }
        itemid = lines[itemidIndex].split( ";|=" )[1];

        String[] parses = lines[index].split( "\"|\\|" ); // 除了第一個和最後一個外都是解析碼

        int order = 0;
        for ( int i = 0 ; i < parses.length ; i++ ) {
            if ( parses[i].split( " " )[0].equals( volumeNoString ) ) {
                order = i;
                break;
            }
        }

        String[] codes = parses[order].split( " " );

        // ex. http://img"+sid+".8comic.com/"+did+"/"+itemid+"/"+num+"/"+img+".jpg"
        // 找出網址
        String num = codes[0];
        String sid = codes[1];
        String did = codes[2];
        String page = codes[3];
        String code = codes[4];

        Common.debugPrint( "開始解析這一集有幾頁 :" );
        totalPage = Integer.valueOf( page );
        comicURL = new String[totalPage];
        Common.debugPrint( "共" + totalPage + "頁" );

        // 完全引用網頁上的javascript碼
        String img = "";
        for ( int p = 1 ; p <= totalPage ; p++ ) {
            if ( p < 10 ) {
                img = "00" + p;
            } else if ( p < 100 ) {
                img = "0" + p;
            } else {
                img = "" + p;
            }

            int m = ((p - 1) / 10) % 10 + (((p - 1) % 10) * 3);
            img += "_" + code.substring( m, m + 3 );

            comicURL[p - 1] = "http://img" + sid + ".8comic.com/" + did + "/"
                    + itemid + "/" + num + "/" + img + ".jpg";
            //Common.debugPrintln( p + " " + comicURL[p-1] );
        }
        //System.exit(0);
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

    @Override // 因為原檔就是utf8了，所以無須轉碼
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_encode_", "html" );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeBIG5File( SetUp.getTempDirectory(), indexName, indexEncodeName );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName ).replace( "&#22338;", "阪" );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*love(?s).*drawing(?s).*" ) ) // ex. http://www.8comic.com/love/drawing-2245.html?ch=51
        {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        // http://www.8comic.com/love/drawing-8170.html?ch=2轉為http://www.8comic.com/html/8170.html

        String[] splitURLs = urlString.split( "://|/|-|\\?" );

        String baseURL = "http://www.8comic.com/html/";
        String mainPageUrlString = baseURL + splitURLs[4];

        return getTitleOnMainPage( mainPageUrlString, getAllPageString( mainPageUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = allPageString.indexOf( "addhistory(" );
        beginIndex = allPageString.indexOf( ",", beginIndex );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "\"", beginIndex );

        String titleString = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( titleString ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "cview(" ) - 20;
        String tempString = allPageString.substring( beginIndex, allPageString.length() - 1 );
        String[] tempStrings = tempString.split( "\\s*>\\s*|\\s*<\\s*" );

        totalVolume = allPageString.split( "onclick=\"cview" ).length - 1;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        int nowVolume = 0;
        for ( int i = 0 ; i < tempStrings.length ; i++ ) {
            String volumeTitle = "";

            if ( tempStrings[i].matches( "(?s).*onclick=(?s).*" ) ) {

                // ex. cview('2245-49.html'
                int b = tempStrings[i].indexOf( "cview(" ) + 7;
                int e = tempStrings[i].indexOf( "'", b );

                // ex. 2245-49.html
                String idAndVolume = tempStrings[i].substring( b, e );

                // 取得單集位址
                String idString = idAndVolume.split( "-|\\." )[0];
                String volumeNoString = idAndVolume.split( "-|\\." )[1];
                urlList.add( getSinglePageURL( idString, volumeNoString ) );
                // 取得單集名稱
                volumeTitle = getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                        Common.getTraditionalChinese( tempStrings[i + 1].trim() ) ) );
                volumeList.add( getVolumeWithFormatNumber( volumeTitle ) );

                if ( ++nowVolume >= totalVolume ) {
                    break;
                }
            }
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    // 取得單集頁面的網址
    public String getSinglePageURL( String idString, String volumeNoString ) {

        String baseMainURL = "http://www.8comic.com/love/drawing-";
        String volumeString = "?ch=" + volumeNoString;

        return baseMainURL + idString + ".html" + volumeString;
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
        System.out.println( " _____________________________" );
        System.out.println( "|                          " );
        System.out.println( "| Run the 8comic module: " );
        System.out.println( "|______________________________\n" );
    }
}

class ParseECphoto extends ParseEC {

    public ParseECphoto() {
        siteID = Site.EIGHT_COMIC_PHOTO;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_photo_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_8comic_photo_encode_parse_", "html" );

        jsName = "index_8comic_photo.js";
    }

    @Override
    public void setParameters() { // let all the non-set attributes get values
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        String allPageString = getAllPageString( webSite );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            int beginIndex = allPageString.indexOf( "<title>" ) + 7;
            int endIndex = allPageString.indexOf( "</title>", beginIndex );
            String titleString = allPageString.substring( beginIndex, endIndex );

            setWholeTitle( getVolumeWithFormatNumber( 
                    Common.getStringRemovedIllegalChar( titleString ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得前面的下載伺服器網址
        String allPageString = getAllPageString( webSite );

        Common.debugPrint( "開始解析這一集有幾頁 :" );
        totalPage = allPageString.split( "\\.jpe'" ).length - 1;
        comicURL = new String[totalPage];
        Common.debugPrint( "共" + totalPage + "頁" );

        String[] tokens = allPageString.split( "'|\\." );

        int page = 0;
        for ( int i = 0 ; i < tokens.length ; i++ ) {
            if ( tokens[i].equals( "jpe" ) ) {
                comicURL[page++] = "http://www.8comic.com" + tokens[i - 1] + ".jpg";
                //Common.debugPrintln( (page-1) + " " + comicURL[page-1] );
            }
        }

        //System.exit(0);
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*\\d+-\\d+.html(?s).*" ) ) // ex. http://www.8comic.com/photo/1-1.html
        {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        return "8comic圖集";
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        return "8comic圖集";
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "id=\"newphoto_dl" );
        int endIndex = allPageString.indexOf( "id=\"newphoto_pager", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex );
        String[] tempStrings = tempString.split( "\\d*>\\d*|\\d*<\\d*|\"" );

        totalVolume = tempString.split( "href=" ).length - 1;
        Common.debugPrintln( "共有" + totalVolume + "個圖集" );

        int nowVolume = 0;
        for ( int i = 0 ; i < tempStrings.length ; i++ ) {
            if ( tempStrings[i].matches( "(?s).*href=(?s).*" ) ) {
                urlList.add( "http://www.8comic.com" + tempStrings[i + 1] );
            } else if ( tempStrings[i].matches( "(?s).*br.*" ) ) {
                volumeList.add( tempStrings[i + 1].trim() );
            }
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void printLogo() {
        System.out.println( " ____________________________________" );
        System.out.println( "|                                 " );
        System.out.println( "| Run the 8comic photo module: " );
        System.out.println( "|_____________________________________\n" );
    }
}

class ParseSixComic extends ParseEC {

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        // http://www.8comic.com/love/drawing-7853.html?ch=1 轉為
        // http://www.6comic.com/comic/manga-7853.html

        String[] splitURLs = urlString.split( "-|\\?" );

        String baseURL = "http://www.6comic.com/comic/manga-";
        String mainPageUrlString = baseURL + splitURLs[1];

        return getTitleOnMainPage( mainPageUrlString, getAllPageString( mainPageUrlString ) );
    }

    

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "comicview.js\"" );
        int endIndex = allPageString.indexOf( "id=\"tb_anime\"", beginIndex );
        String tempString = allPageString.substring( beginIndex, endIndex );

        totalVolume = tempString.split( "onmouseover=" ).length - 1;
        Common.debugPrintln( "共有" + totalVolume + "集" );


        String idString = ""; // ex. 7853
        String volumeNoString = ""; // ex. 3
        String volumeTitle = "";

        beginIndex = endIndex = 0;
        for ( int i = 0 ; i < totalVolume ; i++ ) {
            // 取得單集位址
            beginIndex = tempString.indexOf( "onmouseover=", beginIndex );
            beginIndex = tempString.indexOf( "'", beginIndex ) + 1;
            endIndex = tempString.indexOf( "'", beginIndex );
            idString = tempString.substring( beginIndex, endIndex );

            beginIndex = tempString.indexOf( "'", endIndex + 1 ) + 1;
            endIndex = tempString.indexOf( "'", beginIndex );
            volumeNoString = tempString.substring( beginIndex, endIndex );

            urlList.add( getSinglePageURL( idString, volumeNoString ) );


            // 取得單集名稱
            beginIndex = tempString.indexOf( ">", endIndex ) + 1;
            endIndex = tempString.indexOf( "<", beginIndex );

            volumeTitle = getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( tempString.substring( beginIndex, endIndex ).trim() ) ) );
            volumeList.add( getVolumeWithFormatNumber( volumeTitle ) );

        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    // 取得單集頁面的網址
    public String getSinglePageURL( String idString, String volumeNoString ) {

        String baseMainURL = "http://www.8comic.com/love/drawing-";
        String volumeString = "?ch=" + volumeNoString;

        return baseMainURL + idString + ".html" + volumeString;
    }

    @Override
    public void printLogo() {
        System.out.println( " ____________________________________" );
        System.out.println( "|                                 " );
        System.out.println( "| Run the 6comic module: " );
        System.out.println( "|_____________________________________\n" );
    }
}