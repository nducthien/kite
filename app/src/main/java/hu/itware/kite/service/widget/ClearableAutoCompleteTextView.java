package hu.itware.kite.service.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.annotation.XmlRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.MovementMethod;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Scroller;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Locale;

import hu.itware.kite.service.R;

/**
 * Created by szeibert on 2017.06.09..
 */

public class ClearableAutoCompleteTextView extends LinearLayout {

    public interface OnClearListener {
        void onClear();
    }

    private AutoCompleteTextView text;
    private ImageView icon;

    private boolean enabled = true;

    private OnClearListener listener;

    public ClearableAutoCompleteTextView(Context context) {
        super(context);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void setListener(OnClearListener listener) {
        this.listener = listener;
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout clearableAutoCompleteTextView = (LinearLayout) inflater.inflate(R.layout.clearable_autocompletetextview, this, false);
        addView(clearableAutoCompleteTextView);

        // ID alapjan kereses erdekes dolgokat eredmenyezetett a DoubleCheckBoxView-ban, ugyhogy itt is inkabb index alapjan keresunk
        text = (AutoCompleteTextView) clearableAutoCompleteTextView.getChildAt(0);
        icon = (ImageView) clearableAutoCompleteTextView.getChildAt(1);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!enabled) {
                    return;
                }
                text.setText("");
                if (listener != null) {
                    listener.onClear();
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        text.setEnabled(enabled);
        text.setClickable(enabled);
        text.setFocusable(enabled);
        icon.setClickable(enabled);
        icon.setFocusable(enabled);
    }

    public AutoCompleteTextView getAutoCompleteTextView() {
        return text;
    }

    public Editable getText() {
        return text.getText();
    }

    public void setText(String value) {
        text.setText(value);
    }

    public void setText(int resId) {
        text.setText(resId);
    }

    public void setError(CharSequence error) {
        text.setError(error);
    }

    public void setCompletionHint(CharSequence hint) {
        text.setCompletionHint(hint);
    }

    public CharSequence getCompletionHint() {
        return text.getCompletionHint();
    }

    public int getDropDownWidth() {
        return text.getDropDownWidth();
    }

    public void setDropDownWidth(int width) {
        text.setDropDownWidth(width);
    }

    public int getDropDownHeight() {
        return text.getDropDownHeight();
    }

    public void setDropDownHeight(int height) {
        text.setDropDownHeight(height);
    }

    public int getDropDownAnchor() {
        return text.getDropDownAnchor();
    }

    public void setDropDownAnchor(int id) {
        text.setDropDownAnchor(id);
    }

    public Drawable getDropDownBackground() {
        return text.getDropDownBackground();
    }

    public void setDropDownBackgroundDrawable(Drawable d) {
        text.setDropDownBackgroundDrawable(d);
    }

    public void setDropDownBackgroundResource(@DrawableRes int id) {
        text.setDropDownBackgroundResource(id);
    }

    public void setDropDownVerticalOffset(int offset) {
        text.setDropDownVerticalOffset(offset);
    }

    public int getDropDownVerticalOffset() {
        return text.getDropDownVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int offset) {
        text.setDropDownHorizontalOffset(offset);
    }

    public int getDropDownHorizontalOffset() {
        return text.getDropDownHorizontalOffset();
    }

    public int getThreshold() {
        return text.getThreshold();
    }

    public void setThreshold(int threshold) {
        text.setThreshold(threshold);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        text.setOnItemClickListener(l);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener l) {
        text.setOnItemSelectedListener(l);
    }

    @Deprecated
    public AdapterView.OnItemClickListener getItemClickListener() {
        return text.getItemClickListener();
    }

    @Deprecated
    public AdapterView.OnItemSelectedListener getItemSelectedListener() {
        return text.getItemSelectedListener();
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return text.getOnItemClickListener();
    }

    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return text.getOnItemSelectedListener();
    }

    public void setOnDismissListener(AutoCompleteTextView.OnDismissListener dismissListener) {
        text.setOnDismissListener(dismissListener);
    }

    public ListAdapter getAdapter() {
        return text.getAdapter();
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        text.setAdapter(adapter);
    }

    public boolean enoughToFilter() {
        return text.enoughToFilter();
    }

