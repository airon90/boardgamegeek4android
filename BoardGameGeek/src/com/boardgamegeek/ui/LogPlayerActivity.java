package com.boardgamegeek.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.boardgamegeek.R;
import com.boardgamegeek.model.Player;
import com.boardgamegeek.provider.BggContract.Buddies;
import com.boardgamegeek.util.StringUtils;
import com.boardgamegeek.util.UIUtils;

public class LogPlayerActivity extends Activity {
	// private static final String TAG = "LogPlayerActivity";

	public static final String KEY_GAME_NAME = "GAME_NAME";
	public static final String KEY_THUMBNAIL_URL = "THUMBNAIL_URL";

	private String mGameName;
	private String mThumbnailUrl;

	private UsernameAdapter mAdapter;
	private Player mPlayer;

	private EditText mName;
	private AutoCompleteTextView mUsername;
	private EditText mTeamColor;
	private EditText mStartingPosition;
	private EditText mScore;
	private EditText mRating;
	private CheckBox mNew;
	private CheckBox mWin;
	private AlertDialog mCancelDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logplayer);
		setUiVariables();
		UIUtils.setTitle(this);
		mCancelDialog = UIUtils.createCancelDialog(this);

		if (savedInstanceState == null) {
			final Intent intent = getIntent();
			mGameName = intent.getExtras().getString(KEY_GAME_NAME);
			mThumbnailUrl = intent.getExtras().getString(KEY_THUMBNAIL_URL);

			mPlayer = new Player(intent);
			bindUi();

			UIUtils u = new UIUtils(this);
			u.setGameName(mGameName);
			u.setThumbnail(mThumbnailUrl);
		}

		mAdapter = new UsernameAdapter(this);
		mUsername.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.logplay, menu);
		return true;
	}

	private void setUiVariables() {
		mName = (EditText) findViewById(R.id.logPlayerName);
		mUsername = (AutoCompleteTextView) findViewById(R.id.logPlayerUsername);
		mTeamColor = (EditText) findViewById(R.id.logPlayerTeamColor);
		mStartingPosition = (EditText) findViewById(R.id.logPlayerStartingPosition);
		mScore = (EditText) findViewById(R.id.logPlayerScore);
		mRating = (EditText) findViewById(R.id.logPlayerRating);
		mNew = (CheckBox) findViewById(R.id.logPlayerNew);
		mWin = (CheckBox) findViewById(R.id.logPlayerWin);
	}

	private void bindUi() {
		mName.setText(mPlayer.Name);
		mUsername.setText(mPlayer.Username);
		mTeamColor.setText(mPlayer.TeamColor);
		mStartingPosition.setText(mPlayer.StartingPosition);
		mScore.setText(mPlayer.Score);
		mRating.setText("" + mPlayer.Rating);
		mNew.setChecked(mPlayer.New);
		mWin.setChecked(mPlayer.Win);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				save();
				return true;
			case R.id.cancel:
				cancel();
				return true;
		}
		return false;
	}

	@Override
	public void setTitle(CharSequence title) {
		UIUtils.setTitle(this, title);
	}

	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	public void onSearchClick(View v) {
		onSearchRequested();
	}

	public void onSaveClick(View v) {
		save();
	}

	public void onCancelClick(View v) {
		cancel();
	}

	private void save() {
		captureForm();
		setResult(RESULT_OK, mPlayer.toIntent());
		finish();
	}

	private void cancel() {
		mCancelDialog.show();
	}

	private void captureForm() {
		mPlayer.Name = mName.getText().toString();
		mPlayer.Username = mUsername.getText().toString();
		mPlayer.TeamColor = mTeamColor.getText().toString();
		mPlayer.StartingPosition = mStartingPosition.getText().toString();
		mPlayer.Score = mScore.getText().toString();
		mPlayer.Rating = StringUtils.parseDouble(mRating.getText().toString());
		mPlayer.New = mNew.isChecked();
		mPlayer.Win = mWin.isChecked();
	}

	private class UsernameAdapter extends CursorAdapter {
		public UsernameAdapter(Context context) {
			super(context, null);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.list_item, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final TextView textView = (TextView) view.findViewById(R.id.list_item);
			textView.setText(cursor.getString(BuddiesQuery.NAME));
		}

		@Override
		public CharSequence convertToString(Cursor cursor) {
			return cursor.getString(BuddiesQuery.NAME);
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			String selection = null;
			if (!TextUtils.isEmpty(constraint)) {
				selection = Buddies.BUDDY_NAME + " LIKE '" + constraint + "%'";
			}
			return getContentResolver().query(Buddies.CONTENT_URI, BuddiesQuery.PROJECTION, selection, null,
					Buddies.NAME_SORT);
		}
	}

	private interface BuddiesQuery {
		String[] PROJECTION = { Buddies._ID, Buddies.BUDDY_NAME, };
		// int _ID = 0;
		int NAME = 1;
	}
}
