# 中山大学数据科学与计算机学院本科生实验报告

**课程名称**：区块链原理与技术								**任课教师**： 郑子彬

**年级**：17 级															**专业**：软件工程

**学号**：17343081													**姓名**：lyy

**开始日期**：2019/11/16										**完成日期**：2019/12/10



## 一、项目背景

将供应链上的每一笔交易和应收账款单据上链，同时引入第三方可信机构来确认这些信息的交易，例如银行，物流公司等，确保交易和单据的真实性。同时，支持应收账款的转让，融资，清算等 ，让核心企业的信用可以传递到供应链的下游企业，减小中小企业的融资难度。

### 功能需求

1. 实现采购商品签发应收账款交易上链 。例如车企从轮胎公司购买一批轮胎并签订应收账款单据。
2. 实现应收账款的转让上链 ，轮胎公司从轮毂公司购买一笔轮毂，便将于车企的应收账款单据部分转让给轮毂公司。轮毂公司可以利用这个新的单据去融资或者要求车企到期时归还钱款。
3. 利用应收账款向银行融资上链 ，供应链上所有可以利用应收账款单据向银行申请融资。
4. 应收账款支付结算上链，应收账款单据到期时核心企业向下游企业支付相应的欠款 。



## 二、方案设计

### 开发环境

- Linux Ubuntu16.04

- Fisco-Bcos 2.0 区块链底层

- eclipse IDE

- JAVA GUI

- Gradle 6.0

- Spring 4.0.6 框架

  

### 项目结构

```
.
├── bin
├── build.gradle
├── gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── solidity
│   └── SupplyChain.sol
└── src
    ├── main
    │   ├── java
    │   │   ├── ChainApp
    │   │   │   ├── Library.java
    │   │   │   └── Runner.java
    │   │   ├── gui
    │   │   │   ├── MainWindow.java
    │   │   │   └── StartWindow.java
    │   │   └── web3j
    │   │       └── SupplyChain.java
    │   └── resources
    │       ├── 0x25c90b627ba385104b4a0200dce0b69f562129c0.pem
    │       ├── 0x25c90b627ba385104b4a0200dce0b69f562129c0.public.pem
    │       ├── ...
    │       ├── applicationContext.xml
    │       ├── ca.crt
    │       ├── log4j2.xml
    │       ├── sdk.crt
    │       └── sdk.key
    └── test
        ├── java
        │   └── ChainApp
        │       └── LibraryTest.java
        └── resources
```



`./bin/`	可执行文件路径

`./build.gradle`	gradle 配置文件

`./solidity/SupplyChain.sol`	智能合约源文件

`./src/main/`	JAVA 源文件路径

`./src/main/java/ChainApp/Runner.java`	应用入口

`./src/main/java/gui/`	应用 GUI 源文件路径

`./src/main/java/web3j/`	智能合约 JAVA 源文件

`./src/resources/`	应用 JAVA 源文件依赖文件目录

`./src/resources/applicationContext.xml`	Spring 配置文件



### 存储设计

公司注册信息与交易回执存储在区块链，在智能合约中定义，数据结构如下：

#### 公司信息数据结构

```java
	/*
	*	公司结构体：
	*		name	公司名称
	*		addr 	公司账户地址
	*		balance 公司账户余额
	*		type_t	公司类型("company" 普通公司/ "bank" 银行/ "arbitrator" 仲裁机构)
	*		receipts_num 拥有回执数量
	*		receipts 拥有回执序号的映射
	*/
	struct Company {
		string name;
		address addr;
		int256 balance;
		string type_t;
		uint receipts_num;
		mapping (uint => uint256) receipts;
	}
```

#### 交易回执数据结构

```java
/*
	*	回执结构体:
	*		from 	付款公司名称
	*		to 		收款公司名称
	*		value 	款项大小
	*		return_time 付款时限，格式 yyMMdd
	*		status 	回执状态(0 无效/ 1 有效/ 2 已转移/ 3 逾期未付款/ 4 已付款)
	*		used 	已用于申请融资/融资回执
	*		signer 	签名账户地址(signer[0] 收款公司地址/ signer[1] 仲裁机构地址)
	*		sign 	签名(sign[0] 收款公司签名/ sign[1] 仲裁机构签名)
	*/
	struct Receipt {
		string from;
		string to;
		int256 value;
		uint return_time;
		int status;
		bool used;
		address[] signer;
		bool[] sign;
	}
```

