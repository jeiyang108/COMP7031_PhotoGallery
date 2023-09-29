package com.example.photogallery;
import org.hamcrest.Matcher;
import org.junit.Rule; import org.junit.Test;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;

import android.location.Location;
import android.view.View;
import android.widget.DatePicker;

import java.security.cert.PKIXParameters;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat; import java.util.Calendar; import java.util.Date;
import java.util.Locale;
public class UITest {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test /* Find Photos Test - 1.4 verify that correct photos are found even when a longer time window is specified that may fetch more than one photo.
     */
    public void timeBasedSearchMultiple() throws Exception {
        Date startTimestamp = null, endTimestamp = null;
        String startTimeValue = "20230927_220307";
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            // Set the following to the timestamp of one of the photos in the pictures folder
            startTimestamp = format.parse(startTimeValue);
            calendar.setTime(startTimestamp);
            startTimestamp = calendar.getTime();
            calendar.add(Calendar.MINUTE, 10); // Extend the time window for multiple photos
            endTimestamp = calendar.getTime();

        } catch (Exception ex) { }

        // Find and Click the Search Button
        onView(withText("search")).perform(click());

        // Find the "From Date" and "To Date" EditText fields
        onView(withId(R.id.etFromDateTime)).perform(click());
        // Select a date in the DatePicker (you may need to adjust the date values)
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2023, 9, 27));
        // Confirm the date selection
        onView(withText("OK")).perform(click());

        // Find the "To Date" EditText field
        onView(withId(R.id.etToDateTime)).perform(click());
        // Select a date in the DatePicker (you may need to adjust the date values)
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2023, 9, 27));
        // Confirm the date selection
        onView(withText("OK")).perform(click());


        // Leave the keywords field blank
        onView(withId(R.id.etKeywords)).perform(replaceText(""), closeSoftKeyboard());

        // Find and Click the GO button on the Search View
        onView(withId(R.id.btnGo)).perform(click());

        // Extract and parse timestamps from tvTimestamp
        Date firstDisplayedTimestamp = parseTimestampFromText(getTextFromView(withId(R.id.tvTimestamp)));
        Date currentDisplayedTimestamp = firstDisplayedTimestamp;

        // Verify that the displayed timestamp is after the startTimeValue
        assertTrue(firstDisplayedTimestamp != null && firstDisplayedTimestamp.after(startTimestamp));
        // Additional verification for multiple photos found
        boolean multiplePhotosFound = false;

        // Loop through the photos and verify their timestamps
        do {
            onView(withId(R.id.btnNext)).perform(click());
            currentDisplayedTimestamp = parseTimestampFromText(getTextFromView(withId(R.id.tvTimestamp)));

            // Check if the displayed timestamp is within the specified time window
            if (currentDisplayedTimestamp.after(startTimestamp)) {
                multiplePhotosFound = true;
            }
        } while (!firstDisplayedTimestamp.equals(currentDisplayedTimestamp));

        // Assert that multiple photos within the time window were found
        assertTrue(multiplePhotosFound);
    }

    @Test /* Find Photos Test - 1.5 Keyword-based search*/
    public void keywordBasedSearch() throws Exception {
        String keyword = "caption";

        //Find and Click the Search Button
        onView(withText("search")).perform(click());
        //Find Keyword in the Search layout and fill these with the above test data
        onView(withId(R.id.etKeywords)).perform(replaceText(keyword), closeSoftKeyboard());
        //leave the keywords field blank
        onView(withId(R.id.etKeywords)).perform(replaceText(""), closeSoftKeyboard());
        //Find and Click the GO button on the Search View
        onView(withId(R.id.btnGo)).perform(click());
        //Verify that the caption of the found Image matches the Expected value
        onView(withId(R.id.etCaption)).check(matches(withText(keyword)));
    }

    @Test /* Find Photos Test - Location-based Search*/
    public void locationBasedSearch() throws Exception {
        String latitude = "37.42200";
        //Find and Click the Search Button
        onView(withText("search")).perform(click());
        //Find Keyword in the Search layout and fill these with the above test data
        onView(withId(R.id.etLatitude)).perform(replaceText(latitude), closeSoftKeyboard());
        //leave the keywords field blank
        onView(withId(R.id.etLongitude)).perform(replaceText(""), closeSoftKeyboard());
        //Find and Click the GO button on the Search View
        onView(withId(R.id.btnGo)).perform(click());
        //Verify that the location value of the found Image contains the expected latitude
        assertTrue(getTextFromView(withId(R.id.tvLocation)).contains(latitude));

    }

    @Test /* Find Photos Test*/
    public void timeBasedSearch() throws Exception {
        Date startTimestamp = null, endTimestamp = null;
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
            //Set the following to the timestamp of one of the photos in the pictures folder
            startTimestamp = format.parse("20220228_220307");
            calendar.setTime(startTimestamp);
            startTimestamp = calendar.getTime();
            calendar.add(Calendar.MINUTE, 1);
            endTimestamp = calendar.getTime();
        } catch (Exception ex) { }
        //Find and Click the Search Button
        onView(withText("search")).perform(click());
        //Find From and To fields in the Search layout and fill these with the above test data
        String from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(startTimestamp);
        String to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(endTimestamp);
        onView(withId(R.id.etFromDateTime)).perform(replaceText(from), closeSoftKeyboard());
        onView(withId(R.id.etToDateTime)).perform(replaceText(to), closeSoftKeyboard());
        //leave the keywords field blank
        onView(withId(R.id.etKeywords)).perform(replaceText(""), closeSoftKeyboard());
        //Find and Click the GO button on the Search View
        onView(withId(R.id.btnGo)).perform(click());
        //Verify that the timestamp of the found Image matches the Expected value
        onView(withId(R.id.tvTimestamp)).check(matches(withText("20220228_220307")));
    }



    private String getTextFromView(Matcher<View> matcher) {
        // Extract text from a view and return it as a string
        String text = "";
        try {
            onView(matcher).check(matches(isDisplayed()));
            text = onView(matcher).toString();
        } catch (Exception e) {
            // Handle any exceptions if needed
        }
        return text;
    }

    private Date parseTimestampFromText(String text) {
        try {
            // Extract timestamp from text and convert it to a Date object
            String timestampString = text.replace("tvTimestamp: ", "");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            return format.parse(timestampString);
        } catch (ParseException ex) {
            return null;
        }
    }
}