package com.yooiistudios.quitaddialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by Wooseong Kim in Hello-Admob Yooii Studios Co., LTD. on 2015. 9. 13.
 *
 * AdDialogFactory
 *  종료시 애드뷰를 띄워주는 팩토리 클래스
 */
public class QuitAdDialogFactory {
    /**
     * 세로 고정 앱에서는 landscapeAdView 를 사용하지 않음
     */
    public static class Options {
        @NonNull public final Activity activity;
        @NonNull public final AdView portraitAdView;
        @Nullable public AdView landscapeAdView;
        public boolean isRotatable = false;
        public boolean isAppCompat = true;
        public boolean isAdIncluded = true;

        public Options(@NonNull Activity activity, @NonNull AdView portraitAdView) {
            this.activity = activity;
            this.portraitAdView = portraitAdView;
        }
    }

    private QuitAdDialogFactory() { throw new AssertionError("Must not create this class!"); }

    public static AdView initPortraitAdView(Context context, String adUnitId, AdRequest adRequest) {
        // make AdView again for next quit dialog
        // prevent child reference
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(adUnitId);
        adView.loadAd(adRequest);
        return adView;
    }

    public static AdView initLandscapeAdView(Context context, String adUnitId, AdRequest adRequest) {
        // make AdView again for next quit dialog
        // prevent child reference
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.LARGE_BANNER);
        adView.setAdUnitId(adUnitId);
        adView.loadAd(adRequest);
        return adView;
    }

    public static Dialog makeDialog(Options options) {
        if (options.isAppCompat) {
            return makeAppCompatDialog(options);
        } else {
            return makeBaseDialog(options);
        }
    }

    private static AlertDialog makeAppCompatDialog(Options options) {
        final Activity activity = options.activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.quit_ad_dialog_title_text);
        builder.setPositiveButton(activity.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.finish();
                    }
        });
        builder.setNegativeButton(activity.getString(R.string.no), null);

        final AlertDialog dialog = builder.create();

        if (options.isAdIncluded) {
            if (!options.isRotatable) {
                dialog.setView(options.portraitAdView);
                applyPaddingTop(dialog, options, activity); // only for AppCompat Dialog
            } else {
                View tempFrameView = createTempFrameView(activity);
                dialog.setView(tempFrameView);

                View rotatableAdView = createRotatableAdView(tempFrameView, options, dialog);
                dialog.setView(rotatableAdView);
            }
        }
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private static void applyPaddingTop(AlertDialog dialog, final Options options,
                                        final Activity activity) {
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ViewParent parentView = options.portraitAdView.getParent();
                if (parentView instanceof ViewGroup) {
                    final ViewGroup contentWrapper = ((ViewGroup) parentView);
                    int paddingTop = activity.getResources().getDimensionPixelSize(
                            R.dimen.quit_dialog_padding_top);
                    contentWrapper.setPadding(0, paddingTop, 0, 0);
                }
            }
        });
    }

    private static android.app.AlertDialog makeBaseDialog(Options options) {
        final Activity activity = options.activity;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle(R.string.quit_ad_dialog_title_text);
        builder.setPositiveButton(activity.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.finish();
                    }
        });
        builder.setNegativeButton(activity.getString(R.string.no), null);

        final android.app.AlertDialog dialog = builder.create();

        if (options.isAdIncluded) {
            if (!options.isRotatable) {
                dialog.setView(options.portraitAdView);
            } else {
                // 레이아웃 구조 동적 변경을 위해 setView 총 두 번 사용 필요
                View contentView = createTempFrameView(activity);
                dialog.setView(contentView);

                View rotatableAdView = createRotatableAdView(contentView, options, dialog);
                dialog.setView(rotatableAdView);
            }
        }
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * AdView 추가 전 임시적으로 대입하는 뷰
     */
    @NonNull
    private static View createTempFrameView(Activity activity) {
        final View contentView = new View(activity);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return contentView;
    }

    private static View createRotatableAdView(@NonNull final View tempFrameView,
                                              @NonNull final Options options,
                                              @NonNull final Dialog alertDialog) {
        final AdView portraitAdView = options.portraitAdView;
        final AdView landscapeAdView = options.landscapeAdView;
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                ViewParent parentView = tempFrameView.getParent();
                if (parentView instanceof ViewGroup) {
                    final ViewGroup contentWrapper = ((ViewGroup) parentView);

                    if (options.isAppCompat) {
                        int paddingTop = tempFrameView.getResources().getDimensionPixelSize(
                                R.dimen.quit_dialog_padding_top);
                        contentWrapper.setPadding(0, paddingTop, 0, 0);
                    }

                    contentWrapper.removeView(tempFrameView);

                    if (isDevicePortrait(tempFrameView)) {
                        contentWrapper.addView(portraitAdView);
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                contentWrapper.removeView(portraitAdView);
                            }
                        });
                    } else if (isDeviceLandscape(tempFrameView) && landscapeAdView != null) {
                        contentWrapper.addView(landscapeAdView);
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                contentWrapper.removeView(landscapeAdView);
                            }
                        });
                    }
                }
            }
        });
        return tempFrameView;
    }

    private static boolean isDevicePortrait(final View contentView) {
        int contentWidth = contentView.getWidth();
        int contentHeight = contentView.getHeight();
        float screenDensity = contentView.getResources().getDisplayMetrics().density;

        return contentWidth >= 300 * screenDensity && contentHeight >= 250 * screenDensity;
    }

    private static boolean isDeviceLandscape(final View contentView) {
        int contentWidth = contentView.getWidth();
        int contentHeight = contentView.getHeight();
        float screenDensity = contentView.getResources().getDisplayMetrics().density;

        return contentWidth >= 320 * screenDensity && contentHeight >= 100 * screenDensity;
    }
}