#### 存储数据结构

使用两个动态数组分别存储应用的所有注册公司信息和交易回执信息。

```java
	uint256 companies_num;	// 注册公司数量
	Company[] companies;	// 注册公司动态数组
	uint256 receipts_num;	// 回执数量
	Receipt[] receipts;		// 回执动态数组
```



### 数据流图

![未命名文件](C:\Users\DELL\Desktop\BlockChain\未命名文件.png)

​																	**图 供应链金融应用数据流**

### 角色介绍

每个外部账户对应唯一一个角色，本应用为不同的角色提供不同的功能。

#### 管理员(Admin)

部署智能合约的账户自动成为唯一的管理员，可以增加其它用户的余额。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210191754306.png" alt="image-20191210191754306" style="zoom:50%;" />

​																	**图 管理员界面**

#### 普通公司(Company)

供应链上的公司，可以发起交易、签名交易、转移应收账款、申请融资等。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210192951979.png" alt="image-20191210192951979" style="zoom:50%;" />

​																	**图 普通公司界面**

#### 银行(Bank)

主要功能是为供应链上的公司提供融资服务。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210193123571.png" alt="image-20191210193123571" style="zoom:50%;" />

​																	**图 银行界面**

#### 仲裁机构(Arbitrator)

主要功能是为交易回执签名。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210193217841.png" alt="image-20191210193217841" style="zoom:50%;" />

​																	**图 仲裁机构界面**

### 核心功能介绍

#### 1. 登录(附加功能)

每个应用账户对应区块链上一个有公私钥对的外部账户，通过向应用提供其私钥，登录账户。

##### 创建外部账户

使用 Fisco-Bcos 提供的 `get_account.sh` 脚本，可以生成 PEM 格式或 PKCS12 格式的公私钥文件。此处选择使用 PEM 格式。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210203350296.png" alt="image-20191210203350296" style="zoom:50%;" />

​																	**图 创建外部账户**

##### web3sdk 使用账户

将私钥文件 `.pem` 和公钥文件 `.public.pem` 导入工程的 `./src/main/resources` 文件夹下。

在 spring 配置文件 `applicationContext.xml` 中引入私钥文件。

```xml
	<bean id="pemid" class="org.fisco.bcos.channel.client.PEMManager" init-method="load" >
		<property name="pemFile" value="classpath:address.pem" />
	</bean>
```

通过 `PEMManager` 取得公私钥，并用对应账户连接区块链。

```java
		PEMManager pem = context.getBean("pemid", PEMManager.class);
		ECKeyPair pemKeyPair = pem.getECKeyPair();
		credentials = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
```

##### 智能合约实现

`SupplyChain::getMyInfo` 方法根据传入的账户地址参数取得对应公司信息。若未注册，返回地址信息为 0。

```java
/*
	*	描述：
	*		输出公司信息
	*/
function getMyInfo() public 
    returns(string memory out_name, address out_addr, int256 out_balance, string memory out_type, uint out_r_num, uint256[] out_r) {
    for(uint256 i = 0; i < companies_num; i ++) {
        if(companies[i].addr == msg.sender) {
            out_name = companies[i].name;
            out_addr = companies[i].addr;
            out_balance = companies[i].balance;
            out_type = companies[i].type_t;
            out_r_num = companies[i].receipts_num;
            out_r = new uint256[](out_r_num);
            for(uint j = 0; j < out_r_num; j ++) {
                out_r[j] = companies[i].receipts[j];
            }
            break;
        }
    }
}
```

##### JAVA 代码实现

凭借私钥，使用对应的外部账号连接区块链，并根据已部署的智能合约 `SupplyChain.sol` 的合约地址加载合约。JAVA 代码实现如下：

