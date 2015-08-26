package com.autonomousapps.alexandriaplays;

import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;

import org.junit.Before;

/**
 * Created by Tony Robalik (@AutonomousApps) on 8/26/15.
 */
public class MapsActivityTest extends InstrumentationTestCase {

    private static final String PKG = "com.autonomousapps.alexandriaplays";

    private UiDevice mDevice;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());

        Context context = getInstrumentation().getContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(PKG);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        mDevice.wait(Until.hasObject(By.pkg(PKG).depth(0)), 5000);
    }

    public void testTapMarkerShowsSlider() throws Exception {
        // Wait until a marker is present
//        mDevice.wait(Until.hasObject(By.clazz(Marker.class)), 5000);
        mDevice.wait(Until.findObject(By.desc("Cameron Parke Townes")), 5000);

        // Get specific marker and click it
        UiObject marker = mDevice.findObject(
                new UiSelector().descriptionContains("Cameron Parke Townes"));
        marker.click();

        // Swipe slider up
        UiObject slider = mDevice.findObject(new UiSelector().description("slider"));
        slider.swipeUp(1);

        // Make some assertions
        UiObject restrooms = mDevice.findObject(new UiSelector().textContains("Restrooms"));
        assertEquals("Expected <Restrooms: Yes>, was <" + restrooms.getText() + ">",
                "Restrooms: Yes", restrooms.getText());

        UiObject water = mDevice.findObject(new UiSelector().textContains("Water"));
        assertEquals("Water fountains: Yes", water.getText());

        UiObject seating = mDevice.findObject(new UiSelector().textContains("Seating"));
        assertEquals("Seating: Yes", seating.getText());

        UiObject shade = mDevice.findObject(new UiSelector().textContains("Shade"));
        assertEquals("Shade: Yes", shade.getText());

        UiObject age = mDevice.findObject(new UiSelector().textContains("Age"));
        assertEquals("Age Level: ?", age.getText());
    }

    // TODO figure out how to get handle on the Google Map, and then how to test that the slider is NOT present
    // UiAutomator documentation sucks

    public void testTapMapClearsSlider() throws Exception {
        mDevice.wait(Until.hasObject(By.desc("Cameron Parke Townes")), 5000);

        // Get specific marker and click it
        UiObject marker = mDevice.findObject(
                new UiSelector().descriptionContains("Cameron Parke Townes"));
        marker.click();

        // Swipe slider up
        UiObject slider = mDevice.findObject(new UiSelector().description("slider"));
        slider.swipeUp(1);

        // Now tap to clear the slider
        UiObject map = mDevice.findObject(new UiSelector().description("map"));
        map.click();

        mDevice.wait(Until.gone(By.desc("slider")), 5000);

        slider = mDevice.findObject(new UiSelector().description("slider"));
    }
}