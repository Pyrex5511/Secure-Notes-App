// GridSpacingItemDecoration.kt

package com.example.securenotesapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // index položky v adaptéri
        val column = position % spanCount // stĺpec (0, 1)

        if (includeEdge) {
            // Medzera pred prvou položkou a po poslednej položke (okraje)
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // horná hrana
                outRect.top = spacing
            }
            outRect.bottom = spacing // medzera medzi riadkami (riadkovanie)
        } else {
            // Medzera medzi stĺpcami
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing // medzera medzi riadkami
            }
        }
    }
}