package network;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zy on 15-9-25.
 */
public class Spider {

    static Spider spider = null;

    private Spider(){

    }

    static public Spider getInstance(){
        if (spider == null){
            spider = new Spider();
        }

        return spider;
    }

    public String getHtml(String url, Map<String, String> header) throws IOException {
        Connection connection = Jsoup.connect(url);

        if (header == null){
            connection.header(HttpHeader.userAgentTitle,HttpHeader.userAgent);
        }else {
            Set<String> set = header.keySet();

            for (String s : set){
                connection.header(s,header.get(s));
            }
        }

        connection.method(Connection.Method.GET);
        Connection.Response response = connection.execute();

        return response.body();
    }

    public List<Element> spider(String url,List<String> tags,String code) throws Exception{
        if (tags == null || url == null){
            return null;
        }

        if (code == null){
            code = "utf-8";
        }
        Document document = Jsoup.parse(new URL(url).openConnection().getInputStream(), code, url);

        Elements elements = document.getAllElements();

        for (String e : tags){
            elements = elements.select(e);
        }

        return elements;
    }



    public static class HttpHeader{
        public static String userAgentTitle = "User-Agent";
        public static String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:33.0) Gecko/20100101 Firefox/33.";
    }

}
