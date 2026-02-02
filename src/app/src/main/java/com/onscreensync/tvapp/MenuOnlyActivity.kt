package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.ActivityMenuOnlyBinding
import com.onscreensync.tvapp.datamodels.MenuItemDataModel
import com.onscreensync.tvapp.datamodels.MenuMetadata
import com.onscreensync.tvapp.fragments.BasicMenuFragment
import com.onscreensync.tvapp.fragments.PremiumMenuFragment
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.ObjectExtensions
import com.onscreensync.tvapp.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MenuOnlyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuOnlyBinding

    @Inject
    lateinit var signalRManager: SignalRManager

    private var menuItems: Array<MenuItemDataModel>? = null
    private var menuMetadata: MenuMetadata? = null
    private var textFont: String? = null
    private var textColor: String? = null
    private var backgroundColor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuOnlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the Intent that started this activity
        menuMetadata = intent.getParcelableExtra("menuMetadata")
        textColor = intent.getStringExtra("textColor")
        textFont = intent.getStringExtra("textFont")
        backgroundColor = intent.getStringExtra("backgroundColor")

        backgroundColor?.let {
            if (it.isNotEmpty()) {
                window.decorView.setBackgroundColor(UiHelper.parseColor(it))
            }
        }

        menuItems = ObjectExtensions.getParcelableArrayExtra(intent, "menuItems", MenuItemDataModel::class.java)

        menuMetadata?.subType?.let { subType ->
            when (subType) {
                "Premium" -> loadMenuFragment(PremiumMenuFragment.newInstance())
                "Deluxe", "Basic" -> loadMenuFragment(BasicMenuFragment.newInstance())
                else -> loadMenuFragment(BasicMenuFragment.newInstance())
            }
        } ?: loadMenuFragment(BasicMenuFragment.newInstance())
    }

    override fun onBackPressed() {
        signalRManager.disconnect()

        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }

    private fun loadMenuFragment(fragment: Fragment) {
        val metadata = menuMetadata ?: return
        val items = menuItems ?: return

        val bundle = Bundle().apply {
            putParcelable("menuMetadata", metadata)
            putParcelableArray("menuItems", items)
            putString("textColor", textColor)
            putString("textFont", textFont)
            putString("backgroundColor", backgroundColor)
            putBoolean("setTransparentBackground", true)
        }
        
        loadFragment(fragment, bundle, R.id.menu_only_activity_menu_relativeLayout)
    }

    private fun loadFragment(fragment: Fragment, bundle: Bundle, elementId: Int) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(elementId, fragment)
            .addToBackStack(null)
            .commit()
    }
}
