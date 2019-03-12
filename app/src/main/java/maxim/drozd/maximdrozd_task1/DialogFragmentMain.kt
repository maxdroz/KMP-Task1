package maxim.drozd.maximdrozd_task1

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.widget.EditText
import maxim.drozd.maximdrozd_task1.db.AppDatabase
import maxim.drozd.maximdrozd_task1.db.DesktopAppInfo
import maxim.drozd.maximdrozd_task1.db.Position
import maxim.drozd.maximdrozd_task1.launcher.DesktopFragment
import maxim.drozd.maximdrozd_task1.launcher.LauncherFragmentPageAdaptor
import android.provider.ContactsContract
import android.content.Intent



class DialogFragmentMain : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.icon_type))
                .setItems(R.array.icons){ _: DialogInterface, pos: Int ->
            when(pos){
                0 -> {
                    val builder2 = AlertDialog.Builder(context!!)
                    val cont = context!!
                    val notif = fragmentManager!!.findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(activity!!.findViewById<ViewPager>(R.id.launcher_view_pager).id, 2)) as DesktopFragment
                    val mainPart = layoutInflater.inflate(R.layout.url_request_popout, null)
                    builder2.setTitle("Setup URL")
                            .setView(mainPart)
                            .setPositiveButton(getString(R.string.ok)){ _: DialogInterface, _: Int ->
                                var name = mainPart.findViewById<EditText>(R.id.url_name).text.toString().trim()
                                var url = mainPart.findViewById<EditText>(R.id.url_url).text.toString().trim()
                                if(url == "")
                                    return@setPositiveButton
                                if(name == "")
                                    name = url
                                if(!url.contains("www."))
                                    url = "www.$url"
                                if(!url.contains("https://"))
                                    url = "https://$url"
                                Thread(Runnable {
                                    val h = arguments!!.getInt("h")
                                    val w = arguments!!.getInt("w")
                                    AppDatabase.getInstance(cont).desktopAppInfo().insertAll(DesktopAppInfo(Position(h, w), 0, url, name))
                                    notif.update()
                                }).start()
                            }
                            .setNegativeButton(getString(R.string.cancel)){ _: DialogInterface, _: Int ->
                            }
                    builder2.show()
                }
                else -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    activity?.startActivityForResult(intent, 100)
                }
            }
        }
        return builder.create()
    }


    companion object {
        fun create(h: Int, w: Int): DialogFragmentMain{
            val dfm = DialogFragmentMain()
            val args = Bundle()
            args.putInt("h", h)
            args.putInt("w", w)
            dfm.arguments = args
            return dfm
        }
    }
}