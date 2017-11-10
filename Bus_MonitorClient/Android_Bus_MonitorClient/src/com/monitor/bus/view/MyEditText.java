package com.monitor.bus.view;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monitor.bus.activity.R;

/**
 * 自定义控件
 */
public class MyEditText extends LinearLayout {
	private TextView edit_text;
	private EditText edit_input;

	public MyEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.edittext_theme, this,
				true);
		edit_text = (TextView) findViewById(R.id.edit_text_left);
		edit_input = (EditText) findViewById(R.id.edit_text_input);
	}

	/**
	 * 设置要显示的文字
	 * 
	 * @param textId
	 * @param editId
	 */
	public void setTextViewText(int textId, String editId) {
		edit_text.setText(textId);
		edit_input.setText(editId);
	}

	/**
	 * 获取输入框输入的格式
	 * 
	 * @return
	 */
	public String getEditInputValue() {
		return edit_input.getText().toString();
	}

	/**
	 * 设置密码输入格式
	 */
	public void setEditPasswordType() {
		edit_input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	/**
	 * 设置数字输入格式
	 */
	public void setEditNumberType() {
		edit_input.setInputType(InputType.TYPE_CLASS_NUMBER);
		edit_input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				5) });
	}

	/**
	 * 设置IP地址输入格式
	 * 
	 * @param resid
	 */
	public void setIpConfigType() {
		edit_input.setKeyListener(new NumberKeyListener() {

			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}

			@Override
			protected char[] getAcceptedChars() {
				return new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
						'8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
						'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
						't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
						'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
						'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
			}
		});
	}

	public void setEditHint(int resid) {
		edit_input.setHint(resid);
	}

	public void setEditFocus() {
		edit_input.requestFocus();
	}
}
