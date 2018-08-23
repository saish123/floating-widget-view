package com.sawant.floating_widget_view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class FloatingViewService extends Service {

    private View floatingView;
    private WindowManager windowManager;
    private MediaPlayer mediaPlayer;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * Inflate View
         */
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_view, null);
        initilizeMediaPlayer();
        /**
         * Add View to window
         */
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        /**
         * Specify View Position i.e Where to place on screen
         */
        params.gravity = Gravity.TOP | Gravity.LEFT; // Initially view will be added to top-left corner.
        params.x = 0;
        params.y = 100;

        /**
         * Add view to the window
         */
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        /**
         * The root element of the collapsed view layout
         */
        final View collapsedView = floatingView.findViewById(R.id.collapse_view);
        /**
         * The root element of expanded view layout
         */
        final View expandedView = floatingView.findViewById(R.id.expanded_container);


        /**
         * Set close button
         */
        ImageView closeButtonCollapsed = (ImageView) floatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Stop the service and remove the floating view from window
                 */
                stopSelf();
            }
        });
        /**
         * Set Play button
         */
        ImageView playButton = (ImageView) floatingView.findViewById(R.id.play_btn);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getApplicationContext().st

                try {
                    if (mediaPlayer == null) {
                        initilizeMediaPlayer();
                    }
                    mediaPlayer.setLooping(true);
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        /**
         * Set close button
         */

        ImageView closeButton = (ImageView) floatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });


        ImageView openButton = (ImageView) floatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                /**
                 * stop service and remove floating View from view Hierarchy
                 */
                stopSelf();
            }
        });

        /**
         * Logic that is behind drag and move floating view on users interaction
         */
        RelativeLayout rootContainer = (RelativeLayout) floatingView.findViewById(R.id.root_container);
        rootContainer.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        /**
                         * Remember the initial position of x and y
                         */
                        initialX = params.x;
                        initialY = params.y;

                        /**
                         * get location on touch
                         */
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        int xDiff = (int) (event.getRawX() - initialTouchY);
                        int yDiff = (int) (event.getRawY() - initialTouchY);

                        /**
                         * Check for xDiff <10 && yDiff <10
                         * Because sometimes elements move a little while clicking.
                         * So that is click event.
                         */
                        if (xDiff < 10 && yDiff < 10) {
                            if (isViewCollapsed()) {
                                /**
                                 * When user clicks on the image view of the collapsed layout,
                                 * visibility of the collapsed layout will be changed to "View.GONE"
                                 * and expanded view will become "View.VISIBLE".
                                 */
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        /**
                         * Calculate the X and Y coordinates of the view.
                         */
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        /**
                         * Update the layout with new x and y coordinate's
                         */
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

    }

    private void initilizeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(FloatingViewService.this, R.raw.despacito);
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return floatingView == null || floatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.stop();

        if (floatingView != null)
            windowManager.removeView(floatingView);
    }
}
