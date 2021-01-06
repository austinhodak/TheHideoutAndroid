package com.miguelcatalan.materialsearchview.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * @author Miguel Catalan Bañuls
 */
public class AnimationUtil {

    public static int ANIMATION_DURATION_SHORT = 150;
    public static int ANIMATION_DURATION_MEDIUM = 400;
    public static int ANIMATION_DURATION_LONG = 800;

    public interface AnimationListener {
        /**
         * @return true to override parent. Else execute Parent method
         */
        boolean onAnimationStart(View view);

        boolean onAnimationEnd(View view);

        boolean onAnimationCancel(View view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void reveal(final View view, final AnimationListener listener, int duration) {
        int cx = view.getWidth() - (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, view.getResources().getDisplayMetrics());
        int cy = view.getHeight() / 2;
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        }
        view.setVisibility(View.VISIBLE);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                listener.onAnimationStart(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationEnd(view);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                listener.onAnimationCancel(view);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(duration);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void hide(final View view, final AnimationListener listener) {
        int cx = view.getWidth() - (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, view.getResources().getDisplayMetrics());
        int cy = view.getHeight() / 2;
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, finalRadius, 0);
        view.setVisibility(View.VISIBLE);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                listener.onAnimationStart(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationEnd(view);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                listener.onAnimationCancel(view);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    public static void fadeOutView(View view) {
        fadeOutView(view, ANIMATION_DURATION_SHORT);
    }

    public static void fadeOutView(View view, int duration) {
        fadeOutView(view, duration, null);
    }

    public static void fadeOutView(View view, int duration, final AnimationListener listener) {
        ViewCompat.animate(view).alpha(0f).setDuration(duration).setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
                if (listener == null || !listener.onAnimationStart(view)) {
                    view.setDrawingCacheEnabled(true);
                }
            }

            @Override
            public void onAnimationEnd(View view) {
                if (listener == null || !listener.onAnimationEnd(view)) {
                    view.setVisibility(View.GONE);
                    view.setDrawingCacheEnabled(false);
                }
            }

            @Override
            public void onAnimationCancel(View view) {
            }
        });
    }
}