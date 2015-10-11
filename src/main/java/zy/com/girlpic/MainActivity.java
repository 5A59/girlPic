package zy.com.girlpic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kogitune.activity_transition.ActivityTransitionLauncher;
import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;
import com.srx.widget.PullCallback;
import com.srx.widget.PullToLoadView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.Pic;
import network.PicSpider;
import network.Tags;
import view.AnimView;
import view.MyPopUpWindow;

public class MainActivity extends AppCompatActivity implements AnimView{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private RecyclerView recyclerView;
    private PullToLoadView pullToLoadView;
    private MyAdapter adapter;

    //floating action button
    private FloatingActionButton floatingActionButton;

    private MyPopUpWindow window;
    private View windowView;
    private ListView windowListView;
    private SimpleAdapter windowAdapter;
    private List<Map<String,String>> windowList;

    private int screenHeight;
    private int screenWidth;
    private int windowWidth;
    private int windowHeight;

    private Animation animation;
    private Animation closeAnimation;
    ///////////////////////

    private PicSpider picSpider;

    private List<Pic> pics;
    private List<Tags> tagsList;

    private String next;

    private boolean loading;
    private boolean recyleRefresh;
    private boolean clearBeforePic;
    private boolean close;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (clearBeforePic){
                pics.clear();
                clearBeforePic = false;
                recyclerView.smoothScrollToPosition(0);
            }

            loading = false;
            pullToLoadView.setComplete();
            switch (msg.what){
                case 0://获取tag
                    pics.clear();

                    tagsList.clear();
                    tagsList = (List<Tags>) msg.obj;
                    windowList.clear();

                    for (Tags t : tagsList){
                        Map<String,String> map = new HashMap<>();
                        map.put("tag",t.getName());
                        map.put("url",t.getUrl());
                        windowList.add(map);
                    }
                    windowAdapter.notifyDataSetChanged();

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

        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        windowWidth = (int) (screenWidth / 2.5);
        windowHeight = (int) (screenHeight / 2.5);

        recyleRefresh = true;
        clearBeforePic = false;

        close = true;

    }

    private void initToolBar(){
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle("pic");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
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
                if (recyleRefresh){
                    getTags();
                    return ;
                }
                getPic(next);
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
        recyleRefresh = true;
        pullToLoadView.initLoad();

        recyclerView = pullToLoadView.getRecyclerView();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new MyItemDecoration(10));
        adapter = new MyAdapter(this,pics);
        recyclerView.setAdapter(adapter);

        //floating action button
        floatingActionButton = (FloatingActionButton) this.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrDismissWindow();
            }
        });


        recyclerView.setOnTouchListener(new ShowHideOnScroll(floatingActionButton));

        initPopUpWindow();
   }

    private void initPopUpWindow(){
        windowView = LayoutInflater.from(this).inflate(R.layout.popupwindow,null);
        windowListView = (ListView) windowView.findViewById(R.id.listview);
        windowList = new ArrayList<>();
        Map<String,String> map = new HashMap<>();
        map.put("tag","loading...");
        windowList.add(map);

        windowAdapter = new SimpleAdapter(this,windowList,R.layout.window_item,
                new String[]{"tag"},new int[]{R.id.textview});
        windowListView.setAdapter(windowAdapter);
        windowListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("pic", "click item in popwindow");
                if (windowList.size() <= 1) {
                    return;
                }

                getTagPic(((Map<String, String>) parent.getAdapter().getItem(position)).get("url"));
                showOrDismissWindow();
            }
        });

        //window settings
        if (window == null){
            window = new MyPopUpWindow(windowView);
            window.setAnimView(this);
            window.setAnimationStyle(R.style.popwindow);
            window.setBackgroundDrawable(new BitmapDrawable());
            window.setFocusable(true);
            window.setTouchable(true);
        }
    }


    private void showOrDismissWindow(){
        if (window.isShowing()){
            window.dismiss();
            return;
        }

        int [] location = new int[2];
        floatingActionButton.getLocationOnScreen(location);

        window.setWidth(windowWidth);
        window.setHeight(windowHeight);
        window.showAtLocation(floatingActionButton, Gravity.NO_GRAVITY,
                location[0], location[1] - windowHeight - 50);
//        window.showAsDropDown(floatingActionButton, 0, -windowHeight - floatingActionButton.getMeasuredHeight());
        window.update();
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

    private void getTagPic(String url){
        clearBeforePic = true;
        recyleRefresh = false;
        next = url;
        pullToLoadView.initLoad();
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
    public void startAnim() {
        setWindow(0.4f);
        anim();
    }

    @Override
    public void endAnim() {
        setWindow(1f);
        anim();
    }

    private void setWindow(float al){
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = al;
        getWindow().setAttributes(layoutParams);
    }

    public void anim(){
        if (animation == null){
            animation = new RotateAnimation(0,45,
                    floatingActionButton.getMeasuredWidth() / 2,floatingActionButton.getMeasuredHeight() / 2);
            animation.setDuration(500);
            animation.setFillAfter(true);
            animation.setFillEnabled(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    if (close){
//                        floatingActionButton.setRotation(0);
//                        close = false;
//                    }else{
//                        floatingActionButton.setRotation(45);
//                        close = true;
//                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        if (closeAnimation == null){
            closeAnimation = new RotateAnimation(45,0,
                    floatingActionButton.getMeasuredWidth() / 2,floatingActionButton.getMeasuredHeight() / 2);
            closeAnimation.setDuration(500);
            closeAnimation.setFillAfter(true);
            closeAnimation.setFillEnabled(true);
        }

        if (close){
            floatingActionButton.startAnimation(animation);
            close = false;
        }else {
            floatingActionButton.startAnimation(closeAnimation);
            close = true;
        }
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
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.textView.setText(pics.get(position).getName());
            Glide.with(activity)
                    .load(pics.get(position).getPicUrl())
                    .error(R.mipmap.loading)
                    .placeholder(R.mipmap.loading)
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,DetailPicActivity.class);
                    intent.putExtra("url",pics.get(position).getDetailUrl());
                    intent.putExtra("title",pics.get(position).getName());

                    ActivityTransitionLauncher.with(MainActivity.this)
                            .from(floatingActionButton).launch(intent);
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
