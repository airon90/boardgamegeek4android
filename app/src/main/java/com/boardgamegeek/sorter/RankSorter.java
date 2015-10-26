package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Games;

public class RankSorter extends CollectionSorter {
	private final String defaultHeaderText;
	private final String defaultText;
	private static final SparseArray<String> RANKS = buildRanks();

	public RankSorter(Context context) {
		super(context);
		orderByClause = getClause(Games.GAME_RANK, false);
		descriptionId = R.string.menu_collection_sort_rank;
		defaultHeaderText = context.getResources().getString(R.string.unranked);
		defaultText = context.getResources().getString(R.string.text_not_available);
	}

	private static SparseArray<String> buildRanks() {
		SparseArray<String> ranks = new SparseArray<>();
		ranks.put(100, "1 - 100");
		ranks.put(250, "101 - 250");
		ranks.put(500, "251 - 500");
		ranks.put(1000, "501 - 1000");
		ranks.put(2500, "1001 - 2500");
		ranks.put(5000, "2501 - 5000");
		ranks.put(10000, "5001 - 10000");
		ranks.put(Integer.MAX_VALUE, "10001+");
		return ranks;
	}

	@Override
	public int getType() {
		return CollectionSorterFactory.TYPE_RANK;
	}

	@Override
	public String[] getColumns() {
		return new String[] { Games.GAME_RANK };
	}

	@Override
	public String getHeaderText(Cursor cursor) {
		int rank = getInt(cursor, Games.GAME_RANK, Integer.MAX_VALUE);
		for (int i = 0; i < RANKS.size(); i++) {
			int key = RANKS.keyAt(i);
			if (rank <= key) {
				return RANKS.get(key);
			}
		}
		return defaultHeaderText;
	}

	@Override
	public String getDisplayInfo(Cursor cursor) {
		int rank = getInt(cursor, Games.GAME_RANK, Integer.MAX_VALUE);
		if (rank == Integer.MAX_VALUE) {
			return defaultText;
		}
		return String.valueOf(rank);
	}
}
