package cz.ojohn.locationtracker.screen.sms

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_sms.*
import javax.inject.Inject

/**
 * SMS configuration screen fragment
 */
class SmsFragment : Fragment() {

    companion object {
        fun newInstance(): SmsFragment = SmsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: SmsViewModel
    private lateinit var disposables: CompositeDisposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        App.instance.appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SmsViewModel::class.java)
        disposables = CompositeDisposable()
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onResume() {
        super.onResume()
        disposables.add(viewModel.observeExampleMessage()
                .subscribe { txtExample.text = it })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
