package com.moneytransfer.dao;

import com.moneytransfer.dao.impl.TransferDAOImpl;

public abstract class BankDAOFactory {

	public static final int H2 = 1;

	public abstract UserDAO getUserDAO();

	public abstract BankAccountDAO getAccountDAO();

	public abstract void populateTestData();

	public abstract TransferDAO getTransferDAO();
	public abstract TransferReentrantDAO getReentrantDAO();
	
	public static BankDAOFactory getDAOFactory(int factoryCode) {

		switch (factoryCode) {
		case H2:
			return new H2DAOFactory();
		default:
			// by default using H2 in memory database
			return new H2DAOFactory();
		}
	}

}
