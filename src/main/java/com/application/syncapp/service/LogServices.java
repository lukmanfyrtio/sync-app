package com.application.syncapp.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import okhttp3.OkHttpClient;

import com.application.syncapp.response.BalanceResponse;

@Service
public class LogServices {

	private FileChannel channel;

	private RestTemplate restTemplate;

	@PostConstruct
	private void init() {
		HttpComponentsClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory();
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30, TimeUnit.SECONDS);
		builder.readTimeout(30, TimeUnit.SECONDS);
		builder.writeTimeout(30, TimeUnit.SECONDS);
		builder.build();
		reqFactory.setConnectTimeout(2000000);
		reqFactory.setReadTimeout(2000000);

		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(reqFactory);
	}

	@Async
	public void transferFunds(String to, String pkey, String contractAddress, BigInteger value, String urlRPC)
			throws Exception {
		//init gas price 10 gwei
		BigInteger gasPrice = Convert.toWei("10", Convert.Unit.GWEI).toBigInteger();
		System.out.println("gasPrice = " + gasPrice);

		//init rpc node
		Web3j web3j = Web3j.build(new HttpService(urlRPC));
		//init wallet with private key
		Credentials credentials = Credentials.create(pkey);
		System.out.println(credentials.getAddress());

		//init gas limit from last block
		BigInteger gasLimit = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock()
				.getGasLimit();
		StaticGasProvider gas = new StaticGasProvider(gasPrice, BigInteger.valueOf(96000));
		System.out.println("gasLimit = " + 96000);
        
		TransactionManager transactionManager = new RawTransactionManager(
				web3j, credentials, 97);
		//init java wrapper for token 
		ERC20 token = ERC20.load(contractAddress, web3j, transactionManager, gas);
		//push transfer to blockhain
		System.out.println(value);
		TransactionReceipt receipt = token.transfer(to, value).send();
		System.out.println("GAS USED=" + receipt.getGasUsed());
		System.out.println("txHash = "+receipt.getTransactionHash());
	}

	@Async
	public CompletableFuture<BigInteger> getBalanceBSC(String address, String rpcURL) {
		Web3j web3j = Web3j.build(new HttpService(rpcURL));
		EthGetBalance balanceRes;
		try {
			balanceRes = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).sendAsync().get();
			Thread.sleep(1000L);
			return CompletableFuture.completedFuture(balanceRes.getBalance());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		;
		return null;
	}

	@Async
	public CompletableFuture<BigInteger> getBalanceBSCToken(String address, String contractAddress, String nodeAddress,
			String pkey) {
		try {
			Web3j web3j = Web3j.build(new HttpService(nodeAddress));
			Credentials credentials = Credentials.create(pkey);
			ERC20 javaToken = ERC20.load(contractAddress, web3j, credentials, new DefaultGasProvider());
			Thread.sleep(1000L);
			// to check balance use below code
			return CompletableFuture.completedFuture(javaToken.balanceOf(credentials.getAddress()).send());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Async
	public CompletableFuture<BigInteger> getBalanceInfura(String address, String contractAddress, String nodeAddress) {
		try {
			Web3j web3j = Web3j.build(new HttpService(nodeAddress, createOkHttpClient(), false));
			Credentials credentials = Credentials
					.create("0xc679993bec3b678f180b84e953803d56bb68089a06ae342092e854a64b26ee41");
			ERC20 javaToken = ERC20.load(contractAddress, web3j, credentials, new DefaultGasProvider());
			Thread.sleep(1000L);
			// to check balance use below code
			return CompletableFuture.completedFuture(javaToken.balanceOf(address).send());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private OkHttpClient createOkHttpClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		configureTimeouts(builder);
		return builder.build();
	}

	private void configureTimeouts(OkHttpClient.Builder builder) {
		Long tos = 300L;
		if (tos != null) {
			builder.connectTimeout(tos, TimeUnit.SECONDS);
			builder.readTimeout(tos, TimeUnit.SECONDS); // Sets the socket timeout too
			builder.writeTimeout(tos, TimeUnit.SECONDS);
		}
	}

	@Async
	public CompletableFuture<BigInteger> getBalanceETHInfura(String address, String nodeAddress) {
		try {
			Web3j web3j = Web3j.build(new HttpService(nodeAddress, createOkHttpClient(), false));
			EthGetBalance balanceRes = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
			;
			Thread.sleep(1000L);
			// to check balance use below code
			return CompletableFuture.completedFuture(balanceRes.getBalance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String getAdressByPkey(String pkey) {
		Credentials credentials = Credentials.create(pkey);
		System.out.println(credentials.getAddress());
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
	public CompletableFuture<BalanceResponse> getBalance(String address, String apiKey, boolean pkey) {
		System.out.println("Execute method asynchronously Balance - "
				+ Thread.currentThread().getName());
		String newaddress = pkey ? getAdressByPkey(address) : address;
		try {
			BalanceResponse balanceRes = new BalanceResponse();
			CompletableFuture<String> eth = getBalance(newaddress, null, apiKey);
			CompletableFuture<String> bnb = getBalance(newaddress, "0xB8c77482e45F1F44dE1745F52C74426C631bDD52",
					apiKey);
			CompletableFuture<String> tusd = getBalance(newaddress, "0xdac17f958d2ee523a2206206994597c13d831ec7",
					apiKey);
			CompletableFuture.allOf(eth, bnb, tusd);
			// CompletableFuture<BigInteger> eth=getBalanceETH(address);
			// CompletableFuture<BigInteger> bnb=getBalance(address,
			// "0xB8c77482e45F1F44dE1745F52C74426C631bDD52");
			// CompletableFuture<BigInteger> tusd=getBalance(address,
			// "0xdac17f958d2ee523a2206206994597c13d831ec7");
			// balanceRes.setEth(String.valueOf(eth.get()));
			// balanceRes.setBnb(String.valueOf(bnb.get()));
			// balanceRes.setTusd(String.valueOf(tusd.get()));

			try {
				// CompletableFuture.allOf(eth,bnb,tusd).join();
				balanceRes.setEth(eth.get());
				balanceRes.setBnb(bnb.get());
				balanceRes.setTusd(tusd.get());
				balanceRes.setWallet(newaddress);
				String dataR = pkey
						? String.format(".key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getpKey(),
								balanceRes.getWallet(), balanceRes.getEthScale(), balanceRes.getBNBScale(),
								balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "")
						: String.format("wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getWallet(),
								balanceRes.getEthScale(), balanceRes.getBNBScale(), balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "");
				ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
				System.out.println("eth=" + balanceRes.getEth() + ", bnb=" + balanceRes.getBnb() + ", tusd="
						+ balanceRes.getTusd());
				channel.write(buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pkey) {
				balanceRes.setpKey(address);
			}
			Thread.sleep(2000L);
			return CompletableFuture.completedFuture(balanceRes);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Line 60 ");
			e.printStackTrace();
		}
		return null;

	}

	@Async
	public CompletableFuture<BalanceResponse> getBalanceInfura(String address, boolean pkey, String infuraAddress) {
		System.out.println("Execute method asynchronously Balance - "
				+ Thread.currentThread().getName());
		String newaddress = pkey ? getAdressByPkey(address) : address;
		try {
			BalanceResponse balanceRes = new BalanceResponse();

			try {
				CompletableFuture<BigInteger> eth = getBalanceETHInfura(address, infuraAddress);
				CompletableFuture<BigInteger> bnb = getBalanceInfura(address,
						"0xB8c77482e45F1F44dE1745F52C74426C631bDD52", infuraAddress);
				CompletableFuture<BigInteger> tusd = getBalanceInfura(address,
						"0xdac17f958d2ee523a2206206994597c13d831ec7", infuraAddress);
				CompletableFuture.allOf(eth, bnb, tusd);
				balanceRes.setEth(String.valueOf(eth.get()));
				balanceRes.setBnb(String.valueOf(bnb.get()));
				balanceRes.setTusd(String.valueOf(tusd.get()));
				balanceRes.setWallet(newaddress);
				String dataR = pkey
						? String.format(".key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getpKey(),
								balanceRes.getWallet(), balanceRes.getEthScale(), balanceRes.getBNBScale(),
								balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "")
						: String.format("wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getWallet(),
								balanceRes.getEthScale(), balanceRes.getBNBScale(), balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "");
				ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
				System.out.println("eth=" + balanceRes.getEth() + ", bnb=" + balanceRes.getBnb() + ", tusd="
						+ balanceRes.getTusd());
				channel.write(buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pkey) {
				balanceRes.setpKey(address);
			}
			return CompletableFuture.completedFuture(balanceRes);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Line 60 ");
			e.printStackTrace();
		}
		return null;

	}


	@Async
	public CompletableFuture<BalanceResponse> getBalanceBSC(String address, boolean pkey, String infuraAddress) {
		System.out.println("Execute method asynchronously Balance - "
				+ Thread.currentThread().getName());
		String newaddress = pkey ? getAdressByPkey(address) : address;
		try {
			BalanceResponse balanceRes = new BalanceResponse();

			try {
				CompletableFuture<BigInteger> bnb = getBalanceETHInfura(newaddress, infuraAddress);
				CompletableFuture<BigInteger> eth = getBalanceInfura(newaddress,
						"0x2170ed0880ac9a755fd29b2688956bd959f933f8", infuraAddress);
				CompletableFuture<BigInteger> tusd = getBalanceInfura(newaddress,
						"0x55d398326f99059ff775485246999027b3197955", infuraAddress);
				CompletableFuture.allOf(eth, bnb, tusd);
				balanceRes.setEth(String.valueOf(eth.get()));
				balanceRes.setBnb(String.valueOf(bnb.get()));
				balanceRes.setTusd(String.valueOf(tusd.get()));
				if (pkey) {
					balanceRes.setpKey(address);
				}
				balanceRes.setWallet(newaddress);
				String dataR = pkey
						? String.format("key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getpKey(),
								balanceRes.getWallet(), balanceRes.getEthScale(), balanceRes.getBNBScale(),
								balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "")
						: String.format("wallet = %s ETH = %s BNB = %s TUSD = %s %s\n", balanceRes.getWallet(),
								balanceRes.getEthScale(), balanceRes.getBNBScale(), balanceRes.getTUSDScale(),
								!balanceRes.getBnb().equals("0") || !balanceRes.getTusd().equals("0")
										|| !balanceRes.getEth().equals("0") ? "found" : "");
				ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
				System.out.println("eth=" + balanceRes.getEth() + ", bnb=" + balanceRes.getBnb() + ", tusd="
						+ balanceRes.getTusd());
				channel.write(buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return CompletableFuture.completedFuture(balanceRes);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Line 60 ");
			e.printStackTrace();
		}
		return null;

	}

	@Async
	public void readPkey(List<String> lists) {
		String file = "/Users/lukmanfyrtio/Documents/data/private_key" + new Date().getTime() + ".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw");
				FileChannel channel = writer.getChannel()) {
			long before = System.currentTimeMillis();
			AtomicInteger increment = new AtomicInteger(0);
			lists.stream().forEach(data -> {
				try {
					String addressWallet = getAdressByPkey(data);
					String dataR = String.format("%s\n", addressWallet);
					ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));

					channel.write(buff);
					System.out.println(increment.getAndIncrement() + " key = " + data + " , wallet =" + dataR);
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

	// @Async
	// public void readLogv2(List<String> lists) {
	// List<BalanceResponse> list = new ArrayList<>();
	// String addrescheck = "data";
	// AtomicInteger apiKeyArray = new AtomicInteger(0);
	// AtomicInteger a = new AtomicInteger(0);
	// String apiKey = apiKeys.get(apiKeyArray.get());
	// lists.stream().forEach(strLine -> {
	// try {
	// addrescheck.replace(addrescheck, strLine);
	// BalanceResponse balanceRes = new BalanceResponse();
	// Future<String> eth = getBalance(strLine, null, apiKey);
	// Future<String> bnb = getBalance(strLine,
	// "0xB8c77482e45F1F44dE1745F52C74426C631bDD52",
	// apiKey);
	// Future<String> tusd = getBalance(strLine,
	// "0xdac17f958d2ee523a2206206994597c13d831ec7",
	// apiKey);
	// if (eth.isDone() && bnb.isDone() && tusd.isDone()) {
	// balanceRes.setEth(eth.get());
	// balanceRes.setBnb(bnb.get());
	// balanceRes.setTusd(tusd.get());
	// balanceRes.setWallet(strLine);
	// /* parse strLine to obtain what you want */
	// System.out.println(Thread.currentThread().getName() + " " + a + " Wallet => "
	// + strLine
	// + " || ETH => " + balanceRes.getEth() + " BNB => " + balanceRes.getBnb() + "
	// TUSD => "
	// + balanceRes.getTusd());
	// list.add(balanceRes);
	// }
	// a.getAndIncrement();
	// if (a.get() == 10000) {
	// DataExcelExporter excelExporter = new DataExcelExporter(list);
	// excelExporter.export();
	// a.set(0);
	// apiKeyArray.getAndIncrement();
	// list.clear();
	// if (apiKeyArray.get() == 4) {
	// apiKeyArray.set(0);
	// }
	// ;
	// }
	// } catch (Exception e) {
	// BalanceResponse balanceRes = new BalanceResponse();
	// balanceRes.setEth("Failed to check address");
	// balanceRes.setBnb("Failed to check address");
	// balanceRes.setTusd("Failed to check address");
	// balanceRes.setWallet(strLine);
	//
	// list.add(balanceRes);
	// System.out.println("Failed to check address => " + addrescheck);
	// System.out.println(e.getMessage());
	// }
	// });
	// System.out.println("log finish");
	// }

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

				// hit again if max limit reached
				if (body.getResult().contains("Max rate limit reached")) {
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
			System.out.println("Line 213 Failed to check address => " + address);
			System.out.println("Line 214 " + e.getResponseBodyAsString());
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
