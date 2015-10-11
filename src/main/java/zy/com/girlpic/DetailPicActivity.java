package zy.com.girlpic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kogitune.activity_transition.ActivityTransition;
import com.kogitune.activity_transition.ExitActivityTransition;
import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.shamanland.fab.FloatingActionButton;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import network.Pic;
import network.PicSpider;
import value.Value;

/**
 * Created by zy on 15-9-30.
 */
public class DetailPicActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ViewPager viewPager;
    private MyAdapter adapter;
    private List<Pic> picList;
    private List<ImageView> imgViewList;

    private FloatingActionButton floatingActionButton;

    private CircularProgressDrawable drawable;
    private Animator animator;

    private PicSpider spider;

    private ExitActivityTransition exitActivityTransition;

    private String detailUrl;
    private String title;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    picList = (List<Pic>)msg.obj;
                    if (picList != null){
                        setViewPager();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pic);

        exitActivityTransition = ActivityTransition.with(getIntent())
                .to(findViewById(R.id.fab)).start(savedInstanceState);

        init();
    }

    @Override
    public void onBackPressed() {
        exitActivityTransition.exit(this);
    }

    private void init(){
        initData();
        initToolbar();
        initView();
        getPics();
    }

    private void initToolbar(){
        toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                exitActivityTransition.exit(DetailPicActivity.this);
            }
        });
    }

    private void initData(){
        Intent intent = getIntent();
        detailUrl = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        imgViewList = new ArrayList<>();
        spider = new PicSpider();
    }

    private void initView(){
        viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new MyListener());
        adapter = new MyAdapter(imgViewList);
        viewPager.setAdapter(adapter);

        floatingActionButton = (FloatingActionButton) this.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAsyncTask asyncTask = new MyAsyncTask();
                String url = picList.get(viewPager.getCurrentItem()).getPicUrl();
                String[] name = url.split("uploads/");

                asyncTask.execute(url,name[name.length - 1].replace("/","_"));
                picList.get(viewPager.getCurrentItem()).setHasDown(true);

//                floatingActionButton.setBackgroundDrawable(drawable);
//                animator.start();
            }
        });

        drawable = new CircularProgressDrawable.Builder()
                .setRingWidth(5)
                .setOutlineColor(getResources().getColor(android.R.color.darker_gray))
                .setRingColor(getResources().getColor(android.R.color.holo_green_light))
                .setCenterColor(getResources().getColor(R.color.main))
                .create();

        animator = prepareStyle3Animation();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setViewPager(){
        for (Pic p : picList){
            ImageView imageView = new ImageView(this);
            if (this.isDestroyed()){// 最小api是17
                return;
            }
            Glide.with(this)
                    .load(p.getPicUrl())
                    .error(R.mipmap.loading)
                    .placeholder(R.mipmap.loading)
                    .into(imageView);
            viewPager.addView(imageView);
            imgViewList.add(imageView);
        }
        adapter.notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(imgViewList.size());
    }

    private void getPics(){
        new Thread(){
            @Override
            public void run() {
                List<Pic> picList = spider.getDetailPic(detailUrl);
                handler.sendMessage(handler.obtainMessage(0,picList));
            }
        }.start();
    }

    public class MyListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public class MyAdapter extends PagerAdapter{

        private List<ImageView> imageViewList;

        public MyAdapter(List<ImageView> imageViewList){
            this.imageViewList = imageViewList;
        }

        @Override
        public int getCount() {
            return imageViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            return imageViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewList.get(position));
        }
    }

    public class MyAsyncTask extends AsyncTask<String,Integer,Boolean>{

        @Override
        protected void onPreExecute() {
            //start asynctask
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                Log.d("pic","picname  " + params[1] + "   url  " + params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                String picName = params[1];

                File dir = new File(Value.picPath);
                if (!dir.exists()){
                    dir.mkdir();
                }

                File file = new File(Value.picPath + "/" + picName);

                OutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];

                int len = 0;
                while((len = bufferedInputStream.read(buffer)) != -1){
                    outputStream.write(buffer,0,len);
                    outputStream.flush();
                }
                outputStream.flush();
                outputStream.close();
                bufferedInputStream.close();
                inputStream.close();

            } catch (Exception e) {
                Log.d("pic","download exception" + e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(DetailPicActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
//            floatingActionButton.setColor(getResources().getColor(R.color.main));
        }
    }

    private Animator prepareStyle1Animation() {
        AnimatorSet animation = new AnimatorSet();

        final Animator indeterminateAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.PROGRESS_PROPERTY, 0, 3600);
        indeterminateAnimation.setDuration(3600);

        Animator innerCircleAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 0.75f);
        innerCircleAnimation.setDuration(3600);
        innerCircleAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawable.setIndeterminate(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                indeterminateAnimation.end();
                drawable.setIndeterminate(false);
                drawable.setProgress(0);
            }
        });

        animation.playTogether(innerCircleAnimation, indeterminateAnimation);
        return animation;
    }

    private Animator prepareStyle3Animation() {
        AnimatorSet animation = new AnimatorSet();

        ObjectAnimator progressAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.PROGRESS_PROPERTY, 0.75f, 0f);
        progressAnimation.setDuration(1200);
        progressAnimation.setInterpolator(new AnticipateInterpolator());

        Animator innerCircleAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0.75f, 0f);
        innerCircleAnimation.setDuration(1200);
        innerCircleAnimation.setInterpolator(new AnticipateInterpolator());

        ObjectAnimator invertedProgress = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.PROGRESS_PROPERTY, 0f, 0.75f);
        invertedProgress.setDuration(1200);
        invertedProgress.setStartDelay(1500);
        invertedProgress.setInterpolator(new OvershootInterpolator());

        Animator invertedCircle = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 0.75f);
        invertedCircle.setDuration(1200);
        invertedCircle.setStartDelay(1500);
        invertedCircle.setInterpolator(new OvershootInterpolator());

        animation.playTogether(progressAnimation, innerCircleAnimation, invertedProgress, invertedCircle);
        return animation;
    }
}

