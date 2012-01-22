/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2011/12/25
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
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
     *
     * @author user
     */
    public Parse178() {
        siteID = Site.MANHUA_178;
        indexName = Common.getStoredFileName(SetUp.getTempDirectory(), "index_178_parse_", "html");
        indexEncodeName = Common.getStoredFileName(SetUp.getTempDirectory(), "index_178_encode_parse_", "html");

        jsName = "index_178.js";
        radixNumber = 1593771; // default value, not always be useful!!

        baseURL = "http://manhua.178.com/";
        waitingTime = 2000;
        retransmissionLimit = 30;
    }

    public Parse178(String webSite, String titleName) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln("開始解析各參數 :");

        Common.debugPrintln("開始解析title和wholeTitle :");
        Common.downloadGZIPInputStreamFile(webSite, SetUp.getTempDirectory(), indexName, false, "");

        if (getWholeTitle() == null || getWholeTitle().equals("")) {
            String allPageString = Common.getFileString(SetUp.getTempDirectory(), indexName);

            int beginIndex = allPageString.indexOf("g_chapter_name");
            beginIndex = allPageString.indexOf("\"", beginIndex) + 1;
            int endIndex = allPageString.indexOf("\"", beginIndex);
            String tempTitleString = allPageString.substring(beginIndex, endIndex);

            setWholeTitle(getVolumeWithFormatNumber(Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese(tempTitleString.trim()))));
        }

        Common.debugPrintln("作品名稱(title) : " + getTitle());
        Common.debugPrintln("章節名稱(wholeTitle) : " + getWholeTitle());
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        String allPageString = Common.getFileString(SetUp.getTempDirectory(), indexName);
        Common.debugPrint("開始解析這一集有幾頁 : ");

        // 取得所有位址編碼
        int beginIndex = allPageString.indexOf("'[") + 2;
        int endIndex = allPageString.indexOf("';", beginIndex);

        String allCodeString = allPageString.substring(beginIndex, endIndex);

        totalPage = allCodeString.split(",").length;
        Common.debugPrintln("共 " + totalPage + " 頁");
        comicURL = new String[totalPage];

        String basePicURL = "http://imgfast.manhua.178.com/";
        String[] codeTokens = allCodeString.split(",");
        String firstCode = codeTokens[0].replaceAll("\"", "");

        String firstPicURL = "";
        Common.debugPrintln("第一張編碼：" + firstCode);
        firstPicURL = basePicURL + Common.getFixedChineseURL(getDecodeURL(firstCode));

        Common.debugPrintln("第一張圖片網址：" + firstPicURL);

        String[] picNames = new String[totalPage];
        for (int i = 0; i < picNames.length; i++) {
            beginIndex = codeTokens[i].lastIndexOf("/") + 1;
            endIndex = codeTokens[i].lastIndexOf("\"");
            picNames[i] = Common.getFixedChineseURL(
                    getDecodeURL(codeTokens[i].substring(beginIndex, endIndex)));
            
            //System.exit( 0 ); // debug
        }

        endIndex = firstPicURL.lastIndexOf("/") + 1;
        String parentPicURL = firstPicURL.substring(0, endIndex);

        for (int i = 0; i < codeTokens.length; i++) {
            comicURL[i] = parentPicURL + picNames[i]; // 存入每一頁的網頁網址
            //Common.debugPrintln( ( i + 1 ) + " " + comicURL[i]  ); // debug

        }

        //System.exit( 0 ); // debug
    }

    private String getDecodeURL(String code) {

        StringBuilder decodeBuilder = new StringBuilder();
        int charCode = 0;
        String[] urltokens = code.split("\\\\");
        String urltoken = "";
        String utf8 = "";
        String gb = "";
        
        // 不含中文編碼字串
        if ( !code.matches( ".*\\\\u.*" ) ) {
            //Common.debugPrintln( "不用比對：" + code );
            return code;
        }

        // 因為若是\開頭的字串以\作分割，第一個位置都是空字串
        // 若遇到這種情形，則從第二個位置開始
        int j = urltokens[0].matches("") ? 1 : 0;

        boolean totalDecode = true; // 是否全部都解碼了
        //Common.debugPrintln( "先從之前找過的開始比對：" );
        for (; j < urltokens.length; j++) {
            //Common.debugPrintln( "原始編碼" + urltokens[j] );
            boolean found = false;
            urltoken = "\\" + urltokens[j];
            for (int i = 0; i < codeList.size(); i++) {
                if (urltoken.matches( ".*\\" + codeList.get(i) + ".*")) {
                    //Common.debugPrintln(urltoken + " -> GB: " + decodeList.get(i));
                    found = true;
                    
                    decodeBuilder.append(decodeList.get(i)
                            + urltoken.replaceAll("\\" + codeList.get(i), ""));
                    break;

                }
            }
            if (urltoken.matches(".*\\\\u.*") && !found) {
                Common.debugPrintln("找到這邊為止");
                totalDecode = false;
                break;
            } else {
                if (!found) {
                    decodeBuilder.append(urltokens[j]);
                }
            }
        }


        // 因為沒有全部解碼，所以還是要檢查對照文件檔
        if (!totalDecode) {
            Common.debugPrintln("再以編碼文件繼續比對：");
            String[] decodeLines = getUTF8toGBDataStrings();

            for (; j < urltokens.length; j++) {
                boolean found = false;
                urltoken = "\\" + urltokens[j];
                for (int i = 0; i < decodeLines.length; i++) {

                    utf8 = decodeLines[i].split("	--->	")[0];
                    gb = decodeLines[i].split("	--->	")[1];
                    //System.out.print( utf8 + " " + gb );
                    if (urltoken.matches(".*\\" + utf8 + ".*")) {
                        Common.debugPrintln(urltoken + " -> GB: " + gb);
                        found = true;
                        decodeBuilder.append(gb + urltoken.replaceAll("\\" + utf8, ""));

                        // 存入已經解碼的字
                        codeList.add(utf8);
                        decodeList.add(gb);
                        break;

                    }
                }
                if (urltoken.matches(".*\\\\u.*") && !found ) {
                    Common.debugPrintln("比對失敗：" + urltoken);
                }

                if (!found) {
                    decodeBuilder.append(urltokens[j]);
                }
            }
        }
        Common.debugPrintln("最終網址：" + decodeBuilder.toString());


        return decodeBuilder.toString();
    }

    private String[] getUTF8toGBDataStrings() {
        File decodeFile = new File(Common.getNowAbsolutePath() + "UTF8toGB.txt");
        //Common.debugPrintln( "檔案大小：" + decodeFile.length() );
        
        if (!decodeFile.exists() || decodeFile.length() < 334465) {
            Common.debugPrintln("同資料夾內沒有發現UTF8toGB.txt或尺寸不符，重新下載此檔");
            String downloadURL = "https://sites.google.com/site/jcomicdownloader/release/UTF8toGB.txt?attredirects=0&d=1";
            Common.downloadFile(downloadURL, Common.getNowAbsolutePath(), "UTF8toGB.txt", false, null);
        }

        String[] decodeLines = Common.getFileStrings(Common.getNowAbsolutePath(), "UTF8toGB.txt");

        return decodeLines;
    }

    public void showParameters() { // for debug
        Common.debugPrintln("----------");
        Common.debugPrintln("totalPage = " + totalPage);
        Common.debugPrintln("webSite = " + webSite);
        Common.debugPrintln("----------");
    }

    @Override
    public String getAllPageString(String urlString) {
        String indexName = Common.getStoredFileName(SetUp.getTempDirectory(), "index_178_", "html");
        Common.downloadGZIPInputStreamFile(urlString, SetUp.getTempDirectory(), indexName, false, "");

        return Common.getFileString(SetUp.getTempDirectory(), indexName);
    }

    @Override
    public boolean isSingleVolumePage(String urlString) {
        // ex. http://www.kkkmh.com/manhua/0804/9119/65867.html
        if (urlString.matches("(?s).*www.178.com/(?s).*")) {
            return true;
        } else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl(String volumeURL) {
        // ex. http://www.178.com/mh/kongjuzhiyuan/16381-2.shtml轉為
        //    http://manhua.178.com/kongjuzhiyuan/

        String allPageString = getAllPageString(volumeURL);

        int beginIndex = allPageString.indexOf("g_comic_url");
        beginIndex = allPageString.indexOf("\"", beginIndex) + 1;
        int endIndex = allPageString.indexOf("\"", beginIndex);
        String mainPageURL = baseURL + allPageString.substring(beginIndex, endIndex);

        Common.debugPrintln("MAIN_URL: " + mainPageURL);

        return mainPageURL;
    }

    @Override
    public String getTitleOnSingleVolumePage(String urlString) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl(urlString);

        return getTitleOnMainPage(mainUrlString, getAllPageString(mainUrlString));
    }

    @Override
    public String getTitleOnMainPage(String urlString, String allPageString) {
        int beginIndex = allPageString.indexOf("<h1>") + 4;
        int endIndex = allPageString.indexOf("</h1>", beginIndex);
        Common.debugPrintln("B: " + beginIndex + "  E: " + endIndex);
        String title = allPageString.substring(beginIndex, endIndex).trim();

        return Common.getStringRemovedIllegalChar(Common.getTraditionalChinese(title));
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage(String urlString, String allPageString) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf("class=\"cartoon_online_border\"");
        int endIndex = allPageString.indexOf("document.write", beginIndex);

        String tempString = allPageString.substring(beginIndex, endIndex);

        int volumeCount = tempString.split("href=\"").length - 1;

        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for (int i = 0; i < volumeCount; i++) {
            // 取得單集位址
            beginIndex = tempString.indexOf("href=\"", beginIndex) + 6;
            endIndex = tempString.indexOf("\"", beginIndex);
            urlList.add(tempString.substring(beginIndex, endIndex));

            // 取得單集名稱
            beginIndex = tempString.indexOf(">", beginIndex) + 1;
            endIndex = tempString.indexOf("<", beginIndex);
            volumeTitle = tempString.substring(beginIndex, endIndex);

            volumeList.add(getVolumeWithFormatNumber(Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese(volumeTitle.trim()))));

        }

        totalVolume = volumeCount;
        Common.debugPrintln("共有" + totalVolume + "集");

        combinationList.add(volumeList);
        combinationList.add(urlList);

        return combinationList;
    }

    @Override
    public void outputVolumeAndUrlList(List<String> volumeList, List<String> urlList) {
        Common.outputFile(volumeList, SetUp.getTempDirectory(), Common.tempVolumeFileName);
        Common.outputFile(urlList, SetUp.getTempDirectory(), Common.tempUrlFileName);
    }

    @Override
    public String[] getTempFileNames() {
        return new String[]{indexName, indexEncodeName, jsName};
    }

    @Override
    public void printLogo() {
        Common.debugPrintln(" ______________________________");
        Common.debugPrintln("|                            ");
        Common.debugPrintln("| Run the 178 module:     ");
        Common.debugPrintln("|_______________________________\n");
    }
}
