package maxim.drozd.maximdrozd_task1.launcher

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class LauncherFragmentPageAdaptor(fm: FragmentManager?) : FragmentPagerAdapter(fm){
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> ListLayoutFragment()
            1 -> GridLayoutFragment()
            else -> DesktopFragment()
        }
    }

    fun makeFragmentName(viewPagerId: Int, index: Int): String {
        return "android:switcher:$viewPagerId:$index"
    }

    override fun getCount(): Int = 3
}