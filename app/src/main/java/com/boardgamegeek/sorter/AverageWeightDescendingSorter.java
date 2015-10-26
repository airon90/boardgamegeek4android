package com.boardgamegeek.sorter;

import android.content.Context;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Collection;

public class AverageWeightDescendingSorter extends AverageWeightSorter {
	public AverageWeightDescendingSorter(Context context) {
		super(context);
		orderByClause = getClause(Collection.STATS_AVERAGE_WEIGHT, true);
		subDescriptionId = R.string.heaviest;
	}

	@Override
	public int getType() {
		return CollectionSorterFactory.TYPE_AVERAGE_WEIGHT_DESC;
	}
}
