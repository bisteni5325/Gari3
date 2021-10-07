import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.telematics.reward.databinding.FragmentObdFeatureHostBinding

class OBDFeatureHost : Fragment() {

    companion object {
        fun createFragment(toStreaks: Boolean) = OBDFeatureHost()
    }

    private lateinit var binding: FragmentObdFeatureHostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentObdFeatureHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}