```java
	/*
	 * fisco-bcos
	 */
	private ApplicationContext context;
	private Service service;
	private ChannelEthereumService channelEthereumService;
	private Web3j myWeb3j;
	private Credentials credentials;
	private SupplyChain supplyChain;
	
	private static String contractAddr = "...";

		/*
		 * apply web3sdk
		 */
		context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		service = context.getBean(Service.class);
		service.run();
		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
	    channelEthereumService.setChannelService(service);
	    channelEthereumService.setTimeout(10000);
	    myWeb3j = Web3j.build(channelEthereumService, service.getGroupId());
		
		BigInteger gasPrice = new BigInteger("300000000");
		BigInteger gasLimit = new BigInteger("300000000");
		/*
		 * load up external account with private key
		 */
		PEMManager pem = context.getBean("pem"+pk_str, PEMManager.class);
		ECKeyPair pemKeyPair = pem.getECKeyPair();
		credentials = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
		/*
		 * load deployed smart contract
		 */
		supplyChain = SupplyChain.load(contractAddr, myWeb3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
```

调用 `SupplyChain::getMyInfo` 方法取得公司信息。

> [web3sdk 调用合约方法详情](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/java_sdk.html#id7)

```java
TransactionReceipt getMyInfoTx = supplyChain.getMyInfo().send();

```

调用 web3sdk 提供的 `TransactionDecoder` 类解析调用合约方法的返回值和事件，以下功能介绍时不再赘述。此处使用 `TransactionDecoder.decodeOutputReturnJson` 方法，取得 JSON 格式的输出值字符串，并调用 `fastJSON` 库解析输出。

> [web3sdk 交易解析详情](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/java_sdk.html#id11)

```java
static String abi = "...";
static String bin = "";
private TransactionDecoder txDecoder = new TransactionDecoder(abi, bin);

String strJson = txDecoder.decodeOutputReturnJson(getMyInfoTx.getInput(), getMyInfoTx.getOutput());

```



#### 2.注册(附加功能)

注册需要输入私钥、名称、公司类型，要求：

- 一个外部账户(即一个私钥)只能注册一个应用账户；
- 名称不能重复。

##### 智能合约实现

 `SupplyChain::registerCompany` 方法实现注册功能。

```java
/*
	*	描述：
	*		注册公司
	*	参数：
	*		name	公司名称
	*		addr 	公司账户地址
	*		type_t	公司类型
	*	返回值：
	*		>=0		公司的数组下标
	*		-1		公司姓名或地址已被注册
	*		-2		调用账户地址不是注册公司地址	
	*/
function registerCompany(string memory name, address addr, string memory type_t) public returns(uint256){
    if(addr != msg.sender)
        return (uint256)(-2);

    for(uint256 i = 0;i < companies.length; i ++) {
        if(utilCompareInternal(companies[i].name, name)
           || companies[i].addr == addr) {
            return (uint256)(-1);
        }
    }
    // 余额初始化为 0
    addCompany(name, addr, 0, type_t);

    return companies_num-1;
}

```

##### JAVA 代码实现

调用  `SupplyChain::registerCompany` 方法。

```java
TransactionReceipt tx;
myAddr = new BigInteger(credentials.getAddress().substring(2),16);
tx = supplyChain.registerCompany(name, credentials.getAddress(), type_t).send();

```



#### 3.发起应收账款交易(基本功能)

发起采购商品签发应收账款交易，生成回执单据，要求：

- 交易双方公司都已注册。

若成功，返回回执 ID。

##### 智能合约实现

`registerReceipt` 方法。

```java
/*
	*	描述：
	*		付款公司发起交易，生成应收账款回执
	*	参数：
	*		from 	付款公司名称
	*		to 		收款公司名称
	*		value 	款项大小
	*		return_time 付款时限
	*	返回值：
	*		>=0		回执的数组下标
	*		-1		公司未注册
	*		-2		调用者不是付款公司	
	*/
	function registerReceipt(string memory from, string memory to, int256 value, uint return_time) public returns(uint256) {
		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return (uint256)(-1);

		if(companies[from_idx].addr != msg.sender)
			return (uint256)(-2);

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return (uint256)(-1);

		addReceipt(from_idx, to_idx, from, to, value, return_time);

		return (receipts_num-1);
	}

```

##### JAVA 代码实现

以 GUI 输入内容为参数，调用 `registerReceipt` 方法。

```java
receiptTx = supplyChain.registerReceipt(rfrom_text.getText(), rto_text.getText(), new BigInteger(rval_text.getText()), new BigInteger(rreturn_time_text.getText())).send();

```



#### 4. 查看回执信息(附加功能)

根据回执 ID 查询回执信息。

##### 智能合约实现

`showReceipt` 方法。

```java
/*
	*	描述：
	*		输出回执信息
	*	参数：
	*		r_idx	回执的数组下标
	*/
	function showReceipt(uint256 r_idx) public 
	returns(uint256 out_idx, string memory out_from, string memory out_to, int256 out_value, uint out_return_time, int out_status, bool out_used, address[2] out_signer, bool[2] out_sign){
	//	emit showReceiptEvent(r_idx ,receipts[r_idx].from, receipts[r_idx].to, receipts[r_idx].value, receipts[r_idx].return_time, receipts[r_idx].status, receipts[r_idx].used);
		out_idx = r_idx;
		out_from = receipts[r_idx].from;
		out_to = receipts[r_idx].to;
		out_value = receipts[r_idx].value;
		out_return_time = receipts[r_idx].return_time;
		out_status = receipts[r_idx].status;
		out_used = receipts[r_idx].used;
		for(uint256 i = 0; i < 2; i ++) {
			out_signer[i] = receipts[r_idx].signer[i];
			out_sign[i] = receipts[r_idx].sign[i];
		}
	}

```

##### JAVA 代码实现

调用 `showReceipt` 方法。

```java
showReceiptTx = supplyChain.showReceipt(new BigInteger(ridx_text.getText())).send();

```



#### 5. 回执签名(附加功能)

收款公司或仲裁机构对回执签名，只有在收款公司签名后，回执才有效，仲裁机构对回执签名后增加回执的可信度。要求：

- 签名账户是收款公司或仲裁机构。

##### 智能合约实现

`signReceipt` 方法。

```java
/*
	*	描述：
	*		收款公司/仲裁机构对回执签名
	*	参数：
	*		r_idx	回执的数组下标
	*	返回值：
	*		0		成功
	*		-1		不是收款公司/仲裁机构，签名失败
	*/
	function signReceipt(uint256 r_idx) public returns(int) {
		uint256 c_idx = getCompanyIdx(receipts[r_idx].to);
		if(companies[c_idx].addr == msg.sender) {
			receipts[r_idx].sign[0] = true;
			// 收款公司签名，回执生效
			receipts[r_idx].status = 1;
			return 0;
		}

		c_idx = getCompanyIdxByAddr(msg.sender);
		if(utilCompareInternal(companies[c_idx].type_t, "arbitrator")) {
			receipts[r_idx].signer[1] = msg.sender;
			receipts[r_idx].sign[1] = true;
			return 0;
		}
		return -1;
	}

```

##### JAVA 代码实现

调用 `signReceipt` 方法。

```java
signTx = supplyChain.signReceipt(new BigInteger(ridx_text.getText())).send();

```



#### 6. 查看公司信息(附加功能)

根据公司名称查看公司信息。

##### 智能合约实现

`getCompany` 方法。

```java
/*
	* 	描述：
	*		输出公司信息
	*	参数：
	*		c_name	公司名称
	*/
	function getCompany(string memory c_name) public returns(uint256, string, address, string){
		uint256 c_idx = getCompanyIdx(c_name);
		if(c_idx != (uint256)(-1)){
			emit showCompanyEvent(companies[c_idx].name, companies[c_idx].addr, companies[c_idx].balance, companies[c_idx].type_t, companies[c_idx].receipts_num);
			return (c_idx, companies[c_idx].name, companies[c_idx].addr, companies[c_idx].type_t);
		}
	}

```

##### JAVA 代码实现

调用 `getCompany` 方法。

```java
companyTx = supplyChain.getCompany(cname_text.getText()).send();

```



#### 7. 转移应收账款(基本功能)

转移指定金额的应收账款，要求：

- 交易双方公司都已注册；
- 付款公司能够转移的应收账款的金额大于等于指定金额；
- 调用账户地址与付款公司地址一致。

##### 智能合约实现

`transferReceipt` 方法。

```java
/*
	*	描述：
	*		转移应收账款
	*	参数：
	*		from 	付款公司名称
	*		to 		收款公司名称
	*		value 	款项大小
	*	返回值：
	*		>=0		成功
	*		-1		公司未注册
	*		-2		付款公司应收账款的金额不足
	*		-3		调用者不是付款公司	
	*/
	function transferReceipt(string memory from, string memory to, int256 value) public returns(uint256){
		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return (uint256)(-1);

		if(companies[from_idx].addr != msg.sender)
			return (uint256)(-3);

		uint256 r_idx = (uint256)(-1);
		for(uint256 i = 0; i < companies[from_idx].receipts_num; i ++) {
			uint256 idx = companies[from_idx].receipts[i];
			// 付款公司是应收账款的收款人，回执有效且金额足够
			if(utilCompareInternal(receipts[idx].to, from) && receipts[idx].status == 1
				&& receipts[idx].value >= value) {
				r_idx = idx;
				break;
			}
		}
		if(r_idx == (uint256)(-1))
			return (uint256)(-2);

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return (uint256)(-1);

		if(receipts[r_idx].value == value) {
			receipts[r_idx].status = 2;
		}
		else {
			receipts[r_idx].value -= value;
		}
		emit showReceiptEvent(r_idx ,receipts[r_idx].from, receipts[r_idx].to, receipts[r_idx].value, receipts[r_idx].return_time, receipts[r_idx].status, receipts[r_idx].used);
		uint256 f_idx = getCompanyIdx(receipts[r_idx].from);
		addReceipt(f_idx, to_idx, receipts[r_idx].from, to, value, receipts[r_idx].return_time);

		return (receipts_num-1);
	}

```

##### JAVA 代码实现

调用 `transferReceipt` 方法。

```java
transferTx = supplyChain.transferReceipt(tfrom_text.getText(), tto_text.getText(), new BigInteger(tval_text.getText())).send();

```



#### 8. 申请融资(基本功能)

公司凭借应收账款向银行申请融资，要求：

- 申请公司已注册；
- 融资对象是银行，不能是普通公司；
- 申请公司拥有可用于融资凭证的应收账款。

##### 智能合约实现

`applyFinancing` 方法。

```java
/*
	*	描述：
	*		根据应收账款向银行申请融资
	*	参数：
	*		from 	申请公司名称
	*		to 		银行名称
	*	返回值：
	*		参数 1：
	*			0		成功
	*			-1		公司未注册
	*			-2		接收申请者不是银行
	*			-3		调用者不是申请公司
	*			-4		申请公司不拥有可融资的回执
	*		参数 2：
	*			0		融资失败
	*			>0		融资金额
	*/
	function applyFinancing(string memory from, string memory to) public returns(int, int256){
		int ret = -4;
		int val = 0;

		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return (-1, val);

		if(companies[from_idx].addr != msg.sender)
			return (-3, val);

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return (-1, val);

		if(!utilCompareInternal(companies[to_idx].type_t, "bank"))
			return (-2, val);

		uint256 r_num = companies[from_idx].receipts_num;
		for(uint256 i = 0; i < r_num; i ++) {
			uint256 r_idx = companies[from_idx].receipts[i];
			// 申请公司是应收账款的收款人，回执有效，未曾用于申请融资且不是融资回执
			if(utilCompareInternal(receipts[r_idx].to, from) && receipts[r_idx].status == 1
				&& receipts[r_idx].used == false) {
				addReceipt(from_idx, to_idx, from, to, receipts[r_idx].value, receipts[r_idx].return_time);
				receipts[receipts_num-1].used = true;
				receipts[receipts_num-1].status = 1;
				
				receipts[r_idx].used = true;
				
				transferBalancePrivate(to, from, receipts[r_idx].value);
				
				ret = 0;
				val += receipts[r_idx].value;

				emit showCompanyEvent(from, companies[from_idx].addr, companies[from_idx].balance, companies[from_idx].type_t,companies[from_idx].receipts_num);
			}
		}

		return (ret, val);
	}

```

##### JAVA 代码实现

调用 `applyFinancing` 方法。

```java
financingTx = supplyChain.applyFinancing(ffrom_text.getText(), fto_text.getText()).send();

```



#### 9. 还款(附加功能)

付款公司支付应收账款，要求：

- 付收款公司都已注册；
- 还款公司余额足够；
- 有付收款公司符合申请，有效且未还款的应收账款。

##### 智能合约实现

`returnDebtCompany` 方法。

```java
/*
	*	描述：
	*		付款公司支付应收账款
	*	参数：
	*		from 	付款公司名称
	*		to 		收款公司名称
	*	返回值：
	*		参数 1：
	*			0		成功
	*			-1		公司未注册
	*			-2		还款公司余额不足
	*			-3		没有符合应还账款
	*		参数 2：
	*			0		还款失败
	*			>0		还款金额
	*/
	function returnDebtCompany(string memory from, string memory to) public returns(int, int256) {
		int ret = -3;
		int256 val = 0;
		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return (-1, val);

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return (-1, val);

		for(uint256 i = 0; i < companies[from_idx].receipts_num; i ++) {
			uint256 r_idx = companies[from_idx].receipts[i];
			if(utilCompareInternal(receipts[r_idx].from, from)
				&& utilCompareInternal(receipts[r_idx].to, to)
				&& (receipts[r_idx].status == 1 || receipts[r_idx].status == 3)) {
				int ret_t = transferBalancePrivate(from, to, receipts[r_idx].value);
				if(ret_t == -2)
					return (ret_t, val);
				ret = 0;
				val += receipts[r_idx].value;
				receipts[r_idx].status = 4;
				emit showReceiptEvent(r_idx ,receipts[r_idx].from, receipts[r_idx].to, receipts[r_idx].value, receipts[r_idx].return_time, receipts[r_idx].status, receipts[r_idx].used);
			}
		}

		return (ret, val);
	}

```

##### JAVA 代码实现

调用 `returnDebtCompany` 方法。

```java
debtTx = supplyChain.returnDebtCompany(dfrom_text.getText(), dto_text.getText()).send();

```



#### 10. 转账(附加功能)

简单的转账功能，要求：

- 交易双方公司已注册；
- 付款公司余额足够。

##### 智能合约实现

`transferBalance` 方法。

```java
/*
	*	描述：
	*		转账
	*	参数：
	*		from 	付款公司
	*		to 		收款公司
	*		value 	款项大小
	*	返回值：
	*		0		成功
	*		-1		公司不存在
	*		-2		付款公司余额不足
	*		-3		调用者不是付款公司	
	*/
	function transferBalance(string memory from, string memory to, int256 value) public returns(int) {
		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return -1;

		if(companies[from_idx].addr != msg.sender)
			return -3;

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return -1;

		if(companies[from_idx].balance < value)
			return -2;

		companies[from_idx].balance -= value;
		companies[to_idx].balance += value;

		return 0;
	}

```

##### JAVA 代码实现

调用 `transferBalance` 方法。

```java
tbTx = supplyChain.transferBalance(tbfrom_text.getText(), tbto_text.getText(), new BigInteger(tbval_text.getText())).send();

```



#### 11. 增加余额(附加功能)

管理员账户修改注册公司的余额，要求：

- 公司已注册；
- 调用者为管理员。

##### 智能合约实现

`addBalance` 方法。

```java
/*
	*	描述：
	*		管理员增加公司的余额
	*	参数：
	*		to 		收款公司
	*		value 	款项大小
	*	返回值：
	*		0		成功
	*		-1		调用者不是管理员
	*		-2		公司不存在	
	*/
	function addBalance(string memory to, int256 value) public returns(int) {
		if(msg.sender != admin_addr)
			return -1;

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return -2;

		companies[to_idx].balance += value;
		return 0;
	}

```

##### JAVA 代码实现

调用 `addBalance` 方法。

```java
addbTx = supplyChain.addBalance(acname_text.getText(), new BigInteger(aval_text.getText())).send();

```



## 三、功能测试

#### 1. 登录(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210191622447.png" alt="image-20191210191622447" style="zoom:50%;" />

​																		**图 登录界面**

##### 异常处理

(1) 账户未注册

登录失败，提示必须先注册。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210202314666.png" alt="image-20191210202314666" style="zoom:50%;" />

​																	**图 账户未注册，登陆失败**



#### 2.注册(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210195621110.png" alt="image-20191210195621110" style="zoom:50%;" />

​																	**图 注册界面**

##### 异常处理

(1) 私钥已注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210210838094.png" alt="image-20191210210838094" style="zoom:50%;" />

​																	**图 私钥已注册，注册失败界面**

(2) 名称已注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210211112206.png" alt="image-20191210211112206" style="zoom:50%;" />

​																	**图 名称已注册，注册失败界面**

#### 3.发起应收账款交易(基本功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210212054680.png" alt="image-20191210212054680" style="zoom:50%;" />

​																	**图 发起应收账款交易界面**

##### 异常处理

(1) 收款公司未注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210212605679.png" alt="image-20191210212605679" style="zoom:50%;" />

​															**图 收款公司未注册，发起应收账款交易失败**



#### 4. 查看回执信息(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210212921783.png" alt="image-20191210212921783" style="zoom:50%;" />

​																	**图 查看回执界面**



#### 5. 回执签名(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210213422359.png" alt="image-20191210213422359" style="zoom:50%;" />

​																	**图 回执签名界面**

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210213510631.png" alt="image-20191210213510631" style="zoom:50%;" />

​															**图 回执签名后，sign 的值变为 true**

##### 异常处理

(1) 签名者不是回执的收款人，也不是仲裁机构

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210213841867.png" alt="image-20191210213841867" style="zoom:50%;" />

​																		**图 回执签名失败**



#### 6. 查看公司信息(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210214159428.png" alt="image-20191210214159428" style="zoom:50%;" />

​															**图 查看公司信息界面**



#### 7. 转移应收账款(基本功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210214453577.png" alt="image-20191210214453577" style="zoom:50%;" />

​															**图 转移应收账款界面**

##### 异常处理

(1) 收款方未注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210214905138.png" alt="image-20191210214905138" style="zoom:50%;" />

​															**图 收款方未注册，转移应收账款失败**

(2) 付款方金额不足

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210215228653.png" alt="image-20191210215228653" style="zoom:50%;" />

​															**图 付款方应收账款金额不足，转移应收账款失败**



#### 8. 申请融资(基本功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210215624672.png" alt="image-20191210215624672" style="zoom:50%;" />

​															**图 申请融资界面**

##### 异常处理

(1) 接收申请者未注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210215917991.png" alt="image-20191210215917991" style="zoom:50%;" />

​															**图 接收申请者未注册，融资失败**

(2) 接收申请者不是银行

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210220056320.png" alt="image-20191210220056320" style="zoom:50%;" />

​															**图 接收申请者不是银行，融资失败**

(3) 申请者不拥有应收账款

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210220139692.png" alt="image-20191210220139692" style="zoom:50%;" />

​															**图 申请者不拥有应收账款，融资失败**



#### 9. 还款(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210220337414.png" alt="image-20191210220337414" style="zoom:50%;" />

​																			**图 还款界面**

##### 异常处理

(1) 收款方未注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210220719103.png" alt="image-20191210220719103" style="zoom:50%;" />

​															**图 收款方未注册，还款失败**

(2) 还款方余额不足

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210221043485.png" alt="image-20191210221043485" style="zoom:50%;" />

​															**图 还款方余额不足，还款失败**

(3) 没有待还款的账款

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210221232116.png" alt="image-20191210221232116" style="zoom:50%;" />

​															**图 接没有待还款的账款，还款失败**



#### 10. 转账(附加功能)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210221831517.png" alt="image-20191210221831517" style="zoom:50%;" />

​															**转账界面**

##### 异常处理

(1) 收款方未注册

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210222257863.png" alt="image-20191210222257863" style="zoom:50%;" />

​															**图 收款方未注册，转账失败**

(2) 付款方余额不足

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210222523303.png" alt="image-20191210222523303" style="zoom:50%;" />

​															**图 付款方余额不足，还款失败**



## 四、界面展示

### 开始界面

上端选项卡切换登录界面与注册界面。

#### 登录界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211003332257.png" alt="image-20191211003332257" style="zoom:50%;" />

#### 注册界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211003404574.png" alt="image-20191211003404574" style="zoom:50%;" />

### 主界面

左端选项卡切换功能，右端为功能的输入和输出面板。

#### 按照角色

##### 管理员界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210191754306.png" alt="image-20191210191754306" style="zoom:50%;" />

##### 普通公司界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210192951979.png" alt="image-20191210192951979" style="zoom:50%;" />

##### 银行界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210193123571.png" alt="image-20191210193123571" style="zoom:50%;" />

##### 仲裁机构界面

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210193217841.png" alt="image-20191210193217841" style="zoom:50%;" />

#### 按照功能

##### 登录账户信息界面(Account)

`Refresh` 按键刷新账户信息。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191210193217841.png" alt="image-20191210193217841" style="zoom:50%;" />

##### 回执信息界面(Show & Sign Receipt)

输入需要查询或签名的回执 ID 后，点击 `OK` 按键进行查询，查询结果显示在下方；点击 `Sign` 按键签名回执。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004140568.png" alt="image-20191211004140568" style="zoom:50%;" />

##### 公司信息界面(Show Company)

输入需要查询的公司名称后，点击 `OK` 按键进行查询，查询结果显示在下方。

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004416525.png" alt="image-20191211004416525" style="zoom:50%;" />

##### 生成应收账款界面(Register Receipt)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004535567.png" alt="image-20191211004535567" style="zoom:50%;" />

##### 转移应收账款界面(Transfer Receipt)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004630006.png" alt="image-20191211004630006" style="zoom:50%;" />

##### 申请融资界面(Financing)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004705067.png" alt="image-20191211004705067" style="zoom:50%;" />

##### 还款界面(Return Debt)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004738987.png" alt="image-20191211004738987" style="zoom:50%;" />

##### 转账界面(Transfer Balance)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191211004823989.png" alt="image-20191211004823989" style="zoom:50%;" />

##### 增加余额界面(Add Balance)

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20191213202435233.png" alt="image-20191213202435233" style="zoom:50%;" />



## 五、加分项

- 实现供应链金融相关附加功能：
  - 使用公私钥加密系统，唯一私钥登录账户；
  - 交易签名功能，收款方和仲裁机构审核并签名回执；
  - 转账和还款的基本交易功能。
- 考虑并处理了较多的异常情况。
- 简洁美观、用户友好的图形界面。



## 六、心得体会

本次基于 FISCO-BCOS 区块链底层的供应链金融区块链应用开发项目，经过前中后期三个阶段。

- 前期

  学习区块链的基本原理，包括比特币白皮书、共识机制、非对称密钥体系，以及联盟链等。重点学习的实践了使用 Solidity 语言编写智能合约，并在以 Remix 为例在线编译平台试编译运行。

- 中期

  熟悉 FISCO-BCOS 区块链底层开发平台，在 FISCO-BCOS 上搭建区块链，并编译、部署、调用智能合约。

  设计和编写供应链金融区块链应用开发项目的智能合约，并且初步部署、测试。

- 后期

  搭建  JAVA SDK 环境，在 Eclipse 中集成 Spring 框架和 Gradle 工具，编写前端和后端，并连接区块链进行测试。

  至此基于 FISCO-BCOS 区块链底层的供应链金融区块链应用开发项目基本完成。

经过本次项目，我对区块链基本原理以及区块链应用开发有了深刻的认识，也获得了一个包含前后端的完整项目开发的宝贵经验。
