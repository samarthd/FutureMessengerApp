package cs371m.hermes.futuremessenger.ui.main.animators;

import android.support.v7.widget.RecyclerView;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

/**
 * The ScaleInAnimator extends from BaseItemAnimator, and due to a bug, does not recognize when
 * setSupportsChangeAnimations(false) is called. It animates the change regardless of whether
 * the value is true or false. As a result, override its animate change method to do nothing.
 */
public class MessageAnimator extends ScaleInAnimator {
    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY,
                                 int toX, int toY) {
        return true;
    }
}
