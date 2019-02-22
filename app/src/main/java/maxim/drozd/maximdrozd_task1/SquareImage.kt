package maxim.drozd.maximdrozd_task1

import android.content.Context
import android.util.AttributeSet
import android.view.View

class SquareImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : android.support.v7.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = drawable
        if (d != null) {
            val w = View.MeasureSpec.getSize(widthMeasureSpec)
            val h = w * d.intrinsicHeight / d.intrinsicWidth
            setMeasuredDimension(w, h)
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}