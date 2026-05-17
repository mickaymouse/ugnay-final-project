package ugnay.app.frontend.residents.ui.request

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ugnay.app.R
import ugnay.app.databinding.FragmentResidentsRequestBinding

class RequestFragment : Fragment() {

    private var _binding: FragmentResidentsRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCardClicks()
        addCardAnimations()
    }

    private fun openForm(type: String) {
        val intent = Intent(requireContext(), RequestFormActivity::class.java)
        intent.putExtra("REQUEST_TYPE", type)
        startActivity(intent)
    }

    private fun setupCardClicks() {

        val grid = binding.glRequests

        for (i in 0 until grid.childCount) {

            val child = grid.getChildAt(i)

            if (child is CardView) {

                child.setOnClickListener {

                    val type = when (i) {
                        0 -> "INDIGENCY"
                        1 -> "RESIDENCY"
                        2 -> "BRGY_ID"
                        3 -> "CLEARANCE"
                        else -> "OTHERS"
                    }

                    openForm(type)
                }
            }
        }
    }

    private fun addCardAnimations() {

        val grid = binding.glRequests

        for (i in 0 until grid.childCount) {

            val child = grid.getChildAt(i)

            if (child is CardView) {

                child.alpha = 0f
                child.translationY = 30f

                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 80L)
                    .setDuration(300)
                    .start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}