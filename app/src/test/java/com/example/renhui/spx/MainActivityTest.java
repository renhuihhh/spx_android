package com.example.renhui.spx;


import android.widget.TextView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

/**
 * @author renhui
 * @date 16-9-19
 * @desc com.example.renhui.spx
 */
public class MainActivityTest extends RoboTestBase {
    private ActivityController mController;
    private MainActivity       mActivity;

    private TextView titleTv;

    @Before
    public void setUp() throws Exception {
        // Create new activity
        super.setUp();
        mController = Robolectric.buildActivity(MainActivity.class).create().start().visible();
        mActivity = (MainActivity) mController.get();
    }

    @After
    public void tearDown() throws Exception {
        // Destroy activity
        super.tearDown();
        mController.pause().destroy();
    }

    @Test
    public void testOnCreateNotNull() {
        Assert.assertNotNull(this.mActivity);
    }

    @Test
    public void testTvTitleIsNotNull() {
        // find views
        titleTv = (TextView) mActivity.findViewById(R.id.title_txt);
        Assert.assertNotNull(titleTv);
    }
}