    public boolean isPopupShowing() {
        return text.isPopupShowing();
    }

    public void clearListSelection() {
        text.clearListSelection();
    }

    public void setListSelection(int position) {
        text.setListSelection(position);
    }

    public int getListSelection() {
        return text.getListSelection();
    }

    public void performCompletion() {
        text.performCompletion();
    }

    public void onCommitCompletion(CompletionInfo completion) {
        text.onCommitCompletion(completion);
    }

    public boolean isPerformingCompletion() {
        return text.isPerformingCompletion();
    }

    public void setText(CharSequence text, boolean filter) {
        this.text.setText(text, filter);
    }

    public void onFilterComplete(int count) {
        text.onFilterComplete(count);
    }

    public void dismissDropDown() {
        text.dismissDropDown();
    }

    public void showDropDown() {
        text.showDropDown();
    }

    public void setValidator(AutoCompleteTextView.Validator validator) {
        text.setValidator(validator);
    }

    public AutoCompleteTextView.Validator getValidator() {
        return text.getValidator();
    }

    public void performValidation() {
        text.performValidation();
    }

    public void setText(CharSequence text, TextView.BufferType type) {
        this.text.setText(text, type);
    }

    public void setSelection(int start, int stop) {
        text.setSelection(start, stop);
    }

    public void setSelection(int index) {
        text.setSelection(index);
    }

    public void selectAll() {
        text.selectAll();
    }

