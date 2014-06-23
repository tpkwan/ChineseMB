package com.ijoomer.customviews;

import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.configuration.IjoomerApplicationConfiguration;

/**
 * This Class Contains All Method Related To IjoomerTextView.
 * 
 * @author tasol
 * 
 */
public class IjoomerTextView extends TextView {

	private boolean isDecodeEmojis() {
		return isDecodeEmojis;
	}

	public void setDecodeEmojis(boolean decodeEmojis) {
		isDecodeEmojis = decodeEmojis;
	}

	private boolean isDecodeEmojis = true;

	public IjoomerTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public IjoomerTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public IjoomerTextView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context mContext) {
		setLineSpacing(2, 1);
		try {
			if (IjoomerApplicationConfiguration.getFontFace() != null) {
				setTypeface(IjoomerApplicationConfiguration.getFontFace());
			} else {
				Typeface tf = Typeface.createFromAsset(mContext.getAssets(), IjoomerApplicationConfiguration.getFontNameWithPath());
				setTypeface(tf);
				IjoomerApplicationConfiguration.setFontFace(tf);
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (isDecodeEmojis) {
			try {
				super.setText(getSmiledText(text), type);
			} catch (Exception e) {
			}
		} else {
			super.setText(text, type);
		}
	}

	public Spannable getSmiledText(CharSequence text) {
        SpannableStringBuilder builder;

        try {
            builder = (SpannableStringBuilder) text;
        }catch (Exception e){
            builder = new SpannableStringBuilder(text);
        }
		if (IjoomerUtilities.getEmojisHashMap().size() > 0) {
			int index;
			for (index = 0; index < builder.length(); index++) {
				if (Character.toString(builder.charAt(index)).equals(":")) {
					for (Map.Entry<String, Integer> entry : IjoomerUtilities.getEmojisHashMap().entrySet()) {
						int length = entry.getKey().length();
						if (index + length > builder.length())
							continue;
						if (builder.subSequence(index, index + length).toString().equals(entry.getKey())) {
							builder.setSpan(new ImageSpan(getContext(), entry.getValue()), index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							index += length - 1;
							break;
						}
					}
				}
			}
		}
		return builder;
	}

}
