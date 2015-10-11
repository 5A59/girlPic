package network;

import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 15-9-25.
 */
public class PicSpider {

    final private static String CODE = "GBK";

    private Spider spider;

    public PicSpider(){
        spider = Spider.getInstance();
    }

    public List<Tags> getTags(){
        String [] sub = {"[class=tags]","a"};

        List<String> list = new ArrayList<>();
        for (String s : sub){
            list.add(s);
        }

        try {

            List<Element> elementList = spider.spider(UrlUtil.mainUrl,list,CODE);

            List<Tags> tagsList = new ArrayList<>();
            for (Element e : elementList){
                Tags t = new Tags();
                t.setName(e.text());
                t.setUrl(e.attr("href"));
                tagsList.add(t);
            }

            return tagsList;

        } catch (Exception e) {
            return null;
        }
    }

    public List<Pic> getPics(String url){
        String [] sub = {"[class=pic]"};

        List<String> list = new ArrayList<>();
        for (String s : sub){
            list.add(s);
        }

        try{
            List<Element> elementList = spider.spider(url,list,CODE);

            List<Pic> picList = new ArrayList<>();

            for (Element e : elementList){
                Pic pic = new Pic();
                Elements aE = e.select("a");
                Elements imgE = e.select("img");

                pic.setName(imgE.attr("alt"));
                pic.setPicUrl(imgE.attr("src"));
                pic.setDetailUrl(aE.attr("href"));

                picList.add(pic);
            }

            return picList;

        }catch (Exception e){

        }

        return null;
    }

    public String getNext(String url){
        String [] sub = {"[id=wp_page_numbers]","li"};
        List<String> list = new ArrayList<>();

        for (String s : sub){
            list.add(s);
        }

        try{
            List<Element> elementList = spider.spider(url, list, CODE);

            Log.d("pic","element size :  " + elementList.size());
            Element e = elementList.get(elementList.size() - 2);
            Elements elements = e.select("a");

            String nextName = elements.text();
            Log.d("pic","   nextName   " + nextName);
            if (nextName.equals("下一页")){
                String nextUrl = elements.attr("href");
                return UrlUtil.mainAUrl + nextUrl;
            }

        }catch (Exception e){
            Log.d("pic","exception in getNext");

        }

        return null;
    }

    public List<Pic> getDetailPic(String url){
        String [] sub = {"[id=picture]","img"};
        List<String> list = new ArrayList<>();

        for (String s : sub){
            list.add(s);
        }

        try{
            List<Element> elementList = spider.spider(url,list,CODE);
            List<Pic> picUrl = new ArrayList<>();

            for (Element e : elementList){
                String u = e.attr("src");
                Pic p = new Pic();
                p.setPicUrl(u);

                picUrl.add(p);
            }

            return picUrl;
        }catch (Exception e){

        }

        return null;
    }

}
