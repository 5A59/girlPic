package network;

/**
 * Created by zy on 15-10-1.
 */
public class Download {

    private Download download;

    private Download(){

    }

    public Download getInstance(){
        if (download == null){
            download = new Download();
        }

        return download;
    }

    public void startLoad(){

    }

    public void onStart(){

    }

    public void onLoad(){

    }

    public void onEnd(){

    }
}
