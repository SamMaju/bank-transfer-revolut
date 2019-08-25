package com.moneytransfer.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.moneytransfer.dao.H2DAOFactory;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.MoneyUtil;

public class BankAccountImpl {
	private final static String SQL_GET_ACC_BY_ID = "SELECT * FROM Account WHERE AccountId = ? ";

	private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";
	private static Logger log = Logger.getLogger(BankAccountImpl.class);
	
	
	
	public Account getAccountById(long accountId) throws CustomException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Account acc = null;
		try {
			conn = H2DAOFactory.getConnection();
			stmt = conn.prepareStatement(SQL_GET_ACC_BY_ID);
			stmt.setLong(1, accountId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				acc = new Account(rs.getLong("AccountId"), rs.getString("UserName"), rs.getBigDecimal("Balance"),
						rs.getString("CurrencyCode"));
				if (log.isDebugEnabled())
					log.debug("Retrieve Account By Id: " + acc);
			}
			return acc;
		} catch (SQLException e) {
			throw new CustomException("getAccountById(): Error reading account data", e);
		} finally {
			DbUtils.closeQuietly(conn, stmt, rs);
		}

	}

	
	
	public synchronized int transfer(BigDecimal money, Long sourceAccountNumber,
			Long destinationAccountNumber, String currencyCode) throws CustomException {
		
		
        System.out.println(Thread.currentThread().getName() + " transferring");
        int result = -1;
		Connection conn = null;
		PreparedStatement lockStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		Account fromAccount = null;
		Account toAccount = null;
		
		
		try {
			conn = H2DAOFactory.getConnection();
			conn.setAutoCommit(false);
			// lock the credit and debit account for writing:
			lockStmt = conn.prepareStatement(SQL_GET_ACC_BY_ID);
			lockStmt.setLong(1, sourceAccountNumber);
			rs = lockStmt.executeQuery();
			if (rs.next()) {
				fromAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
						rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
				if (log.isDebugEnabled())
					log.debug("transferAccountBalance from Account: " + fromAccount);
			}
			lockStmt = conn.prepareStatement(SQL_GET_ACC_BY_ID);
			lockStmt.setLong(1, destinationAccountNumber);
			rs = lockStmt.executeQuery();
			if (rs.next()) {
				toAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"), rs.getBigDecimal("Balance"),
						rs.getString("CurrencyCode"));
				if (log.isDebugEnabled())
					log.debug("transferAccountBalance to Account: " + toAccount);
			}

			// check locking status
			if (fromAccount == null || toAccount == null) {
				throw new CustomException("Fail to lock both accounts for write");
			}

			// check transaction currency
			if (!fromAccount.getCurrencyCode().equals(currencyCode)) {
				throw new CustomException(
						"Fail to transfer Fund, transaction ccy are different from source/destination");
			}

			// check ccy is the same for both accounts
			if (!fromAccount.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
				throw new CustomException(
						"Fail to transfer Fund, the source and destination account are in different currency");
			}

			// check enough fund in source account 
			BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(money);
			if (fromAccountLeftOver.compareTo(MoneyUtil.zeroAmount) < 0) {
				throw new CustomException("Not enough Fund from source Account ");
			}
	
			boolean debit = debit(money, fromAccount);
            if(debit) { 
            		credit(money,toAccount);
             }
			// proceed with update
			updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
			updateStmt.setBigDecimal(1, fromAccount.getBalance());
			updateStmt.setLong(2, sourceAccountNumber);
			updateStmt.addBatch();
			updateStmt.setLong(2, destinationAccountNumber);
			updateStmt.addBatch();
			updateStmt.executeBatch();
			updateStmt.executeBatch();
			result = 2;
			// If there is no error, commit the transaction
			conn.commit();
		} catch (SQLException se) {
			log.error("transferAccountBalance(): User Transaction Failed, rollback initiated for: " + fromAccount,
					se);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				throw new CustomException("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
			DbUtils.closeQuietly(updateStmt);
		}
		
		
		return result;
		
            
    }
	
    public static boolean debit(BigDecimal money, Account fromAccount ) throws CustomException {
    	BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(money);
		if (fromAccountLeftOver.compareTo(MoneyUtil.zeroAmount) < 0) {
			throw new CustomException("Not enough Fund from source Account ");
		} else {
			try {
				//Extra processing time!!
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
		}
    }

public void credit(BigDecimal money, Account toAccount) {

	toAccount.getBalance().add(money);
}


}
