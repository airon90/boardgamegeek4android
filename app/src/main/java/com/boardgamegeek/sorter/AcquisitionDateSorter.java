package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Collection;
import com.boardgamegeek.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AcquisitionDateSorter extends CollectionSorter {
	private static final String COLUMN_NAME = Collection.PRIVATE_INFO_ACQUISITION_DATE;
	private static final SimpleDateFormat API_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
	private final String defaultValue;

	public AcquisitionDateSorter(Context context) {
		super(context);
		orderByClause = getClause(COLUMN_NAME, true);
		descriptionId = R.string.menu_collection_sort_acquisition_date;
		defaultValue = context.getString(R.string.text_unknown);
	}

	@Override
	public int getType() {
		return CollectionSorterFactory.TYPE_ACQUISITION_DATE;
	}

	@Override
	public String[] getColumns() {
		return new String[] { Collection.PRIVATE_INFO_ACQUISITION_DATE };
	}

	@Override
	public String getHeaderText(Cursor cursor) {
		long time = getTime(cursor);
		if (time == DateTimeUtils.UNKNOWN_DATE) {
			return defaultValue;
		}
		return DISPLAY_FORMAT.format(time);
	}

	@Override
	public String getDisplayInfo(Cursor cursor) {
		long time = getTime(cursor);
		if (time == DateTimeUtils.UNKNOWN_DATE) {
			return defaultValue;
		}
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
	}

	private long getTime(Cursor cursor) {
		String date = getString(cursor, Collection.PRIVATE_INFO_ACQUISITION_DATE, defaultValue);
		return DateTimeUtils.tryParseDate(DateTimeUtils.UNPARSED_DATE, date, API_FORMAT);
	}
}
