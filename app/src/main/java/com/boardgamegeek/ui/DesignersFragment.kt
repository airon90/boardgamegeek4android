package com.boardgamegeek.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.boardgamegeek.R
import com.boardgamegeek.entities.PersonEntity
import com.boardgamegeek.extensions.*
import com.boardgamegeek.ui.adapter.AutoUpdatableAdapter
import com.boardgamegeek.ui.viewmodel.DesignsViewModel
import com.boardgamegeek.ui.widget.RecyclerSectionItemDecoration
import kotlinx.android.synthetic.main.fragment_designers.*
import kotlinx.android.synthetic.main.include_horizontal_progress.*
import kotlinx.android.synthetic.main.row_designer.view.*
import kotlin.properties.Delegates

class DesignersFragment : Fragment() {
    private val viewModel: DesignsViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(DesignsViewModel::class.java)
    }

    private val adapter: DesignersAdapter by lazy {
        DesignersAdapter(viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_designers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(RecyclerSectionItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recycler_section_header_height),
                adapter))

        viewModel.designers.observe(this, Observer {
            showData(it)
            progressBar.hide()
        })

        viewModel.progress.observe(this, Observer {
            if (it == null) {
                progressContainer.isVisible = false
            } else {
                progressContainer.isVisible = it.second > 0
                progressView.max = it.second
                progressView.progress = it.first
            }
        })
    }

    private fun showData(designers: List<PersonEntity>) {
        adapter.designers = designers
        if (adapter.itemCount == 0) {
            recyclerView.fadeOut()
            emptyTextView.fadeIn()
        } else {
            recyclerView.fadeIn()
            emptyTextView.fadeOut()
        }
    }

    companion object {
        fun newInstance(): DesignersFragment {
            return DesignersFragment()
        }
    }

    class DesignersAdapter(private val viewModel: DesignsViewModel) : RecyclerView.Adapter<DesignersAdapter.DesignerViewHolder>(), AutoUpdatableAdapter, RecyclerSectionItemDecoration.SectionCallback {
        var designers: List<PersonEntity> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
            autoNotify(oldValue, newValue) { old, new ->
                old.id == new.id
            }
        }

        init {
            setHasStableIds(true)
        }

        override fun getItemCount() = designers.size

        override fun getItemId(position: Int) = designers.getOrNull(position)?.id?.toLong() ?: RecyclerView.NO_ID

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesignerViewHolder {
            return DesignerViewHolder(parent.inflate(R.layout.row_designer))
        }

        override fun onBindViewHolder(holder: DesignerViewHolder, position: Int) {
            holder.bind(designers.getOrNull(position))
        }

        override fun isSection(position: Int): Boolean {
            if (position == RecyclerView.NO_POSITION) return false
            if (designers.isEmpty()) return false
            if (position == 0) return true
            val thisLetter = viewModel.getSectionHeader(designers.getOrNull(position))
            val lastLetter = viewModel.getSectionHeader(designers.getOrNull(position - 1))
            return thisLetter != lastLetter
        }

        override fun getSectionHeader(position: Int): CharSequence {
            return when {
                position == RecyclerView.NO_POSITION -> "-"
                designers.isEmpty() -> "-"
                else -> viewModel.getSectionHeader(designers.getOrNull(position))
            }
        }

        inner class DesignerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(designer: PersonEntity?) {
                designer?.let { d ->
                    itemView.avatarView.loadThumbnailInList(d.thumbnailUrl, R.drawable.person_image_empty)
                    itemView.nameView.text = d.name
                    val showWhitmoreScore = itemView.context.isStatusSetToSync(COLLECTION_STATUS_RATED)
                    if (showWhitmoreScore) {
                        itemView.whitmoreScoreView.text = itemView.context.getString(R.string.whitmore_score).plus(" ${d.whitmoreScore}")
                    } else {
                        itemView.countView.text = itemView.context.resources.getQuantityString(R.plurals.games_suffix, d.itemCount, d.itemCount)
                    }
                    itemView.countView.isVisible = !showWhitmoreScore
                    itemView.whitmoreScoreView.isVisible = showWhitmoreScore
                    itemView.setOnClickListener {
                        PersonActivity.startForDesigner(itemView.context, d.id, d.name)
                    }
                }
            }
        }
    }
}