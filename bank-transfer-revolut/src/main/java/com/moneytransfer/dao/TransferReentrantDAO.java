package com.moneytransfer.dao;

import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.UserTransaction;

public interface TransferReentrantDAO {
	 int transferReentrantFund(UserTransaction transaction) throws CustomException;
	 Account getAccountById(long accountId) throws CustomException;;

}
