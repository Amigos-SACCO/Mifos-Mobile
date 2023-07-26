package org.mifos.mobile.ui.views

import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.ui.views.base.MVPView

/**
 * @author Vishwajeet
 * @since 18/8/16.
 */
interface SavingAccountsDetailView : MVPView {
    /**
     * Should be called when saving account object can successfully accessed
     * from the server to display saving account details on the screen.
     *
     * @param savingsWithAssociations object containing details of each saving account,
     * received from server.
     */
    fun showSavingAccountsDetail(savingsWithAssociations: SavingsWithAssociations?)

    /**
     * Should be called if there is any error from client side in getting
     * saving account object from server.
     *
     * Reason for error should be mentioned clearly to the user.
     *
     * @param message Error message to display showing reason of failure in getting
     * saving account object
     */
    fun showErrorFetchingSavingAccountsDetail(message: String?)
    fun showAccountStatus(savingsWithAssociations: SavingsWithAssociations?)
}
