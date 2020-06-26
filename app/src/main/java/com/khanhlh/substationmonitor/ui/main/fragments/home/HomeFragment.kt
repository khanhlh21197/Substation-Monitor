package com.khanhlh.substationmonitor.ui.main.fragments.home


import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentHomeBinding
import com.khanhlh.substationmonitor.extensions.dpToPx
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.model.Device
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment<FragmentHomeBinding>(), ItemClickPresenter<Device> {
    private lateinit var vm: HomeViewModel

    private val mAdapter by lazy {
        SingleTypeAdapter<Device>(mContext, R.layout.home_row, vm.list)
    }

    override fun initView() {
        vm = HomeViewModel()
        mBinding.viewModel = vm
        vm.getAllUsers()

        initRecycler()
    }

    private fun initRecycler() {
        recycler.apply {
            adapter = mAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.top = activity?.dpToPx(R.dimen.xdp_12_0) ?: 0
                }
            })
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_home
    override fun onItemClick(v: View?, item: Device) {
        logD(item.id)
    }

}
