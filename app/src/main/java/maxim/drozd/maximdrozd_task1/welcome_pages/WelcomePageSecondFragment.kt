package maxim.drozd.maximdrozd_task1.welcome_pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_welcome_page2.*
import maxim.drozd.maximdrozd_task1.R

class WelcomePageSecondFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button2.setOnClickListener {
            val pager = activity?.findViewById<ViewPager>(R.id.welcome_view_pager)
            pager?.setCurrentItem(pager.currentItem + 1, true)
        }
    }
}