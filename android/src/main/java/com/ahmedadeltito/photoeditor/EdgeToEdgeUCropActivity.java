package com.ahmedadeltito.photoeditor;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yalantis.ucrop.UCropActivity;

/**
 * UCropActivity that keeps its toolbar and controls clear of the system bars.
 *
 * Android 15+ draws apps targeting SDK 35+ edge-to-edge, and UCrop 2.2.2 doesn't
 * handle window insets, so its cancel / done buttons ended up under the status bar.
 * The toolbar is pushed down by a top margin (padding it instead left the cancel /
 * done buttons misplaced and clipped by the crop area), and a backdrop view in the
 * toolbar's colour fills the area behind the status bar. The rest of the screen is
 * padded by the navigation bar and display cutout.
 */
public class EdgeToEdgeUCropActivity extends UCropActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewGroup root = findViewById(com.yalantis.ucrop.R.id.ucrop_photobox);
        final View toolbar = findViewById(com.yalantis.ucrop.R.id.toolbar);
        if (root == null || toolbar == null) {
            return;
        }

        final View statusBarBackdrop = new View(this);
        Drawable toolbarBackground = toolbar.getBackground();
        if (toolbarBackground != null && toolbarBackground.getConstantState() != null) {
            statusBarBackdrop.setBackground(toolbarBackground.getConstantState().newDrawable().mutate());
        }
        root.addView(statusBarBackdrop, 0,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(insets.left, 0, insets.right, insets.bottom);

            ViewGroup.LayoutParams backdropParams = statusBarBackdrop.getLayoutParams();
            backdropParams.height = insets.top;
            statusBarBackdrop.setLayoutParams(backdropParams);

            ViewGroup.MarginLayoutParams toolbarParams =
                    (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            toolbarParams.topMargin = insets.top;
            toolbar.setLayoutParams(toolbarParams);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);

        // The title is centred in the toolbar but the cancel / done buttons are
        // top-aligned, so centre them too to keep all three on one line. The menu
        // view is created after onCreate, hence the global layout listener.
        if (toolbar instanceof ViewGroup) {
            final ViewGroup toolbarGroup = (ViewGroup) toolbar;
            toolbar.getViewTreeObserver().addOnGlobalLayoutListener(
                    () -> centerToolbarChildrenVertically(toolbarGroup));
        }
    }

    private static void centerToolbarChildrenVertically(ViewGroup toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            ViewGroup.LayoutParams params = child.getLayoutParams();
            if (!(params instanceof ActionBar.LayoutParams)) {
                continue;
            }
            ActionBar.LayoutParams actionBarParams = (ActionBar.LayoutParams) params;
            int gravity = (actionBarParams.gravity & ~Gravity.VERTICAL_GRAVITY_MASK)
                    | Gravity.CENTER_VERTICAL;
            if (actionBarParams.gravity != gravity) {
                actionBarParams.gravity = gravity;
                child.setLayoutParams(actionBarParams);
            }
        }
    }
}
