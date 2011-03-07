package com.tamil.kadhambam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tamil.TChar;
import com.tamil.TString;
import com.tamil.kadhambam.arangam.FinishActivity;

public class WordDragger extends LinearLayout {

	private LinearLayout charLayout;
	private LinkedList<String> words;
	private final Context context;
	private final Typeface tf;
	private TString currentWord;
	private LinearLayout footer;
	private Button nextButton;
	private Button jumbleButton;
	private final FinishActivity finishActivity;

	public WordDragger(Context context, Typeface tf, FinishActivity finishActivity) {
		super(context);
		this.finishActivity = finishActivity;
		setOrientation(LinearLayout.VERTICAL);
		this.context = context;
		this.tf = tf;
		charLayout = new LinearLayout(context);
		charLayout.setGravity(Gravity.CENTER);
		charLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
		addView(charLayout);
		setBackgroundResource(R.layout.app_bg);
		footer = new LinearLayout(context);
		addJumbleButton(context);
		addNextButton(context);
		addView(footer);
	}

	private void addNextButton(Context context) {
		nextButton = new Button(context);
		nextButton.setText("����");
		nextButton.setTypeface(tf);
		nextButton.setVisibility(INVISIBLE);
		nextButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextButton.setVisibility(INVISIBLE);
				jumbleButton.setVisibility(VISIBLE);
				currentWord = new TString(words.getFirst());
				rerender(currentWord.getJumbledChars(), false);
			}
		});
		footer.addView(nextButton);		
	}

	private void addJumbleButton(Context context) {
		jumbleButton = new Button(context);
		jumbleButton.setText("���Ȣ¨�");
		jumbleButton.setTypeface(tf);
		jumbleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rerender(currentWord.getJumbledChars(), false);
			}
		});
		jumbleButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		footer.addView(jumbleButton);
	}

	public void render(LinkedList<String> words) {
		this.words = words;
		currentWord = new TString(words.getFirst());
		rerender(currentWord.getJumbledChars(), false);
	}

	private void rerender(final List<TChar> tamilChars, boolean isComplete) {
		charLayout.removeAllViews();
		if (isComplete) {
			completeView();
			return;

		}
		for (TChar tChar : tamilChars) {
			Button charView = new Button(context);
			styleView(charView, tChar.getChar(), 20, 0xFF000000);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 50, 1);
			charView.setLayoutParams(layoutParams);
			charView.setBackgroundResource(R.layout.char_bg);
			charView.setDrawingCacheEnabled(true);
			charLayout.addView(charView);
			charView.setOnTouchListener(new DragListener(tamilChars));
		}
	}

	private void completeView() {
		List<TChar> tChars = currentWord.getChars();

		for (TChar tChar : tChars) {
			Button charView = new Button(context);
			styleView(charView, tChar.getChar(), 20, 0xFF000000);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 50, 1);
			charView.setLayoutParams(layoutParams);
			charView.setBackgroundResource(R.layout.completed_bg);
			charLayout.addView(charView);
			words.removeFirst();
		}
		if (!words.isEmpty()) {
			nextButton.setVisibility(VISIBLE);
			jumbleButton.setVisibility(INVISIBLE);
		} else finishActivity.endGame();
	}

	private View styleView(TextView view, String text, int size, int color) {
		view.setText(text);
		view.setTextSize(size);
		view.setTextColor(color);
		view.setTypeface(tf);
		view.setPadding(10, 10, 10, 10);
		view.setGravity(Gravity.CENTER);
		return view;
	}

	private class DragListener implements OnTouchListener {

		private final List<TChar> tChars;

		public DragListener(List<TChar> tChars) {
			this.tChars = tChars;
		}

		@Override
		public boolean onTouch(View view, MotionEvent me) {
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				view.setBackgroundResource(R.layout.drag_bg);
			}
			if (me.getAction() == MotionEvent.ACTION_UP) {
				List<TChar> newList = constructNewTamilWord(charLayout, tChars,
						view, me);

				boolean isWordFound = currentWord.getChars().equals(newList);
				if (isWordFound && !words.isEmpty()) {
					raiseAToast();
				}
				rerender(newList, isWordFound);
			}
			return false;
		}

		private void raiseAToast() {
			Toast toast = new Toast(context);
			LinearLayout toastLayout = new LinearLayout(context);
			toastLayout.addView(styleView(new TextView(context), "š�����",
					25, 0xff00ff00));
			toast.setView(toastLayout);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.show();
		}

		private List<TChar> constructNewTamilWord(
				final LinearLayout charLayout, final List<TChar> tamilChars,
				View view, MotionEvent me) {
			int offset = charLayout.getWidth() / tamilChars.size();
			int targetPosition = ((int) me.getRawX() / offset);
			targetPosition = (targetPosition >= tamilChars.size()) ? tamilChars
					.size() - 1 : targetPosition;
			int currentPosition = view.getLeft() / offset;
			List<TChar> newList = new ArrayList<TChar>();
			TChar tempChar = null;
			for (int i = 0; i < tamilChars.size(); i++) {
				if (i == currentPosition) {
					tempChar = tamilChars.get(i);
				} else if (i == targetPosition && tempChar != null
						&& newList.size() == i + 1) {
					newList.add(tempChar);
				} else {
					newList.add(tamilChars.get(i));
				}
			}
			if (tamilChars.size() != newList.size()) {
				newList.add(targetPosition, tempChar);
			}
			return newList;
		}
	}
}
