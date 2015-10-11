package view;

import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by zy on 15-9-30.
 */
public class MyPopUpWindow extends PopupWindow{

    private AnimView animView;

    public MyPopUpWindow(View view){
        super(view);
    }

    public void setAnimView(AnimView animView){
        this.animView = animView;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animView.endAnim();
    }

    @Override
    public void update() {
        super.update();
        animView.startAnim();
    }
}
