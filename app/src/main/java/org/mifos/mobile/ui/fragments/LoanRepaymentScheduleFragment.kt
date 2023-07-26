package org.mifos.mobile.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import org.mifos.mobile.R
import org.mifos.mobile.databinding.FragmentLoanRepaymentScheduleBinding
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.models.accounts.loan.Periods
import org.mifos.mobile.models.accounts.loan.tableview.Cell
import org.mifos.mobile.models.accounts.loan.tableview.ColumnHeader
import org.mifos.mobile.models.accounts.loan.tableview.RowHeader
import org.mifos.mobile.presenters.LoanRepaymentSchedulePresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.LoanRepaymentScheduleAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.LoanRepaymentScheduleMvpView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.Network
import javax.inject.Inject

/**
 * Created by Rajan Maurya on 03/03/17.
 */
class LoanRepaymentScheduleFragment : BaseFragment(), LoanRepaymentScheduleMvpView {

    private var _binding: FragmentLoanRepaymentScheduleBinding? = null
    private val binding get() = _binding!!

    @JvmField
    @Inject
    var loanRepaymentSchedulePresenter: LoanRepaymentSchedulePresenter? = null

    @JvmField
    @Inject
    var loanRepaymentScheduleAdapter: LoanRepaymentScheduleAdapter? = null
    var sweetUIErrorHandler: SweetUIErrorHandler? = null
    private var loanId: Long? = 0
    private var loanWithAssociations: LoanWithAssociations? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        setToolbarTitle(getString(R.string.loan_repayment_schedule))
        if (arguments != null) loanId = arguments?.getLong(Constants.LOAN_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoanRepaymentScheduleBinding.inflate(inflater, container, false)
        loanRepaymentSchedulePresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(context, binding.root)
        showUserInterface()
        if (savedInstanceState == null) {
            loanRepaymentSchedulePresenter?.loanLoanWithAssociations(loanId)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutError.btnTryAgain.setOnClickListener {
            retryClicked()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showLoanRepaymentSchedule(savedInstanceState.getParcelable<Parcelable>(Constants.LOAN_ACCOUNT) as LoanWithAssociations)
        }
    }

    /**
     * Initializes the layout
     */
    override fun showUserInterface() {
        val columnWidth: Double
        binding.tvRepaymentSchedule.setHasFixedWidth(true)
        val orientation = resources.configuration.orientation
        binding.tvRepaymentSchedule.layoutParams?.width = resources.displayMetrics.widthPixels
        binding.tvRepaymentSchedule.setAdapter(loanRepaymentScheduleAdapter)
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnWidth = 2 * (resources.displayMetrics.widthPixels / 7.2)
        } else {
            columnWidth = 2 * (resources.displayMetrics.widthPixels / 6.6)
        }
        loanRepaymentScheduleAdapter?.setColumnWidth(columnWidth)
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    /**
     * Fetches [LoanWithAssociations] for a loan with `loanId`
     *
     * @param loanWithAssociations Contains details about Repayment Schedule
     */
    override fun showLoanRepaymentSchedule(loanWithAssociations: LoanWithAssociations?) {
        this.loanWithAssociations = loanWithAssociations
        var currencyRepresentation = loanWithAssociations?.currency?.displaySymbol
        loanRepaymentScheduleAdapter
            ?.setCurrency(currencyRepresentation)
        setTableViewList(loanWithAssociations?.repaymentSchedule?.periods)
        binding.tvAccountNumber.text = loanWithAssociations?.accountNo
        binding.tvDisbursementDate.text =
            DateHelper.getDateAsString(loanWithAssociations?.timeline?.expectedDisbursementDate)
        binding.tvNumberOfPayments.text = loanWithAssociations?.numberOfRepayments.toString()
    }

    private fun setTableViewList(periods: List<Periods>?) {
        val mColumnHeaderList: MutableList<ColumnHeader?> = ArrayList()
        val mRowHeaders: MutableList<RowHeader?> = ArrayList()
        val mCellList: MutableList<List<Cell?>> = ArrayList()
        mColumnHeaderList.add(ColumnHeader(getString(R.string.date)))
        mColumnHeaderList.add(ColumnHeader(getString(R.string.loan_balance)))
        mColumnHeaderList.add(ColumnHeader(getString(R.string.repayment)))
        if (periods != null) {
            for ((i, period) in periods.withIndex()) {
                val cells: MutableList<Cell> = ArrayList()
                cells.add(Cell(period))
                cells.add(Cell(period))
                cells.add(Cell(period))
                mCellList.add(cells)
                mRowHeaders.add(RowHeader(i + 1))
            }
        }
        loanRepaymentScheduleAdapter?.setAllItems(mColumnHeaderList, mRowHeaders, mCellList)
    }

    /**
     * Shows an empty layout for a loan with `loanId` which has no Repayment Schedule
     *
     * @param loanWithAssociations Contains details about Repayment Schedule
     */
    override fun showEmptyRepaymentsSchedule(loanWithAssociations: LoanWithAssociations?) {
        binding.tvAccountNumber.text = loanWithAssociations?.accountNo
        binding.tvDisbursementDate.text =
            DateHelper.getDateAsString(loanWithAssociations?.timeline?.expectedDisbursementDate)
        binding.tvNumberOfPayments.text = loanWithAssociations?.numberOfRepayments.toString()
        sweetUIErrorHandler?.showSweetEmptyUI(
            getString(R.string.repayment_schedule),
            R.drawable.ic_charges,
            binding.tvRepaymentSchedule,
            binding.layoutError.root,
        )
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showError(message: String?) {
        if (!Network.isConnected(activity)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(
                binding.tvRepaymentSchedule,
                binding.layoutError.root,
            )
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(
                message,
                binding.tvRepaymentSchedule,
                binding.layoutError.root,
            )
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun retryClicked() {
        if (Network.isConnected(context)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(
                binding.tvRepaymentSchedule,
                binding.layoutError.root,
            )
            loanRepaymentSchedulePresenter?.loanLoanWithAssociations(loanId)
        } else {
            Toast.makeText(
                context,
                getString(R.string.internet_not_connected),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressBar()
        loanRepaymentSchedulePresenter?.detachView()
        _binding = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        showUserInterface()
    }

    companion object {
        fun newInstance(loanId: Long?): LoanRepaymentScheduleFragment {
            val loanRepaymentScheduleFragment = LoanRepaymentScheduleFragment()
            val args = Bundle()
            if (loanId != null) args.putLong(Constants.LOAN_ID, loanId)
            loanRepaymentScheduleFragment.arguments = args
            return loanRepaymentScheduleFragment
        }
    }
}
