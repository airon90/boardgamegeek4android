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
import com.boardgamegeek.entities.CompanyEntity
import com.boardgamegeek.extensions.*
import com.boardgamegeek.ui.adapter.AutoUpdatableAdapter
import com.boardgamegeek.ui.viewmodel.PublishersViewModel
import com.boardgamegeek.ui.widget.RecyclerSectionItemDecoration
import kotlinx.android.synthetic.main.fragment_publishers.*
import kotlinx.android.synthetic.main.include_horizontal_progress.*
import kotlinx.android.synthetic.main.row_publisher.view.*
import kotlin.properties.Delegates

class PublishersFragment : Fragment() {
    private val viewModel: PublishersViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(PublishersViewModel::class.java)
    }

    private val adapter: PublisherAdapter by lazy {
        PublisherAdapter(viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_publishers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(RecyclerSectionItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recycler_section_header_height),
                adapter))

        viewModel.publishers.observe(this, Observer {
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

    private fun showData(publishers: List<CompanyEntity>) {
        adapter.publishers = publishers
        if (adapter.itemCount == 0) {
            recyclerView.fadeOut()
            emptyTextView.fadeIn()
        } else {
            recyclerView.fadeIn()
            emptyTextView.fadeOut()
        }
    }

    companion object {
        fun newInstance(): PublishersFragment {
            return PublishersFragment()
        }
    }

    class PublisherAdapter(private val viewModel: PublishersViewModel) : RecyclerView.Adapter<PublisherAdapter.PublisherViewHolder>(), AutoUpdatableAdapter, RecyclerSectionItemDecoration.SectionCallback {
        var publishers: List<CompanyEntity> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
            autoNotify(oldValue, newValue) { old, new ->
                old.id == new.id
            }
        }

        init {
            setHasStableIds(true)
        }

        override fun getItemCount() = publishers.size

        override fun getItemId(position: Int) = publishers.getOrNull(position)?.id?.toLong() ?: RecyclerView.NO_ID

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublisherViewHolder {
            return PublisherViewHolder(parent.inflate(R.layout.row_publisher))
        }

        override fun onBindViewHolder(holder: PublisherViewHolder, position: Int) {
            holder.bind(publishers.getOrNull(position))
        }

        override fun isSection(position: Int): Boolean {
            if (position == RecyclerView.NO_POSITION) return false
            if (publishers.isEmpty()) return false
            if (position == 0) return true
            val thisLetter = viewModel.getSectionHeader(publishers.getOrNull(position))
            val lastLetter = viewModel.getSectionHeader(publishers.getOrNull(position - 1))
            return thisLetter != lastLetter
        }

        override fun getSectionHeader(position: Int): CharSequence {
            return when {
                position == RecyclerView.NO_POSITION -> "-"
                publishers.isEmpty() -> "-"
                else -> viewModel.getSectionHeader(publishers.getOrNull(position))
            }
        }

        inner class PublisherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(publisher: CompanyEntity?) {
                publisher?.let { p ->
                    itemView.thumbnailView.loadThumbnailInList(p.thumbnailUrl)
                    itemView.nameView.text = p.name
                    val showWhitmoreScore = itemView.context.isStatusSetToSync(COLLECTION_STATUS_RATED)
                    if (showWhitmoreScore) {
                        itemView.whitmoreScoreView.text = itemView.context.getString(R.string.whitmore_score).plus(" ${p.whitmoreScore}")
                    } else {
                        itemView.countView.text = itemView.context.resources.getQuantityString(R.plurals.games_suffix, p.itemCount, p.itemCount)
                    }
                    itemView.countView.isVisible = !showWhitmoreScore
                    itemView.whitmoreScoreView.isVisible = showWhitmoreScore
                    itemView.setOnClickListener {
                        PersonActivity.startForPublisher(itemView.context, p.id, p.name)
                    }
                }
            }
        }
    }
}