package com.github.elxreno.funquiz_client.ui.quiz.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.repository.UserRepository
import com.github.elxreno.funquiz_client.databinding.FragmentQuizDashboardBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuizDashboardFragment : Fragment() {

    private lateinit var pagerAdapter: PagerAdapter

    private lateinit var binding: FragmentQuizDashboardBinding

    private var snackbarShown = false

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        fun newInstance() = QuizDashboardFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuizDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lateinit var pagerAdapter: PagerAdapter

        userRepository.userRoles.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                return@observe

            if (::pagerAdapter.isInitialized) {
                binding.viewPager.adapter = null
            }
            pagerAdapter = PagerAdapter(childFragmentManager, requireContext())
            pagerAdapter.addRoles(it)
            binding.viewPager.adapter = pagerAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }

        userRepository.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && !snackbarShown) {
                Snackbar.make(
                    view,
                    getString(R.string.welcome).format(user.displayName),
                    Snackbar.LENGTH_LONG
                ).show()
                userRepository.currentUser.removeObservers(viewLifecycleOwner)
                snackbarShown = true
            }
        }
    }

    class PagerAdapter(
        fm: FragmentManager,
        private val context: Context
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var fragmentList: List<Pair<Fragment, Int>> = listOf()

        private fun initFragments(roles: List<String>) {
            fragmentList = listOf()
            if (roles.contains("Guest")) {
                fragmentList =
                    fragmentList + (QuizzesFragment.newInstance(
                        showOnlyPublic = true,
                        canPassTest = false
                    ) to R.string.quiz_title_public)
            } else {
                if (roles.contains("Admin")) {
                    fragmentList =
                        fragmentList + (QuizzesFragment.newInstance(
                            adminMode = true,
                            showOnlyPublic = false,
                            moderatorMode = true
                        ) to R.string.quiz_title_my_own)
                }
                if (roles.contains("Moderator")) {
                    fragmentList =
                        fragmentList + (QuizzesFragment.newInstance(
                            showOnlyPublic = true,
                            moderatorMode = true,
                            canPassTest = false
                        ) to R.string.quiz_title_moderation)
                }
                if (roles.contains("User")) {
                    fragmentList =
                        fragmentList + (QuizzesFragment.newInstance(
                            showOnlyPublic = true
                        ) to R.string.quiz_title_public)
                }
            }
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position].first
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return context.getString(fragmentList[position].second)
        }

        private fun destroy() {
            fragmentList.forEach {
                it.first.onDestroy()
            }
        }

        fun addRoles(roles: List<String>) {
            destroy()
            initFragments(roles)
            notifyDataSetChanged()
        }
    }

}