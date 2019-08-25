package com.moneytransfer.dao;

import java.math.BigDecimal;
import java.util.List;

import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.UserTransaction;


public interface BankAccountDAO {

    Account getAccountById(long accountId) throws CustomException;
    long createAccount(Account account) throws CustomException;

    /**
     * @param accountId user accountId
     * @param deltaAmount amount to be debit(less than 0)/credit(greater than 0).
     * @return no. of rows updated
     */
    int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws CustomException;
    int transferAccountBalance(UserTransaction userTransaction) throws CustomException;
    List<Account> getAllAccounts() throws CustomException;

}
