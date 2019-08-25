package com.moneytransfer.dao;

import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.UserTransaction;

public interface TransferDAO {
	
	 int transferFund(UserTransaction transaction);
	 Account getAccountById(long accountId) throws CustomException;;
}
