package com.moneytransfer.service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.moneytransfer.dao.BankDAOFactory;
import com.moneytransfer.exception.CustomException;
import com.moneytransfer.model.MoneyUtil;
import com.moneytransfer.model.UserTransaction;

@Path("/transferFundReentrant")
@Produces(MediaType.APPLICATION_JSON)
public class TransferReentrantService {

	private final BankDAOFactory daoFactory = BankDAOFactory.getDAOFactory(BankDAOFactory.H2);
	
	@POST
	public Response fundTransfer(UserTransaction transaction) throws CustomException {

		String currency = transaction.getCurrencyCode();
		if (MoneyUtil.INSTANCE.validateCcyCode(currency)) {
			int updateCount = daoFactory.getReentrantDAO().transferReentrantFund(transaction);	
			if (updateCount == 2) {
				return Response.status(Response.Status.OK).build();
			} else {
				// transaction failed
				throw new WebApplicationException("Transaction failed", Response.Status.BAD_REQUEST);
			}

		} else {
			throw new WebApplicationException("Currency Code Invalid ", Response.Status.BAD_REQUEST);
		}

	}

}
