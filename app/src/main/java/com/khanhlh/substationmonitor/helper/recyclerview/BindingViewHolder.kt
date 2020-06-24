package com.khanhlh.substationmonitor.helper.recyclerview

import androidx.databinding.ViewDataBinding

class BindingViewHolder<out T : ViewDataBinding>(val binding: T) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)