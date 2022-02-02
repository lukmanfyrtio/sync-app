package com.application.syncapp.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponse {
	
	private String wallet;
	private String status;
	private String message;
	private String result;
	private String eth;
	private String bnb;
	private String tusd;
	private String pKey;
	
	public BalanceResponse() {
		
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getWallet() {
		return wallet;
	}
	public void setWallet(String wallet) {
		this.wallet = wallet;
	}
	public String getEth() {
		return eth;
	}
	public void setEth(String eth) {
		this.eth = eth;
	}
	public String getBnb() {
		return bnb;
	}
	public void setBnb(String bnb) {
		this.bnb = bnb;
	}
	public String getTusd() {
		return tusd;
	}
	public void setTusd(String tusd) {
		this.tusd = tusd;
	}

	public String getpKey() {
		return pKey;
	}

	public void setpKey(String pKey) {
		this.pKey = pKey;
	}
	
	public BigDecimal getEthScale() {
		return eth.equals("0")?new BigDecimal(0):new BigDecimal(eth).divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);
	}
	
	public BigDecimal getBNBScale() {
		return bnb.equals("0")?new BigDecimal(0): new BigDecimal(bnb).divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);
	}
	
	public BigDecimal getTUSDScale() {
		return tusd.equals("0")?new BigDecimal(0): new BigDecimal(tusd).divide(new BigDecimal(1000000L), 2, RoundingMode.HALF_UP);
	}
		
	
	
	
	
	
	
	
}