    public void extendSelection(int index) {
        text.extendSelection(index);
    }

    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        text.setEllipsize(ellipsis);
    }

    public void setTypeface(Typeface tf, int style) {
        text.setTypeface(tf, style);
    }

    public int length() {
        return text.length();
    }

    public Editable getEditableText() {
        return text.getEditableText();
    }

    public int getLineHeight() {
        return text.getLineHeight();
    }

    public Layout getLayout() {
        return text.getLayout();
    }

    public KeyListener getKeyListener() {
        return text.getKeyListener();
    }

    public void setKeyListener(KeyListener input) {
        text.setKeyListener(input);
    }

    public MovementMethod getMovementMethod() {
        return text.getMovementMethod();
    }

    public void setMovementMethod(MovementMethod movement) {
        text.setMovementMethod(movement);
    }

    public TransformationMethod getTransformationMethod() {
        return text.getTransformationMethod();
    }

    public void setTransformationMethod(TransformationMethod method) {
        text.setTransformationMethod(method);
    }

    public int getCompoundPaddingTop() {
        return text.getCompoundPaddingTop();
    }

    public int getCompoundPaddingBottom() {
        return text.getCompoundPaddingBottom();
    }

    public int getCompoundPaddingLeft() {
        return text.getCompoundPaddingLeft();
    }

    public int getCompoundPaddingRight() {
        return text.getCompoundPaddingRight();
    }

    public int getCompoundPaddingStart() {
        return text.getCompoundPaddingStart();
    }

    public int getCompoundPaddingEnd() {
        return text.getCompoundPaddingEnd();
    }

    public int getExtendedPaddingTop() {
        return text.getExtendedPaddingTop();
    }

    public int getExtendedPaddingBottom() {
        return text.getExtendedPaddingBottom();
    }

    public int getTotalPaddingLeft() {
        return text.getTotalPaddingLeft();
    }

    public int getTotalPaddingRight() {
        return text.getTotalPaddingRight();
    }

    public int getTotalPaddingStart() {
        return text.getTotalPaddingStart();
    }

    public int getTotalPaddingEnd() {
        return text.getTotalPaddingEnd();
    }

    public int getTotalPaddingTop() {
        return text.getTotalPaddingTop();
    }

    public int getTotalPaddingBottom() {
        return text.getTotalPaddingBottom();
    }

    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        text.setCompoundDrawables(left, top, right, bottom);
    }

    public void setCompoundDrawablesWithIntrinsicBounds(@DrawableRes int left, @DrawableRes int top, @DrawableRes int right, @DrawableRes int bottom) {
        text.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }

    public void setCompoundDrawablesWithIntrinsicBounds(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        text.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }

    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        text.setCompoundDrawablesRelative(start, top, end, bottom);
    }

    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@DrawableRes int start, @DrawableRes int top, @DrawableRes int end, @DrawableRes int bottom) {
        text.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }

    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        text.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }

    public Drawable[] getCompoundDrawables() {
        return text.getCompoundDrawables();
    }

    public Drawable[] getCompoundDrawablesRelative() {
        return text.getCompoundDrawablesRelative();
    }

    public void setCompoundDrawablePadding(int pad) {
        text.setCompoundDrawablePadding(pad);
    }

    public int getCompoundDrawablePadding() {
        return text.getCompoundDrawablePadding();
    }

    public int getAutoLinkMask() {
        return text.getAutoLinkMask();
    }

    public void setTextAppearance(Context context, @StyleRes int resid) {
        text.setTextAppearance(context, resid);
    }

    public Locale getTextLocale() {
        return text.getTextLocale();
    }

    public void setTextLocale(@NonNull Locale locale) {
        text.setTextLocale(locale);
    }

    @ViewDebug.ExportedProperty(
        category = "text"
    )
    public float getTextSize() {
        return text.getTextSize();
    }

    public void setTextSize(float size) {
        text.setTextSize(size);
    }

    public void setTextSize(int unit, float size) {
        text.setTextSize(unit, size);
    }

    public float getTextScaleX() {
        return text.getTextScaleX();
    }

    public void setTextScaleX(float size) {
        text.setTextScaleX(size);
    }

    public void setTypeface(Typeface tf) {
        text.setTypeface(tf);
    }

    public Typeface getTypeface() {
        return text.getTypeface();
    }

    public void setTextColor(@ColorInt int color) {
        text.setTextColor(color);
    }

    public void setTextColor(ColorStateList colors) {
        text.setTextColor(colors);
    }

    public ColorStateList getTextColors() {
        return text.getTextColors();
    }

    public int getCurrentTextColor() {
        return text.getCurrentTextColor();
    }

    public void setHighlightColor(@ColorInt int color) {
        text.setHighlightColor(color);
    }

    public int getHighlightColor() {
        return text.getHighlightColor();
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        text.setShadowLayer(radius, dx, dy, color);
    }

    public float getShadowRadius() {
        return text.getShadowRadius();
    }

    public float getShadowDx() {
        return text.getShadowDx();
    }

    public float getShadowDy() {
        return text.getShadowDy();
    }

    public int getShadowColor() {
        return text.getShadowColor();
    }

    public TextPaint getPaint() {
        return text.getPaint();
    }

    public void setAutoLinkMask(int mask) {
        text.setAutoLinkMask(mask);
    }

    public void setLinksClickable(boolean whether) {
        text.setLinksClickable(whether);
    }

    public boolean getLinksClickable() {
        return text.getLinksClickable();
    }

    public URLSpan[] getUrls() {
        return text.getUrls();
    }

    public void setHintTextColor(@ColorInt int color) {
        text.setHintTextColor(color);
    }

    public void setHintTextColor(ColorStateList colors) {
        text.setHintTextColor(colors);
    }

    public ColorStateList getHintTextColors() {
        return text.getHintTextColors();
    }

    public int getCurrentHintTextColor() {
        return text.getCurrentHintTextColor();
    }

    public void setLinkTextColor(@ColorInt int color) {
        text.setLinkTextColor(color);
    }

    public void setLinkTextColor(ColorStateList colors) {
        text.setLinkTextColor(colors);
    }

    public ColorStateList getLinkTextColors() {
        return text.getLinkTextColors();
    }

    public int getGravity() {
        return text.getGravity();
    }

    public int getPaintFlags() {
        return text.getPaintFlags();
    }

    public void setPaintFlags(int flags) {
        text.setPaintFlags(flags);
    }

    public void setHorizontallyScrolling(boolean whether) {
        text.setHorizontallyScrolling(whether);
    }

    public void setMinLines(int minlines) {
        text.setMinLines(minlines);
    }

    public int getMinLines() {
        return text.getMinLines();
    }

    public void setMinHeight(int minHeight) {
        text.setMinHeight(minHeight);
    }

    public int getMinHeight() {
        return text.getMinHeight();
    }

    public void setMaxLines(int maxlines) {
        text.setMaxLines(maxlines);
    }

    public int getMaxLines() {
        return text.getMaxLines();
    }

    public void setMaxHeight(int maxHeight) {
        text.setMaxHeight(maxHeight);
    }

    public int getMaxHeight() {
        return text.getMaxHeight();
    }

    public void setLines(int lines) {
        text.setLines(lines);
    }

    public void setHeight(int pixels) {
        text.setHeight(pixels);
    }

    public void setMinEms(int minems) {
        text.setMinEms(minems);
    }

    public int getMinEms() {
        return text.getMinEms();
    }

    public void setMinWidth(int minpixels) {
        text.setMinWidth(minpixels);
    }

    public int getMinWidth() {
        return text.getMinWidth();
    }

    public void setMaxEms(int maxems) {
        text.setMaxEms(maxems);
    }

    public int getMaxEms() {
        return text.getMaxEms();
    }

    public void setMaxWidth(int maxpixels) {
        text.setMaxWidth(maxpixels);
    }

    public int getMaxWidth() {
        return text.getMaxWidth();
    }

    public void setEms(int ems) {
        text.setEms(ems);
    }

    public void setWidth(int pixels) {
        text.setWidth(pixels);
    }

    public void setLineSpacing(float add, float mult) {
        text.setLineSpacing(add, mult);
    }

    public float getLineSpacingMultiplier() {
        return text.getLineSpacingMultiplier();
    }

    public float getLineSpacingExtra() {
        return text.getLineSpacingExtra();
    }

    public void append(CharSequence text) {
        this.text.append(text);
    }

    public void append(CharSequence text, int start, int end) {
        this.text.append(text, start, end);
    }

    public void setFreezesText(boolean freezesText) {
        text.setFreezesText(freezesText);
    }

    public boolean getFreezesText() {
        return text.getFreezesText();
    }

    public void setEditableFactory(Editable.Factory factory) {
        text.setEditableFactory(factory);
    }

    public void setSpannableFactory(Spannable.Factory factory) {
        text.setSpannableFactory(factory);
    }

    public void setText(CharSequence text) {
        this.text.setText(text);
    }

    public void setTextKeepState(CharSequence text) {
        this.text.setTextKeepState(text);
    }

    public void setText(char[] text, int start, int len) {
        this.text.setText(text, start, len);
    }

    public void setTextKeepState(CharSequence text, TextView.BufferType type) {
        this.text.setTextKeepState(text, type);
    }

    public void setText(@StringRes int resid, TextView.BufferType type) {
        text.setText(resid, type);
    }

    public void setHint(CharSequence hint) {
        text.setHint(hint);
    }

    public void setHint(@StringRes int resid) {
        text.setHint(resid);
    }

    @ViewDebug.CapturedViewProperty
    public CharSequence getHint() {
        return text.getHint();
    }

    public void setInputType(int type) {
        text.setInputType(type);
    }

    public void setRawInputType(int type) {
        text.setRawInputType(type);
    }

    public int getInputType() {
        return text.getInputType();
    }

    public void setImeOptions(int imeOptions) {
        text.setImeOptions(imeOptions);
    }

    public int getImeOptions() {
        return text.getImeOptions();
    }

    public void setImeActionLabel(CharSequence label, int actionId) {
        text.setImeActionLabel(label, actionId);
    }

    public CharSequence getImeActionLabel() {
        return text.getImeActionLabel();
    }

    public int getImeActionId() {
        return text.getImeActionId();
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener l) {
        text.setOnEditorActionListener(l);
    }

    public void onEditorAction(int actionCode) {
        text.onEditorAction(actionCode);
    }

    public void setPrivateImeOptions(String type) {
        text.setPrivateImeOptions(type);
    }

    public String getPrivateImeOptions() {
        return text.getPrivateImeOptions();
    }

    public void setInputExtras(@XmlRes int xmlResId) throws XmlPullParserException, IOException {
        text.setInputExtras(xmlResId);
    }

    public Bundle getInputExtras(boolean create) {
        return text.getInputExtras(create);
    }

    public CharSequence getError() {
        return text.getError();
    }

    public void setError(CharSequence error, Drawable icon) {
        text.setError(error, icon);
    }

    public void setFilters(InputFilter[] filters) {
        text.setFilters(filters);
    }

    public InputFilter[] getFilters() {
        return text.getFilters();
    }

    public boolean onPreDraw() {
        return text.onPreDraw();
    }

    public boolean isTextSelectable() {
        return text.isTextSelectable();
    }

    public void setTextIsSelectable(boolean selectable) {
        text.setTextIsSelectable(selectable);
    }

    public int getLineCount() {
        return text.getLineCount();
    }

    public int getLineBounds(int line, Rect bounds) {
        return text.getLineBounds(line, bounds);
    }

    public boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        return text.extractText(request, outText);
    }

    public void setExtractedText(ExtractedText text) {
        this.text.setExtractedText(text);
    }

    public void onCommitCorrection(CorrectionInfo info) {
        text.onCommitCorrection(info);
    }

    public void beginBatchEdit() {
        text.beginBatchEdit();
    }

    public void endBatchEdit() {
        text.endBatchEdit();
    }

    public void onBeginBatchEdit() {
        text.onBeginBatchEdit();
    }

    public void onEndBatchEdit() {
        text.onEndBatchEdit();
    }

    public boolean onPrivateIMECommand(String action, Bundle data) {
        return text.onPrivateIMECommand(action, data);
    }

    public void setIncludeFontPadding(boolean includepad) {
        text.setIncludeFontPadding(includepad);
    }

    public boolean getIncludeFontPadding() {
        return text.getIncludeFontPadding();
    }

    public boolean bringPointIntoView(int offset) {
        return text.bringPointIntoView(offset);
    }

    public boolean moveCursorToVisibleOffset() {
        return text.moveCursorToVisibleOffset();
    }

    @ViewDebug.ExportedProperty(
        category = "text"
    )
    public int getSelectionStart() {
        return text.getSelectionStart();
    }

    @ViewDebug.ExportedProperty(
        category = "text"
    )
    public int getSelectionEnd() {
        return text.getSelectionEnd();
    }

    public boolean hasSelection() {
        return text.hasSelection();
    }

    public void setSingleLine() {
        text.setSingleLine();
    }

    public void setAllCaps(boolean allCaps) {
        text.setAllCaps(allCaps);
    }

    public void setSingleLine(boolean singleLine) {
        text.setSingleLine(singleLine);
    }

    public void setMarqueeRepeatLimit(int marqueeLimit) {
        text.setMarqueeRepeatLimit(marqueeLimit);
    }

    public int getMarqueeRepeatLimit() {
        return text.getMarqueeRepeatLimit();
    }

    @ViewDebug.ExportedProperty
    public TextUtils.TruncateAt getEllipsize() {
        return text.getEllipsize();
    }

    public void setSelectAllOnFocus(boolean selectAllOnFocus) {
        text.setSelectAllOnFocus(selectAllOnFocus);
    }

    public void setCursorVisible(boolean visible) {
        text.setCursorVisible(visible);
    }

    public boolean isCursorVisible() {
        return text.isCursorVisible();
    }

    public void addTextChangedListener(TextWatcher watcher) {
        text.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        text.removeTextChangedListener(watcher);
    }

    public void clearComposingText() {
        text.clearComposingText();
    }

    public boolean didTouchFocusSelect() {
        return text.didTouchFocusSelect();
    }

    public void setScroller(Scroller s) {
        text.setScroller(s);
    }
//
//    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
//        return TextView.getTextColors(context, attrs);
//    }
//
//    public static int getTextColor(Context context, TypedArray attrs, int def) {
//        return TextView.getTextColor(context, attrs, def);
//    }

    public boolean isInputMethodTarget() {
        return text.isInputMethodTarget();
    }

    public boolean onTextContextMenuItem(int id) {
        return text.onTextContextMenuItem(id);
    }

    public boolean isSuggestionsEnabled() {
        return text.isSuggestionsEnabled();
    }

    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        text.setCustomSelectionActionModeCallback(actionModeCallback);
    }

    public ActionMode.Callback getCustomSelectionActionModeCallback() {
        return text.getCustomSelectionActionModeCallback();
    }

    public int getOffsetForPosition(float x, float y) {
        return text.getOffsetForPosition(x, y);
    }
}
