package com.bue.rssnotifier.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.bue.rssnotifier.R;
import com.bue.rssnotifier.StartScreen;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Xml;

public class RssNotificationListenerService extends IntentService {
	private URL feedUrl;
	private URLConnection feedURLConnection;
	private InputStream inputStream;

	public RssNotificationListenerService() {
		super(null);

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences spUpdate = getSharedPreferences("rssUpdate", 0);
		String lastTitle = spUpdate.getString("lastTitle", "No feed");
		String feedLink = spUpdate.getString("feedLink",
				"http://news.cnet.com/8300-1023_3-93.xml");// "http://www.contra.gr/?widget=rssfeed&view=feed&contentId=1169269");
		String rssFeed = "";
		// Initialize Connection
		try {
			feedUrl = new URL(feedLink);
			feedURLConnection = feedUrl.openConnection();
			inputStream = feedURLConnection.getInputStream();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		String title = "";
		String summary = "";
		String feedTitle= "";
		try {
			CharSequence[] entry = getEntry(rssFeed);
			title = (String) entry[0];
			summary = (String) entry[1];
			feedTitle=(String) entry[2];
			if (!lastTitle.equalsIgnoreCase(title)) {
				showNotification(title, summary, feedTitle);
				// store last title so next time will not notify for same rss
				Editor editor = spUpdate.edit();
				editor.putString("lastTitle", title);
				editor.commit();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CharSequence[] getEntry(String rssFeed)
			throws XmlPullParserException, IOException {
		CharSequence[] title = { "No Feed Found", "No Feed Found", "No Feed Found" };
		XmlPullParser rssParser = Xml.newPullParser();
		rssParser.setInput(inputStream, null);

		// Parse the XML
		int eventType = -1;
		boolean foundEntry = false;
		boolean firstEntry = false;

		while (eventType != XmlPullParser.END_DOCUMENT && !foundEntry) {
			if (eventType == XmlPullParser.START_TAG) {

				String strName = rssParser.getName();
				if(!firstEntry && strName.equals("title")) {
					title[2] = rssParser.nextText();
				} else if(strName.equals("item")) {
					firstEntry = true;
				} else if (firstEntry) {
					if (strName.equalsIgnoreCase("title")) {
						title[0] = rssParser.nextText();
					} else if (strName.equalsIgnoreCase("description")) {
						title[1] = rssParser.nextText();
					}
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				String strName = rssParser.getName();
				if (strName.equals("item"))
					foundEntry = true;
			}
			eventType = rssParser.next();
		}
		return title;
	}

	private void showNotification(String title, String summary, String feedTitle) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(feedTitle).setContentText(title)
				.setAutoCancel(true);
		// Creates an Intent that shows the title and a description of the feed
		Intent resultIntent = new Intent(this, StartScreen.class);
		resultIntent.putExtra("title", title);
		resultIntent.putExtra("summary", summary);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(StartScreen.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 1;
		mNotificationManager.notify(mId, mBuilder.build());
	}

}
