package com.application.syncapp.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import com.application.syncapp.response.BalanceResponse;

@Service
public class LogServices {
	
	private FileChannel channel;
	
	private RestTemplate restTemplate;
	@PostConstruct
	private void init() {
		HttpComponentsClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory();
		reqFactory.setConnectTimeout(200000);
		reqFactory.setReadTimeout(200000);

		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(reqFactory);

	}
	
	
	public void transferFunds(String to,String pkey,String contractAddress,BigInteger value) {
		BigInteger gasPrice=Convert.toWei("1", Convert.Unit.WEI).toBigInteger();
		Web3j web3j=Web3j.build(new HttpService("https://mainnet.infura.io/v3/c08196122bf14df79f1b9cb63d7984b4"));
		Credentials credentials=Credentials.create(pkey);
		StaticGasProvider gas=new StaticGasProvider(gasPrice, BigInteger.valueOf(21000));
		ERC20 token=ERC20.load(contractAddress, web3j, credentials, gas);
        TransactionReceipt receipt;
		try {
//			receipt = token.transferFrom(to, "0xF01413046858033fbDC816B81DCef1E055ee8E42", value);
//			System.out.println(receipt.getTransactionHash());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Async
	public CompletableFuture<BigInteger> getBalance(String address,String contractAddress) {
		try {
			 Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/c08196122bf14df79f1b9cb63d7984b4"));
		Credentials credentials=Credentials.create("0xc679993bec3b678f180b84e953803d56bb68089a06ae342092e854a64b26ee41");
		ERC20 javaToken=ERC20.load(contractAddress, web3j, credentials, new DefaultGasProvider());
		//to check balance use below code
			 return javaToken.balanceOf(address).sendAsync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Async
	public CompletableFuture<BigInteger> getBalanceETH(String address) {
		try {
			 Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/c08196122bf14df79f1b9cb63d7984b4"));
			 
			EthGetBalance balanceRes= web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).sendAsync().get();
		//to check balance use below code
			 return CompletableFuture.completedFuture(balanceRes.getBalance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	private String getAdressByPkey(String pkey) {
		Credentials credentials=Credentials.create(pkey);
		return credentials.getAddress();
	}
	List<String> apiKeys = Arrays.asList("6ZRPHAIJAZKT94NF5WW46K5NAAU57HSMMF", "I6W3DJCECRIZGH1148E1IRRC8PNKYYM839",
			"E84IYMIB9BRV4B9PRC47R77C4RDE5IJEQF", "DR688TYGCRUGJE483ESKVWB12PPYI85F74",
			"ZRDCEDCCMWFWTQNV1ATJHB18SFV1YCAPN4");

	
	public int getRandomNumberUsingInts(int min, int max) {
	    Random random = new Random();
	    return random.ints(min, max)
	      .findFirst()
	      .getAsInt();
	}
	
	@Async
	public CompletableFuture<BalanceResponse> getBalance(String address,String apiKey,boolean pkey) {
	    System.out.println("Execute method asynchronously Balance - " 
	      + Thread.currentThread().getName());
	    String newaddress=pkey?getAdressByPkey(address):address;
		try {
		    BalanceResponse balanceRes = new BalanceResponse();
//		    CompletableFuture<String> eth = getBalance(newaddress, null, apiKey);
//		    CompletableFuture<String> bnb = getBalance(newaddress, "0xB8c77482e45F1F44dE1745F52C74426C631bDD52",
//					apiKey);
//		    CompletableFuture<String> tusd = getBalance(newaddress, "0xdac17f958d2ee523a2206206994597c13d831ec7",
//					apiKey);
		    CompletableFuture<BigInteger> eth=getBalanceETH(address);
		    CompletableFuture<BigInteger> bnb=getBalance(address, "0xB8c77482e45F1F44dE1745F52C74426C631bDD52");
		    CompletableFuture<BigInteger> tusd=getBalance(address, "0xdac17f958d2ee523a2206206994597c13d831ec7");
			balanceRes.setEth(String.valueOf(eth.get()));
			balanceRes.setBnb(String.valueOf(bnb.get()));
			balanceRes.setTusd(String.valueOf(tusd.get()));
			balanceRes.setWallet(newaddress);
			String dataR=pkey?String.format(".key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n" ,balanceRes.getpKey(),balanceRes.getWallet(),balanceRes.getEthScale(),balanceRes.getBNBScale(),balanceRes.getTUSDScale(),!balanceRes.getBnb().equals("0")||!balanceRes.getTusd().equals("0")||!balanceRes.getEth().equals("0")?"found":""):String.format("wallet = %s ETH = %s BNB = %s TUSD = %s %s\n" ,balanceRes.getWallet(),balanceRes.getEthScale(),balanceRes.getBNBScale(),balanceRes.getTUSDScale(),!balanceRes.getBnb().equals("0")||!balanceRes.getTusd().equals("0")||!balanceRes.getEth().equals("0")?"found":"");
	        ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
			try {
				channel.write(buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("eth="+balanceRes.getEth()+", bnb="+balanceRes.getBnb()+", tusd="+balanceRes.getTusd());
			if(pkey) {
				balanceRes.setpKey(address);
			}
			Thread.sleep(4000L);
			return CompletableFuture.completedFuture(balanceRes);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Line 60 ");
			e.printStackTrace();
		}
		return null;

	}
	
	
	@Async
	public void readPkey(List<String> lists) {
		String file = "/Users/lukmanfyrtio/Documents/data/private_key"+new Date().getTime()+".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw");
		        FileChannel channel = writer.getChannel()){
		long before = System.currentTimeMillis();
			    AtomicInteger increment=new AtomicInteger(0);
			    lists.stream().forEach(data->{
			    	try {
			    		String addressWallet=getAdressByPkey(data);
	    				String dataR=String.format("%s\n",addressWallet);
	    		        ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
	    				 
	    		        channel.write(buff);
						System.out.println(increment.getAndIncrement()+" key = "+data+" , wallet ="+dataR);
					} catch (Exception e2) {
						System.out.println(e2.getMessage());
						System.out.println("Line 85");
					}
			    });

			    long after = System.currentTimeMillis();
			    System.out.println("time to execute: " + (after - before) / 1000.0 + " seconds.");
		} catch (Exception e2) {
			// TODO: handle exception
		}
	}
	
	
	


//	@Async
//	public void readLogv2(List<String> lists) {
//		List<BalanceResponse> list = new ArrayList<>();
//		String addrescheck = "data";
//		AtomicInteger apiKeyArray = new AtomicInteger(0);
//		AtomicInteger a = new AtomicInteger(0);
//		String apiKey = apiKeys.get(apiKeyArray.get());
//		lists.stream().forEach(strLine -> {
//			try {
//				addrescheck.replace(addrescheck, strLine);
//				BalanceResponse balanceRes = new BalanceResponse();
//				Future<String> eth = getBalance(strLine, null, apiKey);
//				Future<String> bnb = getBalance(strLine, "0xB8c77482e45F1F44dE1745F52C74426C631bDD52",
//						apiKey);
//				Future<String> tusd = getBalance(strLine, "0xdac17f958d2ee523a2206206994597c13d831ec7",
//						apiKey);
//				if (eth.isDone() && bnb.isDone() && tusd.isDone()) {
//					balanceRes.setEth(eth.get());
//					balanceRes.setBnb(bnb.get());
//					balanceRes.setTusd(tusd.get());
//					balanceRes.setWallet(strLine);
//					/* parse strLine to obtain what you want */
//					System.out.println(Thread.currentThread().getName() + " " + a + " Wallet => " + strLine
//							+ " || ETH => " + balanceRes.getEth() + " BNB => " + balanceRes.getBnb() + " TUSD => "
//							+ balanceRes.getTusd());
//					list.add(balanceRes);
//				}
//				a.getAndIncrement();
//				if (a.get() == 10000) {
//					DataExcelExporter excelExporter = new DataExcelExporter(list);
//					excelExporter.export();
//					a.set(0);
//					apiKeyArray.getAndIncrement();
//					list.clear();
//					if (apiKeyArray.get() == 4) {
//						apiKeyArray.set(0);
//					}
//					;
//				}
//			} catch (Exception e) {
//				BalanceResponse balanceRes = new BalanceResponse();
//				balanceRes.setEth("Failed to check address");
//				balanceRes.setBnb("Failed to check address");
//				balanceRes.setTusd("Failed to check address");
//				balanceRes.setWallet(strLine);
//
//				list.add(balanceRes);
//				System.out.println("Failed to check address => " + addrescheck);
//				System.out.println(e.getMessage());
//			}
//		});
//		System.out.println("log finish");
//	}

	@Async
	public CompletableFuture<String> getBalance(String address, String contractAddres, String token)
			throws InterruptedException {
		Map<String, String> pathVariable = new HashMap<>();
		pathVariable.put("address", address);
		pathVariable.put("token", token);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
				"https://api.etherscan.io/api?module=account&action=balance&address={address}&tag=latest&apikey={token}");
		if (contractAddres != null) {
			pathVariable.put("contractAddres", contractAddres);
			builder = UriComponentsBuilder.fromUriString(
					"https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress={contractAddres}&apikey={token}&address={address}&tag=latest");
		}
		try {

			ResponseEntity<BalanceResponse> json = restTemplate.exchange(builder.buildAndExpand(pathVariable).toUri(),
					HttpMethod.GET, null, BalanceResponse.class);
			if (json.getStatusCodeValue() == 200) {
				BalanceResponse body = json.getBody();
				body.setWallet(address);
				
				//hit again if max limit reached
				if(body.getResult().contains("Max rate limit reached")) {
					json = restTemplate.exchange(builder.buildAndExpand(pathVariable).toUri(),
							HttpMethod.GET, null, BalanceResponse.class);
					if (json.getStatusCodeValue() == 200) {
						body = json.getBody();
						body.setWallet(address);
					return CompletableFuture.completedFuture(body.getResult());
					}
				}
				Thread.sleep(3000L);
				return CompletableFuture.completedFuture(body.getResult());
			}
		} catch (HttpStatusCodeException e) {
			System.out.println("Line 213 Failed to check address => " + address );
			System.out.println("Line 214 "+e.getResponseBodyAsString());
			return null;
		}
		return null;
	}


	public FileChannel getChannel() {
		return channel;
	}


	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}
}
