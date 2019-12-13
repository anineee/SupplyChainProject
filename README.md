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


## 更多
详见实验报告。
