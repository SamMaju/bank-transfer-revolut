package com.taskforce.moneyapp.dao;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.moneytransfer.dao.BankAccountDAO;
import com.moneytransfer.dao.BankDAOFactory;
import com.moneytransfer.dao.TransferDAO;
import com.moneytransfer.dao.TransferReentrantDAO;
import com.moneytransfer.dao.impl.TransferReentrantDAOImpl;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.Account;
import com.moneytransfer.model.UserTransaction;

public class TestTransferReentrantDAO {

	private static Logger log = Logger.getLogger(TestAccountDAO.class);
	private static final BankDAOFactory h2DaoFactory = BankDAOFactory.getDAOFactory(BankDAOFactory.H2);
	private static final int THREADS_COUNT = 100;

	@BeforeClass
	public static void setup() {
		// prepare test database and test data, Test data are initialised from
		// src/test/resources/demo.sql
		h2DaoFactory.populateTestData();
	}

	@After
	public void tearDown() {

	}
/*
	@Test
	public void testAccountSingleThreadSameCcyTransfer() throws CustomException {

		final TransferReentrantDAO transferDAO=h2DaoFactory.getReentrantDAO();
		BigDecimal transferAmount = new BigDecimal(50.01234).setScale(4, RoundingMode.HALF_EVEN);

		UserTransaction transaction = new UserTransaction("EUR", transferAmount, 3L, 4L);

		long startTime = System.currentTimeMillis();

		transferDAO.transferReentrantFund(transaction);
		long endTime = System.currentTimeMillis();

		log.info("TransferAccountBalance finished, time taken: " + (endTime - startTime) + "ms");

		Account accountFrom = transferDAO.getAccountById(3);

		Account accountTo = transferDAO.getAccountById(4);

		log.debug("Account From: " + accountFrom);

		log.debug("Account From: " + accountTo);

		assertTrue(
				accountFrom.getBalance().compareTo(new BigDecimal(449.9877).setScale(4, RoundingMode.HALF_EVEN)) == 0);
		assertTrue(accountTo.getBalance().equals(new BigDecimal(550.0123).setScale(4, RoundingMode.HALF_EVEN)));

	}*/

	@Test
	public void testAccountMultiThreadedTransfer() throws InterruptedException, CustomException {
		//final TransferReentrantDAO transferDAO=h2DaoFactory.getReentrantDAO();
		// transfer a total of 200USD from 100USD balance in multi-threaded
		// mode, expect half of the transaction fail
		final TransferReentrantDAOImpl transferDAO= new TransferReentrantDAOImpl();
		ExecutorService executorService= Executors.newFixedThreadPool(3);	
		
		Runnable a = new Runnable(){
			public void run()
			{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			UserTransaction transaction = new UserTransaction("USD",
					new BigDecimal(2), 1L, 2L);
			try {
				System.out.println("Transfer initialize.....");
				transferDAO.transferReentrantFund(transaction);
			} catch (CustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() +" says :: Transfer successfull");
			}
			};
		
			Runnable b = new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					UserTransaction transaction = new UserTransaction("USD",
							new BigDecimal(2), 2L, 1L);
					try {
						transferDAO.transferReentrantFund(transaction);
					} catch (CustomException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(Thread.currentThread().getName() + " says :: Transfer successfull");
				}
			};
		
			for(int i=0;i<100;i++)
			{
				System.out.println(Thread.currentThread().getName() +" says :: Transfer initialising");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				executorService.submit(a);
				executorService.submit(b);
			}
		
		
		
		
		
		
		
		
		
		
		
		
		
	/*	final CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
		for (int i = 0; i < THREADS_COUNT; i++) {
			new Thread(new Runnable() {

				public void run() {
					try {
						UserTransaction transaction = new UserTransaction("USD",
								new BigDecimal(2).setScale(4, RoundingMode.HALF_EVEN), 1L, 2L);
						//accountDAO.transferAccountBalance(transaction);
						transferDAO.transferReentrantFund(transaction);
					} catch (Exception e) {
						log.error("Error occurred during transfer ", e);
					} finally {
						latch.countDown();
					}
				}
			}).start();
		}

		latch.await();

		Account accountFrom = transferDAO.getAccountById(1);

		Account accountTo = transferDAO.getAccountById(2);

		log.debug("Account From: " + accountFrom);

		log.debug("Account From: " + accountTo);

		assertTrue(accountFrom.getBalance().equals(new BigDecimal(0).setScale(4, RoundingMode.HALF_EVEN)));
		assertTrue(accountTo.getBalance().equals(new BigDecimal(300).setScale(4, RoundingMode.HALF_EVEN)));*/

	}
	
/*
	@Test
	public void testTransferFailOnDBLock() throws CustomException, SQLException {
		final String SQL_LOCK_ACC = "SELECT * FROM Account WHERE AccountId = 5 FOR UPDATE";
		Connection conn = null;
		PreparedStatement lockStmt = null;
		ResultSet rs = null;
		Account fromAccount = null;

		try {
			conn = H2DAOFactory.getConnection();
			conn.setAutoCommit(false);
			// lock account for writing:
			lockStmt = conn.prepareStatement(SQL_LOCK_ACC);
			rs = lockStmt.executeQuery();
			if (rs.next()) {
				fromAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
						rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
				if (log.isDebugEnabled())
					log.debug("Locked Account: " + fromAccount);
			}

			if (fromAccount == null) {
				throw new CustomException("Locking error during test, SQL = " + SQL_LOCK_ACC);
			}
			// after lock account 5, try to transfer from account 6 to 5
			// default h2 timeout for acquire lock is 1sec
			BigDecimal transferAmount = new BigDecimal(50).setScale(4, RoundingMode.HALF_EVEN);

			UserTransaction transaction = new UserTransaction("GBP", transferAmount, 6L, 5L);
			h2DaoFactory.getTransferDAO().transferFund(transaction);
			conn.commit();
		} catch (Exception e) {
			log.error("Exception occurred, initiate a rollback");
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				log.error("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
		}

		// now inspect account 3 and 4 to verify no transaction occurred
		BigDecimal originalBalance = new BigDecimal(500).setScale(4, RoundingMode.HALF_EVEN);
		assertTrue(h2DaoFactory.getAccountDAO().getAccountById(6).getBalance().equals(originalBalance));
		assertTrue(h2DaoFactory.getAccountDAO().getAccountById(5).getBalance().equals(originalBalance));
	}*/

}
