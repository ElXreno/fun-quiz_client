package com.github.elxreno.funquiz_client.ui

import android.widget.ExpandableListView

class NonScrollExpandableListView : ExpandableListView {
    constructor(context: android.content.Context?) : super(context)
    constructor(context: android.content.Context?, attrs: android.util.AttributeSet?) : super(
        context,
        attrs
    )

    constructor(
        context: android.content.Context?,
        attrs: android.util.AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpecCustom =
            MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpecCustom)
        val params = layoutParams
        params.height = measuredHeight
    }
}