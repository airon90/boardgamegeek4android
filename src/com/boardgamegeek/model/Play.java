package com.boardgamegeek.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.boardgamegeek.provider.BggContract.PlayItems;
import com.boardgamegeek.provider.BggContract.Plays;
import com.boardgamegeek.util.CursorUtils;

public class Play {
	private static final String TAG = "Play";
	private static final String KEY_PLAY_ID = "PLAY_ID";
	private static final String KEY_GAME_ID = "GAME_ID";
	private static final String KEY_YEAR = "YEAR";
	private static final String KEY_MONTH = "MONTH";
	private static final String KEY_DATY = "DAY";
	private static final String KEY_QUANTITY = "QUANTITY";
	private static final String KEY_LENGTH = "LENGTH";
	private static final String KEY_LOCATION = "LOCATION";
	private static final String KEY_INCOMPLETE = "INCOMPLETE";
	private static final String KEY_NOWINSTATS = "NO_WIN_STATS";
	private static final String KEY_COMMENTS = "COMMENTS";
	private static final String KEY_PLAYERS = "PLAYERS";
	private static final String KEY_UPDATED = "UPDATED";

	private DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
	private List<Player> mPlayers = new ArrayList<Player>();

	public Play() {
		init(-1);
	}

	public Play(int gameId) {
		init(gameId);
	}

	private void init(int gameId) {
		GameId = gameId;
		Quantity = 1;
		// set current date
		final Calendar c = Calendar.getInstance();
		Year = c.get(Calendar.YEAR);
		Month = c.get(Calendar.MONTH);
		Day = c.get(Calendar.DAY_OF_MONTH);
	}

	public Play(Bundle bundle) {
		PlayId = bundle.getInt(KEY_PLAY_ID);
		GameId = bundle.getInt(KEY_GAME_ID);
		Year = bundle.getInt(KEY_YEAR);
		Month = bundle.getInt(KEY_MONTH);
		Day = bundle.getInt(KEY_DATY);
		Quantity = bundle.getInt(KEY_QUANTITY);
		Length = bundle.getInt(KEY_LENGTH);
		Location = bundle.getString(KEY_LOCATION);
		Incomplete = bundle.getBoolean(KEY_INCOMPLETE);
		NoWinStats = bundle.getBoolean(KEY_NOWINSTATS);
		Comments = bundle.getString(KEY_COMMENTS);
		Updated = bundle.getLong(KEY_UPDATED);
		mPlayers = bundle.getParcelableArrayList(KEY_PLAYERS);
	}

	public int PlayId;
	public int GameId;
	public int Year;
	public int Month;
	public int Day;
	public int Quantity;
	public int Length;
	public String Location;
	public boolean Incomplete;
	public boolean NoWinStats;
	public String Comments;
	public long Updated;

	public void populate(Cursor c) {
		PlayId = CursorUtils.getInt(c, Plays.PLAY_ID);
		GameId = CursorUtils.getInt(c, PlayItems.OBJECT_ID);
		String date = CursorUtils.getString(c, Plays.DATE);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date d = sdf.parse(date);
			Year = d.getYear() + 1900;
			Month = d.getMonth();
			Day = d.getDate();
		} catch (ParseException e) {
			Log.w(TAG, "Couldn't parse " + date);
		}
		Quantity = CursorUtils.getInt(c, Plays.QUANTITY, 1);
		Length = CursorUtils.getInt(c, Plays.LENGTH);
		Location = CursorUtils.getString(c, Plays.LOCATION);
		Incomplete = CursorUtils.getBoolean(c, Plays.INCOMPLETE);
		NoWinStats = CursorUtils.getBoolean(c, Plays.NO_WIN_STATS);
		Comments = CursorUtils.getString(c, Plays.COMMENTS);
		Updated = CursorUtils.getLong(c, Plays.UPDATED_LIST);
	}

	public List<Player> getPlayers() {
		return mPlayers;
	}

	public String getFormattedDate() {
		return String.format("%04d", Year) + "-" + String.format("%02d", Month + 1) + "-" + String.format("%02d", Day);
	}

	public CharSequence getDateText() {
		return df.format(new Date(Year - 1900, Month, Day));
	}

	public void setDate(int year, int month, int day) {
		Year = year;
		Month = month;
		Day = day;
	}

	public void clearPlayers() {
		mPlayers.clear();
	}

	public void addPlayer(Player player) {
		mPlayers.add(player);
	}

	public void saveState(Bundle bundle) {
		bundle.putInt(KEY_PLAY_ID, PlayId);
		bundle.putInt(KEY_GAME_ID, GameId);
		bundle.putInt(KEY_YEAR, Year);
		bundle.putInt(KEY_MONTH, Month);
		bundle.putInt(KEY_DATY, Day);
		bundle.putInt(KEY_QUANTITY, Quantity);
		bundle.putInt(KEY_LENGTH, Length);
		bundle.putString(KEY_LOCATION, Location);
		bundle.putBoolean(KEY_INCOMPLETE, Incomplete);
		bundle.putBoolean(KEY_NOWINSTATS, NoWinStats);
		bundle.putString(KEY_COMMENTS, Comments);
		bundle.putLong(KEY_UPDATED, Updated);
		bundle.putParcelableArrayList(KEY_PLAYERS, (ArrayList<? extends Parcelable>) mPlayers);
	}

	public List<NameValuePair> toNameValuePairs() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("ajax", "1"));
		nvps.add(new BasicNameValuePair("action", "save"));
		nvps.add(new BasicNameValuePair("version", "2"));
		nvps.add(new BasicNameValuePair("objecttype", "thing"));
		if (PlayId > 0) {
			nvps.add(new BasicNameValuePair("playid", String.valueOf(PlayId)));
		}
		nvps.add(new BasicNameValuePair("objectid", String.valueOf(GameId)));
		nvps.add(new BasicNameValuePair("playdate", getFormattedDate()));
		// TODO: ask Aldie what this is
		nvps.add(new BasicNameValuePair("dateinput", getFormattedDate()));
		nvps.add(new BasicNameValuePair("length", String.valueOf(Length)));
		nvps.add(new BasicNameValuePair("location", Location));
		nvps.add(new BasicNameValuePair("quantity", String.valueOf(Quantity)));
		nvps.add(new BasicNameValuePair("incomplete", Incomplete ? "1" : "0"));
		nvps.add(new BasicNameValuePair("nowinstats", NoWinStats ? "1" : "0"));
		nvps.add(new BasicNameValuePair("comments", Comments));

		for (int i = 0; i < mPlayers.size(); i++) {
			nvps.addAll(mPlayers.get(i).toNameValuePairs(i));
		}

		Log.d(TAG, nvps.toString());
		return nvps;
	}

	public String toShortDescription(String gameName) {
		return "Played " + gameName + " on " + getFormattedDate();
	}

	public String toLongDescription(String gameName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Played ").append(gameName);
		if (Quantity > 1) {
			sb.append(" ").append(Quantity).append(" times");
		}
		sb.append(" on ").append(getFormattedDate());
		if (!TextUtils.isEmpty(Location)) {
			sb.append(" at ").append(Location);
		}
		if (mPlayers.size() > 0) {
			sb.append(" with ").append(mPlayers.size()).append(" players");
		}
		sb.append(" (www.boardgamegeek.com/boardgame/").append(GameId).append(")");
		return sb.toString();
	}
}
