package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Games;

public class LastViewedSorter extends CollectionSorter {
	private final String never;

	public LastViewedSorter(Context context) {
		super(context);
		descriptionId = R.string.menu_collection_sort_last_viewed;
		orderByClause = getClause(Games.LAST_VIEWED, true);
		never = context.getString(R.string.never);
	}

	@Override
	public int getType() {
		return CollectionSorterFactory.TYPE_LAST_VIEWED;
	}

	@Override
	public String[] getColumns() {
		return new String[] { Games.LAST_VIEWED };
	}

	@Override
	public String getHeaderText(Cursor cursor) {
		long time = getLong(cursor, Games.LAST_VIEWED);
		if (time == 0) {
			return never;
		}
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
	}

	@Override
	public String getDisplayInfo(Cursor cursor) {
		long time = getLong(cursor, Games.LAST_VIEWED);
		if (time == 0) {
			return never;
		}
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
	}
}
