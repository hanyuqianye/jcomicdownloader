/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/11/21
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  2.01: 1. 修正Google圖片搜尋中部份非英文關鍵字沒有正確解析為資料夾名稱的bug。
 *  1.19: 1. 修正後已支援『顯示更多結果』後面的圖。
 *       2. 修改下載機制，遇到非正常連線直接放棄，加快速度。
 *  1.18: 1. 新增新增對google圖片搜尋的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import javax.swing.JOptionPane;
import jcomicdownloader.ComicDownGUI;

public class ParseGooglePic extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
     *
     * @author user
     */
    public ParseGooglePic() {
        siteID = Site.GOOGLE_PIC;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_google_pic_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_google_pic_encode_parse_", "html" );

        jsName = "index_google_pic.js";
        radixNumber = 185223571; // default value, not always be useful!!

        baseURL = "";
    }

    public ParseGooglePic( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址
        String allPageString = "";

        // 取得基本版位址的前面
        String baseURL = getBaseUrlString( webSite );
        // google圖片搜尋只有到980，之後就搜不到了。
        allPageString = getNewAllPageString( baseURL + "&start=980&sa=N" );

        Common.debugPrint( "開始解析這一集有幾頁 : " );
        //String[] tokens = allPageString.split( ";imgurl=");

        // 最後一頁的圖片張數
        int lastPagePicCount = allPageString.split( "imgurl=" ).length - 1;

        int beginIndex = allPageString.indexOf( "style=\"float:right\"" );
        beginIndex = allPageString.indexOf( "<div>", beginIndex ) + 5;
        int endIndex = allPageString.indexOf( "</div>", beginIndex );
        String[] pageTokens = allPageString.substring( beginIndex, endIndex ).split( "\\s" );

        int lastPageNumber = 1000;
        for ( int i = 1 ; i < pageTokens.length ; i++ ) {
            if ( pageTokens[i].matches( "\\d+" ) ) {
                // 因為最後一頁不全，所以減一
                // 取較小的，防止判斷錯誤
                if ( (Integer.parseInt( pageTokens[i] ) - 1) < lastPageNumber ) {
                    lastPageNumber = Integer.parseInt( pageTokens[i] ) - 1;
                }
            }
        }
        System.out.println( lastPageNumber );
        // 每一頁有二十張圖
        totalPage = lastPageNumber * 20 + lastPagePicCount;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        boolean existSameNameFile = false;
        int choice = -1; // 當資料夾內已有同檔名檔案時應該做何處理
        int p = 0; // 頁數編號
        for ( int i = 1 ; i <= lastPageNumber ; i++ ) {
            // "&start=0&sa=N"
            String baseNo = "&start=" + ((i - 1) * 20) + "&sa=N";
            allPageString = getNewAllPageString( baseURL + baseNo );

            String[] tokens = allPageString.split( "imgurl=" );

            for ( int j = 1 ; j < tokens.length ; j++ ) {
                p++;
                comicURL[p - 1] = tokens[j].split( "&amp;" )[0];
                beginIndex = comicURL[p - 1].lastIndexOf( "/" ) + 1;
                String picName = comicURL[p - 1].substring( beginIndex, comicURL[p - 1].length() );

                if ( picName.length() > 40 ) // 檔名太長
                {
                    continue;
                }

                if ( new File( getDownloadDirectory() + picName ).exists() ) {
                    existSameNameFile = true;

                    if ( choice < 0 ) {
                        Object[] options = { "自動更改名稱", "自動略過不下載", "自動複寫檔案" };
                        choice = JOptionPane.showOptionDialog( ComicDownGUI.mainFrame, "資料夾內已有" + picName + "，請問應該怎麼處理？（選定後，往後遇到相同情形皆依此次辦理)",
                                "詢問視窗",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options, options[0] );
                    }

                    if ( choice == 0 ) {
                        String newPicName = Common.getStoredFileName( Common.tempDirectory, picName.split( "\\." )[0], picName.split( "\\." )[1] );

                        singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, newPicName, 0, true );
                    } else if ( choice == 1 ); else if ( choice == 2 ) {
                        new File( getDownloadDirectory() + picName ).delete();
                        singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, picName, 0, true );
                    } else {
                        Common.errorReport( "不可能有這種數字：" + choice );
                    }

                } else {
                    existSameNameFile = false;
                    singlePageDownload( getTitle(), getWholeTitle(), comicURL[p - 1], totalPage, p, picName, 0, true );
                }

                //System.out.println( comicURL[p-1] );
            }


        }

        //System.exit(0); // debug
    }

    // 取得基本版的網址(最後面還要加上"&start=0&sa=N"才完整)
    private String getBaseUrlString( String standardUrlString ) {
        String allPageString = getAllPageString( standardUrlString + "&sout=1" );

        int beginIndex = allPageString.indexOf( "href=\"http://" ) + 6;
        int endIndex = allPageString.indexOf( "&gbv=", beginIndex );
        String baseUrlString = allPageString.substring( beginIndex, endIndex );

        return baseUrlString;
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        if ( !new File( Common.tempDirectory + indexName ).exists() ) {
            Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        }

        return Common.getFileString( Common.tempDirectory, indexName );
    }

    // 不管有沒有存在檔案，都下載新的
    public String getNewAllPageString( String urlString ) {
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        return Common.getFileString( Common.tempDirectory, indexName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // google圖片搜尋只有一頁
        return false;
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        return volumeURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        return "Google圖片搜尋";
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        return "Google圖片搜尋";
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = 0;
        int endIndex = 0;

        String volumeTitle = "搜尋結果";

        try {
            beginIndex = urlString.indexOf( "q=" ) + 2;
            endIndex = urlString.indexOf( "&", beginIndex );

            // 由網址碼轉為utf8字串（如果只是普通英文+符號的關鍵字，則不會有影響）
            volumeTitle = URLDecoder.decode( urlString.substring( beginIndex, endIndex ), "UTF-8" );

            //volumeTitle = volumeTitle.replaceAll( "%20", " " );
        } catch ( UnsupportedEncodingException ex ) {
            Logger.getLogger( ParseGooglePic.class.getName() ).log( Level.SEVERE, null, ex );
        }
        /*
        else { // 搜尋關鍵字沒有中文（只有英文字母或符號）
        beginIndex = urlString.indexOf( "q=" ) + 2;
        endIndex = urlString.indexOf( "&", beginIndex );
        volumeTitle = urlString.substring( beginIndex, endIndex ).replaceAll( "\\+", " " );
        volumeTitle = volumeTitle.replaceAll( "%20", " " );
        }
         */

        urlList.add( urlString );
        volumeList.add( "搜尋「" + volumeTitle + "」" );

        totalVolume = 1;
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
        System.out.println( " _____________________________________" );
        System.out.println( "|                                                                    |" );
        System.out.println( "| Run the Google Picture Search module:     |" );
        System.out.println( "|______________________________________|\n" );
    }
}
