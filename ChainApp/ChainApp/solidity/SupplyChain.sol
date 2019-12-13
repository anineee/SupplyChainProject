pragma solidity ^0.4.24;

contract SupplyChain {
	// 管理员账号地址
	address admin_addr;

	event returnDebtEvent(string err_code, string from, string to, int256 value, uint return_time);
	event showCompanyEvent(string name, address addr, int256 balance, string type_t, uint receipts_num);
	event showReceiptEvent(uint256 r_idx, string from, string to, int256 value, uint return_time, int status, bool used);
	event errorEvent(string err_info);
	event showSignEvent(address[] signer, bool[] sign);

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

	uint256 companies_num;	// 注册公司数量
	Company[] companies;	// 注册公司动态数组
	uint256 receipts_num;	// 回执数量
	Receipt[] receipts;		// 回执动态数组

	// ================= 构造函数 =====================

	/*
	*	描述：
	*		由管理员账户部署合约
	*/
	constructor() public {
		admin_addr = msg.sender;
	}

	// ================= 私有函数 =====================

	/*
	*	描述：
	*		比较两字符串是否相同
	*/
	function utilCompareInternal(string memory a, string memory b) internal returns (bool) {
	    if (bytes(a).length != bytes(b).length) {
	        return false;
	    }
	    for (uint i = 0; i < bytes(a).length; i ++) {
	        if(bytes(a)[i] != bytes(b)[i]) {
	            return false;
	        }
	    }
	    return true;
	}

	/*
	* 	描述：
	*		根据公司名称取得该公司在公司动态数组中的下标
	*	参数：
	*		name 公司名称
	*	返回值：
	*		该公司在公司动态数组中的下标
	*/
	function getCompanyIdx(string memory name) private returns(uint256) {
		for(uint256 i = 0; i < companies_num; i ++) {
			if(utilCompareInternal(companies[i].name, name))
				return i;
		}
		return (uint256)(-1);
	}

	/*
	* 	描述：
	*		根据公司地址取得该公司在公司动态数组中的下标
	*	参数：
	*		addr 公司地址
	*	返回值：
	*		该公司在公司动态数组中的下标
	*/
	function getCompanyIdxByAddr(address addr) private returns(uint256) {
		for(uint256 i = 0; i < companies_num; i ++) {
			if(companies[i].addr == addr)
				return i;
		}
		return (uint256)(-1);
	}

	/*
	*	描述：
	*		生成新的公司实例，并加入公司动态数组
	*	参数：
	*		name	公司名称
	*		addr 	公司账户地址
	*		balance 公司账户余额
	*		type_t	公司类型
	*/
	function addCompany(string memory name, address addr, int256 balance, string memory type_t) private {
		companies.length ++;
		companies[companies_num] = Company(name, addr, balance, type_t, 0);
		companies_num ++;

		emit showCompanyEvent(name, addr, balance, type_t, 0);
	}

	/*
	*	描述：
	*		生成新的回执实例，并加入回执动态数组
	*	参数：
	*		from_idx	付款公司的数组下标
	*		to_idx		收款公司的数组下标
	*		from 		付款公司名称
	*		to 			收款公司名称
	*		value 		款项大小
	*		return_time 付款时限
	*/
	function addReceipt(uint256 from_idx, uint256 to_idx, string memory from, string memory to, int256 value, uint return_time) private {
		receipts.length ++;
//		receipts[receipts_num] = Receipt(from, to, value, return_time, 0, false);
		receipts[receipts_num].from = from;
		receipts[receipts_num].to = to;
		receipts[receipts_num].value = value;
		receipts[receipts_num].return_time = return_time;
		receipts[receipts_num].status = 0;
		receipts[receipts_num].used = false;
		receipts[receipts_num].signer.length += 2;
		receipts[receipts_num].sign.length += 2;
		receipts[receipts_num].signer[0] = companies[to_idx].addr;
		receipts[receipts_num].sign[0] = receipts[receipts_num].sign[1] = false;
		receipts_num ++;

		companies[from_idx].receipts[companies[from_idx].receipts_num] = receipts_num-1;
		companies[from_idx].receipts_num ++;

		companies[to_idx].receipts[companies[to_idx].receipts_num] = receipts_num-1;
		companies[to_idx].receipts_num ++;

		emit showReceiptEvent(receipts_num-1, from, to, value, return_time, 0, false);
	}

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
	*/
	function transferBalancePrivate(string memory from, string memory to, int256 value) private returns(int) {
		uint256 from_idx = getCompanyIdx(from);
		if(from_idx == (uint256)(-1))
			return -1;

		uint256 to_idx = getCompanyIdx(to);
		if(to_idx == (uint256)(-1))
			return -1;

		if(companies[from_idx].balance < value)
			return -2;

		companies[from_idx].balance -= value;
		companies[to_idx].balance += value;

		return 0;
	}

	// ================= 接口函数 =====================

	// =============== 回执操作函数 ===================

	/*
	*	描述：
	*		输出回执信息并取得该回执的数组下标
	*	参数：
	*		from 	付款公司名称
	*		to 		收款公司名称
	*	返回值：
	*		取得回执下标的数量
	*/
	function getReceiptIdx(string memory from, string memory to) public returns(uint256) {
		uint256 r_num = 0;
		for(uint256 i = 0; i < receipts_num; i ++) {
			if(utilCompareInternal(receipts[i].from, from)
				&& utilCompareInternal(receipts[i].to, to)){
				r_num ++;
				emit showReceiptEvent(i ,receipts[i].from, receipts[i].to, receipts[i].value, receipts[i].return_time, receipts[i].status, receipts[i].used);
			}	
		}
		return r_num;
	}

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

	/*
	*	描述：
	*		查看回执签名
	*	参数：
	*		r_idx	回执的数组下标
	*/
	function checkReceiptSign(uint256 r_idx) public returns(address[2] out_signer, bool[2] out_sign){
		emit showSignEvent(receipts[r_idx].signer, receipts[r_idx].sign);
		
		for(uint256 i = 0; i < 2; i ++) {
			out_signer[i] = receipts[r_idx].signer[i];
			out_sign[i] = receipts[r_idx].sign[i];
		}
	}

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

	// =============== 供应链金融操作函数 ===================
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

	/*
	*	描述：
	*		自动归还所有到期账款
	*	参数：
	*		cur_time	当前时间
	*/
	function returnDebt(uint cur_time) public{
		for(uint256 i = 0; i < receipts_num; i ++) {
			if(receipts[i].status == 1 && receipts[i].return_time <= cur_time) {
				uint256 from_idx = getCompanyIdx(receipts[i].from);
				uint256 to_idx = getCompanyIdx(receipts[i].to);
				// 付款公司余额不足，还款失败
				if(companies[from_idx].balance < receipts[i].value) {
					receipts[i].status = 3;
					emit returnDebtEvent("RETURN FAIL", receipts[i].from, receipts[i].to, receipts[i].value, receipts[i].return_time);
				}
				else {
					transferBalancePrivate(receipts[i].from, receipts[i].to, receipts[i].value);
					receipts[i].status = 4;
					emit returnDebtEvent("RETURN SUCCESS", receipts[i].from, receipts[i].to, receipts[i].value, receipts[i].return_time);
				}
			}
		}
	}

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
}