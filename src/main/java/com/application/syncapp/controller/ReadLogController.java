package com.application.syncapp.controller;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.syncapp.response.BalanceResponse;
import com.application.syncapp.service.LogServices;

@RestController
public class ReadLogController {

	@Autowired
	private LogServices logService;

	@PostMapping("/pkey")
	private String getAddres(@RequestParam(required = true) String pathFile,
			@RequestParam(required = false, defaultValue = "0") Integer from_line,
			@RequestParam(required = false, defaultValue = "10000") Integer to_line) throws IOException {
		System.out.println("started read log" + new Date());
		List<String> result = new ArrayList<String>();
		try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
			// Convert a Stream to List
			result = lines.filter(x -> x != null || x != "").collect(Collectors.toList());
		}

		logService.readPkey(result);
		System.out.println("end" + new Date());
		return "read the log started";
	}
	
	
	@GetMapping("/balance")
	public String getBalance(@RequestParam(required = true) String address,@RequestParam(required = false) String rpcURL) throws InterruptedException, ExecutionException {
		CompletableFuture<BigInteger> balance=logService.getBalanceBSC(address,rpcURL);
		return String.valueOf(balance.get());
	}

	@GetMapping("/transfer")
	public String transferFund(@RequestParam(required = true) String to,@RequestParam(required = true) String rpcURL,@RequestParam(required = true) String contract,@RequestParam(required = true) String pkey) throws Exception {
		BigInteger balance= logService.getBalanceBSCToken(to, contract, rpcURL, pkey).get();
		System.out.println(balance);
		logService.transferFunds(to, pkey, contract, balance,rpcURL);
		return String.valueOf("Transfer Success");
	}

	@PostMapping("/check/log/file")
	@Async
	public String checkreadTheLogFile(@RequestParam(required = true) String pathFile,
					@RequestParam(required = true) String pathComparedFile) throws IOException {
			System.out.println("started read log" + new Date());
			List<String> result = new ArrayList<String>();

			try (Stream<String> lines = Files.lines(Paths.get(pathComparedFile))) {
					// Convert a Stream to List
					result.clear();
					result.addAll(lines.collect(Collectors.toList()));
			}

			try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
					// Convert a Stream to List
								String file = "C:/Users/Lukman Fyrtio/Documents/Others/newlist" + new Date().getTime() + ".txt";
			try (RandomAccessFile writer = new RandomAccessFile(file, "rw"); FileChannel channel = writer.getChannel()) {
					lines.filter(x -> !x.isEmpty()).forEach(data -> {
							String line = data.split("=")[1].split(" ")[1].replaceAll("\\s", "");
							System.out.println(result.contains(line)+" "+data);
							if (result.contains(line)) {
								try {
									String data1=data+"\n";
									channel.write(ByteBuffer.wrap(data1.getBytes(StandardCharsets.UTF_8)));
							} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
							}
							}
					});
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}

			return "";
		}

	@PostMapping("/log/etherscan")
	@Async
	public String readTheLog(@RequestParam(required = true) String pathFile,
			@RequestParam(required = false, defaultValue = "0") Integer from_line,
			@RequestParam(required = false, defaultValue = "10000") Integer to_line,
			@RequestParam(defaultValue = "false") boolean pkey, @RequestParam(defaultValue = "false") boolean all)
			throws IOException {
		System.out.println("started read log" + new Date());
		List<String> result = new ArrayList<String>();
		try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
			// Convert a Stream to List
			result = lines.filter(x -> !x.isEmpty()).collect(Collectors.toList());
		}

		readLines(all ? result : result.subList(from_line, to_line), pkey);
		System.out.println("end" + new Date());
		return "read the log started";
	}


	@PostMapping("/log/infura")
	@Async
	public String readTheLogv2(@RequestParam(required = true) String pathFile,
			@RequestParam(required = true) String node_address,
			@RequestParam(required = false, defaultValue = "0") Integer from_line,
			@RequestParam(required = false, defaultValue = "10000") Integer to_line,
			@RequestParam(defaultValue = "false") boolean pkey, @RequestParam(defaultValue = "false") boolean all)
			throws IOException {
		System.out.println("started read log" + new Date());
		List<String> result = new ArrayList<String>();
		try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
			// Convert a Stream to List
			result = lines.filter(x -> !x.isEmpty()).collect(Collectors.toList());
		}

		readLinesInfura(all ? result : result.subList(from_line, to_line), pkey,node_address);
		System.out.println("end" + new Date());
		return "read the log started";
	}

	@PostMapping("/log/bsc")
	@Async
	public String readTheLogBsc(@RequestParam(required = true) String pathFile,
			@RequestParam(required = true) String node_address,
			@RequestParam(required = false, defaultValue = "0") Integer from_line,
			@RequestParam(required = false, defaultValue = "10000") Integer to_line,
			@RequestParam(defaultValue = "false") boolean pkey, @RequestParam(defaultValue = "false") boolean all)
			throws IOException {
		System.out.println("started read log" + new Date());
		List<String> result = new ArrayList<String>();
		try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
			// Convert a Stream to List
			result = lines.filter(x -> !x.isEmpty()).collect(Collectors.toList());
		}
        Path path = Paths.get(pathFile);
  
        // call getFileName() and get FileName path object
        Path fileName = path.getFileName();
		readLinesBSC(all ? result : result.subList(from_line, to_line), pkey,node_address,fileName.toString());
		System.out.println("end" + new Date());
		return "read the log started";
	}
	
	@PostMapping("/check/log")
	@Async
	public String checkreadTheLog(@RequestParam(required = true) String pathFile,
			@RequestParam(required = true) String pathComparedFile,
			@RequestParam(required = false, defaultValue = "0") Integer from_line,
			@RequestParam(required = false, defaultValue = "10000") Integer to_line,
			@RequestParam(defaultValue = "false") boolean onlyread,
			@RequestParam(defaultValue = "false") boolean pkey, @RequestParam(defaultValue = "false") boolean all)
			throws IOException {
		System.out.println("started read log" + new Date());
		List<String> result = new ArrayList<String>();
		List<String> result2 = new ArrayList<String>();
		
		if(onlyread) {
			try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
				// Convert a Stream to List
				result = lines.filter(x -> !x.isEmpty()).collect(Collectors.toList());
			}
		}else {
			try (Stream<String> lines = Files.lines(Paths.get(pathComparedFile))) {
				// Convert a Stream to List
				lines.filter(x -> !x.isEmpty()).forEach(data->{
					System.out.println(data.split("=")[1].split(" ")[1]);
					result2.add(data.split("=")[1].split(" ")[1].replaceAll("\\s", ""));
				});
			}
			
			try (Stream<String> lines = Files.lines(Paths.get(pathFile))) {
				// Convert a Stream to List
				result = lines.collect(Collectors.toList());
			}
		}
		
		result=all ? result : result.subList(from_line, to_line);

		checkReadLines(result.stream().filter(x -> !result2.contains(x)).collect(Collectors.toList()), pkey);
		AtomicInteger increment = new AtomicInteger(0);
		result.forEach(s->{
			System.out.println(increment.getAndIncrement()+" "+s);
		});
		System.out.println("end" + new Date());
		return "read the log started";
	}

	public void checkReadLines(List<String> lists, boolean pKey) {
		String file = "C:/Users/Lukman Fyrtio/Documents/Others/wallet_not_checks" + new Date().getTime() + ".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw"); FileChannel channel = writer.getChannel()) {
			long before = System.currentTimeMillis();
			lists.stream().forEach(data -> {
				try {
					String a=String.format("%s\n", data);
    				channel.write(ByteBuffer.wrap(a.getBytes(StandardCharsets.UTF_8)));
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
	
	public void readLines(List<String> lists, boolean pKey) {
		String[] key = { "6ZRPHAIJAZKT94NF5WW46K5NAAU57HSMMF", "I6W3DJCECRIZGH1148E1IRRC8PNKYYM839",
				"E84IYMIB9BRV4B9PRC47R77C4RDE5IJEQF", "DR688TYGCRUGJE483ESKVWB12PPYI85F74",
				"ZRDCEDCCMWFWTQNV1ATJHB18SFV1YCAPN4" };
		String file = "C:/Users/Lukman Fyrtio/Documents/Others/wallet_checking" + new Date().getTime() + ".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw"); FileChannel channel = writer.getChannel()) {
			long before = System.currentTimeMillis();
			Collection<CompletableFuture<BalanceResponse>> listThread = new ArrayList<CompletableFuture<BalanceResponse>>();
			AtomicInteger increment = new AtomicInteger(0);
			logService.setChannel(channel);
			lists.stream().forEach(data -> {
				try {
//					int intKey = logService.getRandomNumberUsingInts(0, 5);
					if(increment.get()==4)increment.set(0);
					String apiKey = key[increment.getAndIncrement()];
					listThread.add(logService.getBalance(data.replaceAll("\\s", ""), apiKey, pKey));
					if (listThread.size() >= 30) {
						listThread.stream().forEach(async -> {
							try {
								while (!async.isDone()) {
									System.out.println("Wait until checking");
									Thread.sleep(1000L);
								}
//								increment.getAndIncrement();
//								BalanceResponse resA = async.get();
//								String dataR = pKey
//										? String.format(
//												increment.getAndIncrement()
//														+ ".key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n",
//												resA.getpKey(), resA.getWallet(), resA.getEthScale(),
//												resA.getBNBScale(), resA.getTUSDScale(),
//												!resA.getBnb().equals("0") || !resA.getTusd().equals("0")
//														|| !resA.getEth().equals("0") ? "found" : "")
//										: String.format(
//												increment.getAndIncrement()
//														+ ". wallet = %s ETH = %s BNB = %s TUSD = %s %s\n",
//												resA.getWallet(), resA.getEthScale(), resA.getBNBScale(),
//												resA.getTUSDScale(),
//												!resA.getBnb().equals("0") || !resA.getTusd().equals("0")
//														|| !resA.getEth().equals("0") ? "found" : "");
////				    		        ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
////				    				channel.write(buff);
//								System.out.println(dataR);
							} catch (InterruptedException e) {
								System.out.println("Line 80");
								e.printStackTrace();
							}
						});
						listThread.clear();
					}
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
					System.out.println("Line 85");
				}
			});
			if (!listThread.isEmpty()) {
				listThread.stream().forEach(async -> {
					try {
						while (!async.isDone()) {
							Thread.sleep(1000L);
							
						}
//						BalanceResponse resA = async.get();
//						String dataR = pKey
//								? String.format(
//										increment.getAndIncrement()
//												+ ".key = %s wallet = %s ETH = %s BNB = %s TUSD = %s %s\n",
//										resA.getpKey(), resA.getWallet(), resA.getEthScale(), resA.getBNBScale(),
//										resA.getTUSDScale(),
//										!resA.getBnb().equals("0") || !resA.getTusd().equals("0")
//												|| !resA.getEth().equals("0") ? "found" : "")
//								: String.format(
//										increment.getAndIncrement() + ". wallet = %s ETH = %s BNB = %s TUSD = %s %s\n",
//										resA.getWallet(), resA.getEthScale(), resA.getBNBScale(), resA.getTUSDScale(),
//										!resA.getBnb().equals("0") || !resA.getTusd().equals("0")
//												|| !resA.getEth().equals("0") ? "found" : "");
//			    		        ByteBuffer buff = ByteBuffer.wrap(dataR.getBytes(StandardCharsets.UTF_8));
//			    				channel.write(buff);
//						System.out.println(dataR);
					} catch (InterruptedException e) {
						System.out.println("Line 119");
						e.printStackTrace();
					}
				});
				listThread.clear();
			}
			long after = System.currentTimeMillis();
			System.out.println("time to execute: " + (after - before) / 1000.0 + " seconds.");
		} catch (Exception e2) {
			// TODO: handle exception
		}
	}


	public void readLinesBSC(List<String> lists, boolean pKey,String nodeAddress,String fileName) {
		String file = "C:/Users/Lukman Fyrtio/Documents/Others/"+fileName+"_bsc_" + new Date().getTime() + ".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw"); FileChannel channel = writer.getChannel()) {
			long before = System.currentTimeMillis();
			Collection<CompletableFuture<BalanceResponse>> listThread = new ArrayList<CompletableFuture<BalanceResponse>>();
			logService.setChannel(channel);
			lists.stream().forEach(data -> {
				try {
					listThread.add(logService.getBalanceBSC(data.replaceAll("\\s", ""), pKey,nodeAddress));
					if (listThread.size() >= 60) {
						listThread.stream().forEach(async -> {
							try {
								while (!async.isDone()) {
									System.out.println("Wait until checking");
									Thread.sleep(1000L);
								}
							} catch (InterruptedException e) {
								System.out.println("Line 80");
								e.printStackTrace();
							}
						});
						listThread.clear();
					}
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
					System.out.println("Line 85");
				}
			});
			if (!listThread.isEmpty()) {
				listThread.stream().forEach(async -> {
					try {
						while (!async.isDone()) {
							Thread.sleep(1000L);
							
						}
					} catch (InterruptedException e) {
						System.out.println("Line 119");
						e.printStackTrace();
					}
				});
				listThread.clear();
			}
			long after = System.currentTimeMillis();
			System.out.println("time to execute: " + (after - before) / 1000.0 + " seconds.");
		} catch (Exception e2) {
			// TODO: handle exception
		}
	}


	public void readLinesInfura(List<String> lists, boolean pKey,String nodeAddress) {
		String file = "C:/Users/Lukman Fyrtio/Documents/Others/wallet_checking" + new Date().getTime() + ".txt";
		try (RandomAccessFile writer = new RandomAccessFile(file, "rw"); FileChannel channel = writer.getChannel()) {
			long before = System.currentTimeMillis();
			Collection<CompletableFuture<BalanceResponse>> listThread = new ArrayList<CompletableFuture<BalanceResponse>>();
			logService.setChannel(channel);
			lists.stream().forEach(data -> {
				try {
					listThread.add(logService.getBalanceInfura(data.replaceAll("\\s", ""), pKey,nodeAddress));
					if (listThread.size() >= 60) {
						listThread.stream().forEach(async -> {
							try {
								while (!async.isDone()) {
									System.out.println("Wait until checking");
									Thread.sleep(1000L);
								}
							} catch (InterruptedException e) {
								System.out.println("Line 80");
								e.printStackTrace();
							}
						});
						listThread.clear();
					}
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
					System.out.println("Line 85");
				}
			});
			if (!listThread.isEmpty()) {
				listThread.stream().forEach(async -> {
					try {
						while (!async.isDone()) {
							Thread.sleep(1000L);
							
						}
					} catch (InterruptedException e) {
						System.out.println("Line 119");
						e.printStackTrace();
					}
				});
				listThread.clear();
			}
			long after = System.currentTimeMillis();
			System.out.println("time to execute: " + (after - before) / 1000.0 + " seconds.");
		} catch (Exception e2) {
			// TODO: handle exception
		}
	}

	public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
		CompletableFuture<Void> allFuturesResult = CompletableFuture
				.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
		return allFuturesResult
				.thenApply(v -> futuresList.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}

}