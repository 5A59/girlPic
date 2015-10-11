package zy.com.girlpic;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.srx.widget.PullCallback;
import com.srx.widget.PullToLoadView;
import com.wangjie.androidbucket.utils.ABTextUtil;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.List;

import network.Pic;
import network.PicSpider;
import network.Tags;

public class MainActivity1 extends AppCompatActivity
        implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private RecyclerView recyclerView;
    private PullToLoadView pullToLoadView;
    private MyAdapter adapter;

    //floating action button
    private RapidFloatingActionButton rapidFloatingActionButton;
    private RapidFloatingActionLayout rapidFloatingActionLayout;
    private RapidFloatingActionHelper rapidFloatingActionHelper;
    private RapidFloatingActionContentLabelList rapidFloatingActionContentLabelList;
    private List<RFACLabelItem> items;
    private List<List<RFACLabelItem>> itemsList;
    ///////////////////////////

    private PicSpider picSpider;

    private List<Pic> pics;
    private List<Tags> tagsList;

    private String next;

    private int itemNum = 5;
    private int curItem = 0;

    private boolean loading;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            loading = false;
            pullToLoadView.setComplete();
            switch (msg.what){
                case 0://获取tag
                    pics.clear();

                    tagsList.clear();
                    tagsList = (List<Tags>) msg.obj;

                    items = new ArrayList<>();
                    int num = (int) Math.ceil(tagsList.size() / itemNum);
                    Log.d("pic","tags size  :  " + num);

                    itemsList.clear();
                    for (int i = 0 ; i < tagsList.size() ; i ++){
                        if (i % itemNum == 0 && i != 0){
                            Log.d("pic", "itemnum == 0  ");
                            items.add(new RFACLabelItem<Integer>()
                                    .setLabel("下一页")
                                    .setResId(R.mipmap.loading)
                                    .setIconNormalColor(0xffd84315)
                                    .setIconPressedColor(0xffbf360c)
                                    .setWrapper(i % itemNum));
                            itemsList.add(items);
                            items = new ArrayList<>();
                            continue;
                        }

                        items.add(new RFACLabelItem<Integer>()
                                .setLabel(tagsList.get(i).getName())
                                .setResId(R.mipmap.loading)
                                .setIconNormalColor(0xffd84315)
                                .setIconPressedColor(0xffbf360c)
                                .setWrapper(i % itemNum));

                    }

                    items.add(new RFACLabelItem<Integer>()
                            .setLabel("下一页")
                            .setResId(R.mipmap.loading)
                            .setIconNormalColor(0xffd84315)
                            .setIconPressedColor(0xffbf360c)
                            .setWrapper(tagsList.size() % num));
                    itemsList.add(items);
                    items = new ArrayList<>();

                    curItem = 0;
                    Log.d("pic","itemlist size at 0   " + itemsList.get(0).size());
                    initFLAB(itemsList.get(curItem));

                    next = tagsList.get(0).getUrl();
                    getPic(next);
                    break;
                case 1://获取pic
                    List<Pic> picList = (List<Pic>) msg.obj;
                    pics.addAll(picList);
                    adapter.notifyDataSetChanged();
                    break;
                case 2://获取next
                    String n = (String) msg.obj;
                    next = n;
                    Log.d("pic","next is  :  " + next);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        initToolBar();
        initData();
        initView();
//        getTags();
    }

    private void initData(){
        picSpider = new PicSpider();
        pics = new ArrayList<>();
        tagsList = new ArrayList<>();
        itemsList = new ArrayList<>();
    }

    private void initToolBar(){
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle("pic");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOrOpenDrawer();
            }
        });
    }

    private void initView(){
        drawerLayout = (DrawerLayout) this.findViewById(R.id.drawer);
        navigationView = (NavigationView) this.findViewById(R.id.navigation);

        toggle = new ActionBarDrawerToggle(this,drawerLayout,0,0);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(toggle);


        pullToLoadView = (PullToLoadView) this.findViewById(R.id.recycler);
        pullToLoadView.isLoadMoreEnabled(true);
        pullToLoadView.setPullCallback(new PullCallback() {
            @Override
            public void onLoadMore() {
                Log.d("pic", "loading");
                getPic(next);
            }

            @Override
            public void onRefresh() {
                getTags();
            }

            @Override
            public boolean isLoading() {
                return loading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                if (next == null) {
                    return true;
                }
                return false;
            }
        });
        pullToLoadView.initLoad();

        recyclerView = pullToLoadView.getRecyclerView();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new MyItemDecoration(10));
        adapter = new MyAdapter(this,pics);
        recyclerView.setAdapter(adapter);

        //floating action button
        rapidFloatingActionButton = (RapidFloatingActionButton) this.findViewById(R.id.activity_main_rfab);
        rapidFloatingActionLayout = (RapidFloatingActionLayout) this.findViewById(R.id.activity_main_rfal);

        items = new ArrayList<>();

        items.add(new RFACLabelItem<Integer>()
                        .setLabel("loading...")
                        .setResId(R.mipmap.loading)
                        .setIconNormalColor(0xff6a1b9a)
                        .setIconPressedColor(0xff4a148c)
                        .setWrapper(0)
        );

        initFLAB(items);
   }

    private void initFLAB(List<RFACLabelItem> items){

        rapidFloatingActionContentLabelList =
                new RapidFloatingActionContentLabelList(this);

        rapidFloatingActionContentLabelList.setOnRapidFloatingActionContentLabelListListener(this);
        rapidFloatingActionContentLabelList.setItems(items)
                .setIconShadowRadius(ABTextUtil.dip2px(this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(ABTextUtil.dip2px(this, 5));
        rapidFloatingActionHelper = new RapidFloatingActionHelper(this,rapidFloatingActionLayout,
                rapidFloatingActionButton,rapidFloatingActionContentLabelList).build();


    }

    private void closeOrOpenDrawer(){
        if (drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawer(navigationView);
            return ;
        }

        drawerLayout.openDrawer(navigationView);
    }

    private void getTags(){
        loading = true;
        new Thread(){
            @Override
            public void run() {
                List<Tags> tags = picSpider.getTags();
                handler.sendMessage(handler.obtainMessage(0,tags));
            }
        }.start();
    }

    private void getPic(final String url){
        loading = true;
        new Thread(){
            @Override
            public void run() {
                List<Pic> pics = picSpider.getPics(url);
                handler.sendMessage(handler.obtainMessage(1,pics));
                String next = picSpider.getNext(url);
                Log.d("pic","next in thread get pic  :  " + next);
                handler.sendMessage(handler.obtainMessage(2,next));

            }
        }.start();
    }

    private void getNext(final String url){
        loading = true;
        new Thread(){
            @Override
            public void run() {
                String next = picSpider.getNext(url);
                handler.sendMessage(handler.obtainMessage(2,next));
            }
        }.start();
    }

    @Override
    public void onRFACItemLabelClick(int i, RFACLabelItem rfacLabelItem) {
        rapidFloatingActionHelper.toggleContent();

        if (i >= itemsList.get(curItem).size() - 1){
            curItem ++;
            curItem %= itemsList.size();
            initFLAB(itemsList.get(curItem));
            rapidFloatingActionHelper.toggleContent();
        }else {
            int tmp = 0;
            for (int j = 0 ; j <= curItem ; j ++){
                tmp += itemsList.get(j).size();
                tmp -= 1;
            }
            getPic(tagsList.get(tmp).getUrl());
            Log.d("pic","url " + tagsList.get(tmp).getUrl() + "tmp  " + tmp);
        }
    }

    @Override
    public void onRFACItemIconClick(int i, RFACLabelItem rfacLabelItem) {

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private List<Pic> pics;
        private Activity activity;

        public MyAdapter(Activity activity,List<Pic> pics){
            this.pics = pics;
            this.activity = activity;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            View view;
            TextView textView;
            ImageView imageView;

            public MyViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                textView = (TextView) view.findViewById(R.id.text_item);
                imageView = (ImageView) view.findViewById(R.id.img_item);
            }
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item, null);

            MyViewHolder viewHolder = new MyViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.textView.setText(pics.get(position).getName());
            Glide.with(activity)
                    .load(pics.get(position).getPicUrl())
                    .error(R.mipmap.loading)
                    .placeholder(R.mipmap.loading)
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return pics.size();
        }
    }

    public class MyItemDecoration extends RecyclerView.ItemDecoration{

        int dec = 0;

        public MyItemDecoration(int dec){
            this.dec = dec;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(dec / 2, 0, dec / 2, dec);
        }
    }

}
