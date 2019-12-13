package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.fisco.bcos.web3j.abi.datatypes.generated.Int256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import web3j.SupplyChain;

public class MainWindow extends JFrame{
	/*
	 * fisco-bcos
	 */
	private ApplicationContext context;
	private Service service;
	private ChannelEthereumService channelEthereumService;
	private Web3j myWeb3j;
	private Credentials credentials;
	private SupplyChain supplyChain;
	
	private static String contractAddr = "0xec3f60f3ce57049641ee90d5cfa2b2988862ec55";
	static String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"address\"},{\"name\":\"type_t\",\"type\":\"string\"}],\"name\":\"registerCompany\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"r_idx\",\"type\":\"uint256\"}],\"name\":\"showReceipt\",\"outputs\":[{\"name\":\"out_idx\",\"type\":\"uint256\"},{\"name\":\"out_from\",\"type\":\"string\"},{\"name\":\"out_to\",\"type\":\"string\"},{\"name\":\"out_value\",\"type\":\"int256\"},{\"name\":\"out_return_time\",\"type\":\"uint256\"},{\"name\":\"out_status\",\"type\":\"int256\"},{\"name\":\"out_used\",\"type\":\"bool\"},{\"name\":\"out_signer\",\"type\":\"address[2]\"},{\"name\":\"out_sign\",\"type\":\"bool[2]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"}],\"name\":\"returnDebtCompany\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"},{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"int256\"}],\"name\":\"transferReceipt\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"r_idx\",\"type\":\"uint256\"}],\"name\":\"signReceipt\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"int256\"}],\"name\":\"transferBalance\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"getMyInfo\",\"outputs\":[{\"name\":\"out_name\",\"type\":\"string\"},{\"name\":\"out_addr\",\"type\":\"address\"},{\"name\":\"out_balance\",\"type\":\"int256\"},{\"name\":\"out_type\",\"type\":\"string\"},{\"name\":\"out_r_num\",\"type\":\"uint256\"},{\"name\":\"out_r\",\"type\":\"uint256[]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"r_idx\",\"type\":\"uint256\"}],\"name\":\"checkReceiptSign\",\"outputs\":[{\"name\":\"out_signer\",\"type\":\"address[2]\"},{\"name\":\"out_sign\",\"type\":\"bool[2]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"}],\"name\":\"getReceiptIdx\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"c_name\",\"type\":\"string\"}],\"name\":\"getCompany\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"cur_time\",\"type\":\"uint256\"}],\"name\":\"returnDebt\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"int256\"},{\"name\":\"return_time\",\"type\":\"uint256\"}],\"name\":\"registerReceipt\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"}],\"name\":\"applyFinancing\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"},{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"int256\"}],\"name\":\"addBalance\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"err_code\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"from\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"int256\"},{\"indexed\":false,\"name\":\"return_time\",\"type\":\"uint256\"}],\"name\":\"returnDebtEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"balance\",\"type\":\"int256\"},{\"indexed\":false,\"name\":\"type_t\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"receipts_num\",\"type\":\"uint256\"}],\"name\":\"showCompanyEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"r_idx\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"from\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"int256\"},{\"indexed\":false,\"name\":\"return_time\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"status\",\"type\":\"int256\"},{\"indexed\":false,\"name\":\"used\",\"type\":\"bool\"}],\"name\":\"showReceiptEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"err_info\",\"type\":\"string\"}],\"name\":\"errorEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"signer\",\"type\":\"address[]\"},{\"indexed\":false,\"name\":\"sign\",\"type\":\"bool[]\"}],\"name\":\"showSignEvent\",\"type\":\"event\"}]";
	static String bin = "";
	private TransactionDecoder txDecoder = new TransactionDecoder(abi, bin);
	
	/*
	 * GUI
	 */
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
	private JPanel accountPanel = new JPanel(new BorderLayout());
	private JPanel showReceiptPanel = new JPanel(new BorderLayout());
	private JPanel companyPanel = new JPanel();
	private JPanel receiptPanel = new JPanel();
	private JPanel transferPanel = new JPanel();
	private JPanel financingPanel = new JPanel();
	private JPanel debtPanel = new JPanel();
	private JPanel addbPanel = new JPanel();
	private JPanel tbPanel = new JPanel();
	
	private JPanel accountContent = new JPanel(new GridLayout(6,2));
	private JLabel name_label = new JLabel("Name:");
	private JTextField name_text = new JTextField();
	private JLabel type_label = new JLabel("Type:");
	private JTextField type_text = new JTextField();
	private JLabel balance_label = new JLabel("Balance:");
	private JTextField balance_text = new JTextField();
	private JLabel rnum_label = new JLabel("Receipt Number:");
	private JTextField rnum_text = new JTextField();
	private JLabel rlist_label = new JLabel("Receipt Index List:");
	private JTextField rlist_text = new JTextField();
	
	/*
	 * my info
	 */
	private String myName;
	private String myType;
	private BigInteger myAddr;
	
	/*
	 * help attribute
	 */
	static BigInteger m1 = BigInteger.ONE.negate();
	static BigInteger m2 = m1.subtract(BigInteger.ONE);
	static BigInteger m3 = m2.subtract(BigInteger.ONE);
	static BigInteger m4 = m3.subtract(BigInteger.ONE);
	
	static String e88 = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
	static BigInteger uint256_m1 = new BigInteger(e88,16);
	static BigInteger uint256_m2 = uint256_m1.subtract(BigInteger.ONE);
	static BigInteger uint256_m3 = uint256_m2.subtract(BigInteger.ONE);
	static BigInteger uint256_m4 = uint256_m3.subtract(BigInteger.ONE);
	
	static String adminAddr = "0x8a5849e36fbaac6f8232344410aae9c4bbc5d3b7";
	
	public MainWindow(JFrame start, String pk_str, String name, String type_t, boolean is_loadup) throws Exception {
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
		
		if(credentials.getAddress().contentEquals(adminAddr)) {
			init_admin();
			start.setVisible(false);
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return;
		}
		
		TransactionReceipt getMyInfoTx = supplyChain.getMyInfo().send();
		String strJson = txDecoder.decodeOutputReturnJson(getMyInfoTx.getInput(), getMyInfoTx.getOutput());
		Map<String,String> outJson = JSONArr2Map(strJson);
		List<JSONObject> listJson = JSONArr2List(strJson);
		BigInteger addr_t = new BigInteger(outJson.get("out_addr").substring(2), 16);
		
		if(is_loadup) {
			if(!addr_t.equals(BigInteger.ZERO)) {
				String name_t = outJson.get("out_name");
				String type_tt = outJson.get("out_type");
				BigInteger balance_t = new BigInteger(outJson.get("out_balance"));
				BigInteger r_num_t = new BigInteger(outJson.get("out_r_num"));
				int j = 0;
				for(; j < listJson.size(); j ++) {
					if(listJson.get(j).getString("name").contentEquals("out_r"))
						break;
				}
				System.out.println(listJson.get(j).getString("data"));
				JSONArray r_json_arr = listJson.get(j).getJSONArray("data");
				List<BigInteger> r_list_t = new LinkedList<BigInteger>();
				for(int i = 0; i < r_json_arr.size(); i ++) {
					BigInteger r_obj = r_json_arr.getBigInteger(i);
					r_list_t.add(r_obj);
				}
				
				init(name_t, type_tt, addr_t, balance_t, r_num_t, r_list_t);
				start.setVisible(false);
				this.setVisible(true);
				this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
			else {
				JOptionPane.showMessageDialog(null, "The Priavte Key hasn\'t been registered.Please load in.", "Load In Error",JOptionPane.ERROR_MESSAGE);
			}
		}
		
		else {
			if(!addr_t.equals(BigInteger.ZERO)) {
				JOptionPane.showMessageDialog(null, "The Priavte Key has been registered.Please load up.", "Load In Error",JOptionPane.ERROR_MESSAGE);
			}
			else {
				TransactionReceipt tx;
				myAddr = new BigInteger(credentials.getAddress().substring(2),16);
				tx = supplyChain.registerCompany(name, credentials.getAddress(), type_t).send();
				String strJsonTx = txDecoder.decodeOutputReturnJson(tx.getInput(), tx.getOutput());
				Map<String,String> out = JSONArr2Map(strJsonTx);
				BigInteger ret_code = new BigInteger(out.get(""));
				System.out.println(ret_code);
				if(ret_code.equals(uint256_m1)) {
					JOptionPane.showMessageDialog(null, "The Name has been registered.", "Load In Error",JOptionPane.ERROR_MESSAGE);
				}
				else if(ret_code.equals(BigInteger.ZERO)) {
					init(name, type_t, myAddr, BigInteger.ZERO, BigInteger.ZERO, new LinkedList<BigInteger>());
					start.setVisible(false);
					this.setVisible(true);
					this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
		}
		
	}
	
	private void refreshAccountInfo() {
		TransactionReceipt getMyInfoTx;
		try {
			getMyInfoTx = supplyChain.getMyInfo().send();
			String strJson = txDecoder.decodeOutputReturnJson(getMyInfoTx.getInput(), getMyInfoTx.getOutput());
			Map<String,String> outJson = JSONArr2Map(strJson);
			List<JSONObject> listJson = JSONArr2List(strJson);
			BigInteger addr_t = new BigInteger(outJson.get("out_addr").substring(2), 16);
			
			BigInteger balance_t = new BigInteger(outJson.get("out_balance"));
			BigInteger r_num_t = new BigInteger(outJson.get("out_r_num"));
			int j = 0;
			for(; j < listJson.size(); j ++) {
				if(listJson.get(j).getString("name").contentEquals("out_r"))
					break;
			}
			System.out.println(listJson.get(j).getString("data"));
			JSONArray r_json_arr = listJson.get(j).getJSONArray("data");
			List<BigInteger> r_list_t = new LinkedList<BigInteger>();
			for(int i = 0; i < r_json_arr.size(); i ++) {
				BigInteger r_obj = r_json_arr.getBigInteger(i);
				r_list_t.add(r_obj);
			}
			
			balance_text.setText(balance_t.toString());	
			rnum_text.setText(r_num_t.toString());
			String list = "[ ";
			for(BigInteger i : r_list_t) {
				list += i.toString();
				list += " ";
			}
			list += "]";
			rlist_text.setText(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	private void init_admin() {
		this.setSize(600, 400);
		this.setTitle("Supply Chain App:Admin");
		/*
		 * showReceipt Panel
		 */
		JPanel showReceiptContent = new JPanel();
		Box inBox1 =  Box.createVerticalBox();
		JLabel ridx_label = new JLabel("Please enter the index of receipt:");
		JTextField ridx_text = new JTextField();
		JButton show_receipt_button = new JButton("OK");
		inBox1.add(ridx_label);
		inBox1.add(ridx_text);
		inBox1.add(show_receipt_button);
		showReceiptContent.add(inBox1);
		
		JPanel outPanel = new JPanel(new GridLayout(10,2));
		JLabel show_ridx_label = new JLabel("Receipt Index:");
		JTextField show_ridx_text = new JTextField();
		show_ridx_text.disable();
		JLabel from_label = new JLabel("From:");
		JTextField from_text = new JTextField();
		from_text.disable();
		JLabel to_label = new JLabel("To:");
		JTextField to_text = new JTextField();
		to_text.disable();
		JLabel val_label = new JLabel("Value:");
		JTextField val_text = new JTextField();
		val_text.disable();
		JLabel return_time_label = new JLabel("Return Time:");
		JTextField return_time_text = new JTextField();
		return_time_text.disable();
		JLabel status_label = new JLabel("Status:");
		JTextField status_text = new JTextField();
		status_text.disable();
		JLabel used_label = new JLabel("Used:");
		JTextField used_text = new JTextField();
		used_text.disable();
		JLabel signer_label = new JLabel("Signer:");
		JTextField signer_text = new JTextField();
		signer_text.disable();
		JLabel sign_label = new JLabel("Sign:");
		JTextField sign_text = new JTextField();
		sign_text.disable();
		outPanel.add(show_ridx_label);
		outPanel.add(show_ridx_text);
		outPanel.add(from_label);
		outPanel.add(from_text);
		outPanel.add(to_label);
		outPanel.add(to_text);
		outPanel.add(val_label);
		outPanel.add(val_text);
		outPanel.add(return_time_label);
		outPanel.add(return_time_text);
		outPanel.add(status_label);
		outPanel.add(status_text);
		outPanel.add(used_label);
		outPanel.add(used_text);
		outPanel.add(signer_label);
		outPanel.add(signer_text);
		outPanel.add(sign_label);
		outPanel.add(sign_text);
		
		showReceiptPanel.add(showReceiptContent);
		showReceiptPanel.add(outPanel, BorderLayout.SOUTH);
		
		show_receipt_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt showReceiptTx;
				try {
					showReceiptTx = supplyChain.showReceipt(new BigInteger(ridx_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(showReceiptTx.getInput(), showReceiptTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					Map<String, String> outJson = JSONArr2Map(strJson);
					
					show_ridx_text.setText(ridx_text.getText());
					from_text.setText(outJson.get("out_from"));
					to_text.setText(outJson.get("out_to"));
					val_text.setText(outJson.get("out_value"));
					return_time_text.setText(outJson.get("out_return_time"));
					status_text.setText(outJson.get("out_status"));
					used_text.setText(outJson.get("out_used"));
					
					int j = 0;
					for(; j < listJson.size(); j ++) {
						if(listJson.get(j).getString("name").contentEquals("out_signer"))
							break;
					}
					System.out.println(listJson.get(j).getString("data"));
					JSONArray signerArr = listJson.get(j).getJSONArray("data");
					List<String> signer = new LinkedList<String>();
					for(int i = 0; i < signerArr.size(); i ++) {
						signer.add(signerArr.getString(i));
					}
					signer_text.setText("[ 0x"+signer.get(0)+", 0x"+signer.get(1)+" ]");
					
					j = 0;
					for(; j < listJson.size(); j ++) {
						if(listJson.get(j).getString("name").contentEquals("out_sign"))
							break;
					}
					System.out.println(listJson.get(j).getString("data"));
					JSONArray signArr = listJson.get(j).getJSONArray("data");
					List<String> sign = new LinkedList<String>();
					for(int i = 0; i < signArr.size(); i ++) {
						sign.add(signArr.getString(i));
					}
					sign_text.setText("[" + sign.get(0)+", "+sign.get(1)+" ]");
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Company Panel
		 */
		JPanel companyContent = new JPanel();
		Box inBox2 =  Box.createVerticalBox();
		JLabel cname_label = new JLabel("Please enter the name of company:");
		JTextField cname_text = new JTextField();
		JButton company_button = new JButton("OK");
		inBox2.add(cname_label);
		inBox2.add(cname_text);
		inBox2.add(company_button);
		companyContent.add(inBox2);
		
		JPanel outPanel2 = new JPanel(new GridLayout(4,2));
		JLabel cidx_label = new JLabel("Company Index:");
		JTextField cidx_text = new JTextField();
		cidx_text.disable();
		JLabel cn_label = new JLabel("Name:");
		JTextField cn_text = new JTextField();
		cn_text.disable();
		JLabel caddr_label = new JLabel("Address:");
		JTextField caddr_text = new JTextField();
		caddr_text.disable();
		JLabel ctype_label = new JLabel("Type:");
		JTextField ctype_text = new JTextField();
		ctype_text.disable();
		outPanel2.add(cidx_label);
		outPanel2.add(cidx_text);
		outPanel2.add(cn_label);
		outPanel2.add(cn_text);
		outPanel2.add(caddr_label);
		outPanel2.add(caddr_text);
		outPanel2.add(ctype_label);
		outPanel2.add(ctype_text);
		
		companyPanel.add(companyContent);
		companyPanel.add(outPanel2, BorderLayout.SOUTH);
		
		company_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt companyTx;
				try {
					companyTx = supplyChain.getCompany(cname_text.getText()).send();
					String strJson = txDecoder.decodeOutputReturnJson(companyTx.getInput(), companyTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					
					cidx_text.setText(listJson.get(0).getString("data"));
					cn_text.setText(listJson.get(1).getString("data"));
					caddr_text.setText(listJson.get(2).getString("data"));
					ctype_text.setText(listJson.get(3).getString("data"));
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Add Balance Panel
		 */
		JPanel addbContent = new JPanel();
		Box inBox3 =  Box.createVerticalBox();
		JLabel acname_label = new JLabel("Name:");
		JTextField acname_text = new JTextField(20);
		JLabel aval_label = new JLabel("Value:");
		JTextField aval_text = new JTextField(20);
		JButton addb_button = new JButton("OK");
		inBox3.add(acname_label);
		inBox3.add(acname_text);
		inBox3.add(aval_label);
		inBox3.add(aval_text);
		inBox3.add(addb_button);
		addbContent.add(inBox3);
		
		addbPanel.add(addbContent);
		
		addb_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt addbTx;
				try {
					addbTx = supplyChain.addBalance(acname_text.getText(), new BigInteger(aval_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(addbTx.getInput(), addbTx.getOutput());
					System.out.println(strJson);
					Map<String,String> out = JSONArr2Map(strJson);
					BigInteger ret_code = new BigInteger(out.get(""));
					System.out.println(ret_code);
					if(ret_code.equals(m2)) {
						JOptionPane.showMessageDialog(null, "The Name has been registered.", "Add Balance Error",JOptionPane.ERROR_MESSAGE);
					}
					else if(ret_code.equals(BigInteger.ZERO)) {
						JOptionPane.showMessageDialog(null, "Add Balance Success.");
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		tabbedPane.add("Show & Sign Receipt", showReceiptPanel);
		tabbedPane.add("Show Company", companyPanel);
		tabbedPane.add("Add Balance", addbPanel);
		
		this.add(tabbedPane);
	}

	private void init(String name, String type_t, BigInteger addr, BigInteger balance, BigInteger r_num, List<BigInteger> r_list) {
		this.setSize(600, 400);
		this.setTitle("Supply Chain App:Client");
		/*
		 * account panel
		 */
		myName = name;
		name_text.setText(name);
		name_text.disable();
		
		myType = type_t;
		type_text.setText(type_t);
		type_text.disable();
		
		balance_text.setText(balance.toString());
		balance_text.disable();
		
		rnum_text.setText(r_num.toString());
		rnum_text.disable();
		
		String list = "[ ";
		for(BigInteger i : r_list) {
			list += i.toString();
			list += " ";
		}
		list += "]";
		rlist_text.setText(list);
		rlist_text.disable();
		
		Box refresh_b = Box.createHorizontalBox();
		JButton refresh_button = new JButton("Refresh");
		refresh_button.setPreferredSize(new Dimension(100,50));
		refresh_b.add(refresh_button);
		
		accountContent.add(name_label);
		accountContent.add(name_text);
		accountContent.add(type_label);
		accountContent.add(type_text);
		accountContent.add(balance_label);
		accountContent.add(balance_text);
		accountContent.add(rnum_label);
		accountContent.add(rnum_text);
		accountContent.add(rlist_label);
		accountContent.add(rlist_text);
		accountContent.add(refresh_b);
		accountPanel.add(accountContent);
		
		refresh_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshAccountInfo();
			}
		});
		
		/*
		 * showReceipt Panel
		 */
		JPanel showReceiptContent = new JPanel();
		Box inBox1 =  Box.createVerticalBox();
		JLabel ridx_label = new JLabel("Please enter the index of receipt:");
		JTextField ridx_text = new JTextField();
		JButton show_receipt_button = new JButton("OK");
		inBox1.add(ridx_label);
		inBox1.add(ridx_text);
		inBox1.add(show_receipt_button);
		showReceiptContent.add(inBox1);
		
		JPanel outPanel = new JPanel(new GridLayout(10,2));
		JLabel show_ridx_label = new JLabel("Receipt Index:");
		JTextField show_ridx_text = new JTextField();
		show_ridx_text.disable();
		JLabel from_label = new JLabel("From:");
		JTextField from_text = new JTextField();
		from_text.disable();
		JLabel to_label = new JLabel("To:");
		JTextField to_text = new JTextField();
		to_text.disable();
		JLabel val_label = new JLabel("Value:");
		JTextField val_text = new JTextField();
		val_text.disable();
		JLabel return_time_label = new JLabel("Return Time:");
		JTextField return_time_text = new JTextField();
		return_time_text.disable();
		JLabel status_label = new JLabel("Status:");
		JTextField status_text = new JTextField();
		status_text.disable();
		JLabel used_label = new JLabel("Used:");
		JTextField used_text = new JTextField();
		used_text.disable();
		JLabel signer_label = new JLabel("Signer:");
		JTextField signer_text = new JTextField();
		signer_text.disable();
		JLabel sign_label = new JLabel("Sign:");
		JTextField sign_text = new JTextField();
		sign_text.disable();
		JButton sign_button = new JButton("Sign");
		sign_button.disable();
		outPanel.add(show_ridx_label);
		outPanel.add(show_ridx_text);
		outPanel.add(from_label);
		outPanel.add(from_text);
		outPanel.add(to_label);
		outPanel.add(to_text);
		outPanel.add(val_label);
		outPanel.add(val_text);
		outPanel.add(return_time_label);
		outPanel.add(return_time_text);
		outPanel.add(status_label);
		outPanel.add(status_text);
		outPanel.add(used_label);
		outPanel.add(used_text);
		outPanel.add(signer_label);
		outPanel.add(signer_text);
		outPanel.add(sign_label);
		outPanel.add(sign_text);
		outPanel.add(sign_button);
		
		showReceiptPanel.add(showReceiptContent);
		showReceiptPanel.add(outPanel, BorderLayout.SOUTH);
		
		show_receipt_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt showReceiptTx;
				try {
					showReceiptTx = supplyChain.showReceipt(new BigInteger(ridx_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(showReceiptTx.getInput(), showReceiptTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					Map<String, String> outJson = JSONArr2Map(strJson);
					
					show_ridx_text.setText(ridx_text.getText());
					from_text.setText(outJson.get("out_from"));
					to_text.setText(outJson.get("out_to"));
					val_text.setText(outJson.get("out_value"));
					return_time_text.setText(outJson.get("out_return_time"));
					status_text.setText(outJson.get("out_status"));
					used_text.setText(outJson.get("out_used"));
					
					int j = 0;
					for(; j < listJson.size(); j ++) {
						if(listJson.get(j).getString("name").contentEquals("out_signer"))
							break;
					}
					System.out.println(listJson.get(j).getString("data"));
					JSONArray signerArr = listJson.get(j).getJSONArray("data");
					List<String> signer = new LinkedList<String>();
					for(int i = 0; i < signerArr.size(); i ++) {
						signer.add(signerArr.getString(i));
					}
					signer_text.setText("[ 0x"+signer.get(0)+", 0x"+signer.get(1)+" ]");
					
					j = 0;
					for(; j < listJson.size(); j ++) {
						if(listJson.get(j).getString("name").contentEquals("out_sign"))
							break;
					}
					System.out.println(listJson.get(j).getString("data"));
					JSONArray signArr = listJson.get(j).getJSONArray("data");
					List<String> sign = new LinkedList<String>();
					for(int i = 0; i < signArr.size(); i ++) {
						sign.add(signArr.getString(i));
					}
					sign_text.setText("[" + sign.get(0)+", "+sign.get(1)+" ]");
					
					sign_button.enable();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		sign_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt signTx;
				try {
					signTx = supplyChain.signReceipt(new BigInteger(ridx_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(signTx.getInput(), signTx.getOutput());
					System.out.println(strJson);
					Map<String,String> out = JSONArr2Map(strJson);
					int ret_code = Integer.valueOf(out.get(""));
					System.out.println(ret_code);
					if(ret_code == 0) {
						JOptionPane.showMessageDialog(null, "Sign Success!");
					}
					else {
						JOptionPane.showMessageDialog(null, "Sign Fail!", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					sign_button.disable();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Company Panel
		 */
		JPanel companyContent = new JPanel();
		Box inBox2 =  Box.createVerticalBox();
		JLabel cname_label = new JLabel("Please enter the name of company:");
		JTextField cname_text = new JTextField();
		JButton company_button = new JButton("OK");
		inBox2.add(cname_label);
		inBox2.add(cname_text);
		inBox2.add(company_button);
		companyContent.add(inBox2);
		
		JPanel outPanel2 = new JPanel(new GridLayout(4,2));
		JLabel cidx_label = new JLabel("Company Index:");
		JTextField cidx_text = new JTextField();
		cidx_text.disable();
		JLabel cn_label = new JLabel("Name:");
		JTextField cn_text = new JTextField();
		cn_text.disable();
		JLabel caddr_label = new JLabel("Address:");
		JTextField caddr_text = new JTextField();
		caddr_text.disable();
		JLabel ctype_label = new JLabel("Type:");
		JTextField ctype_text = new JTextField();
		ctype_text.disable();
		outPanel2.add(cidx_label);
		outPanel2.add(cidx_text);
		outPanel2.add(cn_label);
		outPanel2.add(cn_text);
		outPanel2.add(caddr_label);
		outPanel2.add(caddr_text);
		outPanel2.add(ctype_label);
		outPanel2.add(ctype_text);
		
		companyPanel.add(companyContent);
		companyPanel.add(outPanel2, BorderLayout.SOUTH);
		
		company_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt companyTx;
				try {
					companyTx = supplyChain.getCompany(cname_text.getText()).send();
					String strJson = txDecoder.decodeOutputReturnJson(companyTx.getInput(), companyTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					
					cidx_text.setText(listJson.get(0).getString("data"));
					cn_text.setText(listJson.get(1).getString("data"));
					caddr_text.setText(listJson.get(2).getString("data"));
					ctype_text.setText(listJson.get(3).getString("data"));
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Register Receipt Panel
		 */
		JPanel receiptContent = new JPanel();
		Box inBox3 =  Box.createVerticalBox();
		JLabel rfrom_label = new JLabel("From:");
		JTextPane rfrom_text = new JTextPane();
		rfrom_text.setText(myName);
		rfrom_text.disable();
		JLabel rto_label = new JLabel("To:");
		JTextPane rto_text = new JTextPane();
		JLabel rval_label = new JLabel("Value:");
		JTextPane rval_text = new JTextPane();
		JLabel rreturn_time_label = new JLabel("Return Time:");
		JTextPane rreturn_time_text = new JTextPane();
		JButton receipt_button = new JButton("OK");
		inBox3.add(rfrom_label);
		inBox3.add(rfrom_text);
		inBox3.add(rto_label);
		inBox3.add(rto_text);
		inBox3.add(rval_label);
		inBox3.add(rval_text);
		inBox3.add(rreturn_time_label);
		inBox3.add(rreturn_time_text);
		inBox3.add(receipt_button);
		receiptContent.add(inBox3);
		receiptPanel.add(receiptContent);
		
		receipt_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt receiptTx;
				try {
					receiptTx = supplyChain.registerReceipt(rfrom_text.getText(), rto_text.getText(), new BigInteger(rval_text.getText()), new BigInteger(rreturn_time_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(receiptTx.getInput(), receiptTx.getOutput());
					System.out.println(strJson);
					Map<String,String> out = JSONArr2Map(strJson);
					BigInteger ret_code = new BigInteger(out.get(""));
					System.out.println(ret_code);
					
					if(ret_code.equals(uint256_m1)) {
						JOptionPane.showMessageDialog(null, "Receiver hasn\'t registered", "Message",JOptionPane.ERROR_MESSAGE);
					}
					else if(ret_code.compareTo(BigInteger.ZERO) >= 0) {
						JOptionPane.showMessageDialog(null, "New Receipt Index:"+ret_code.toString());
						refreshAccountInfo();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Transfer Receipt Panel
		 */
		JPanel transferContent = new JPanel();
		Box inBox4 =  Box.createVerticalBox();
		JLabel tfrom_label = new JLabel("From:");
		JTextPane tfrom_text = new JTextPane();
		tfrom_text.setText(myName);
		tfrom_text.disable();
		JLabel tto_label = new JLabel("To:");
		JTextPane tto_text = new JTextPane();
		JLabel tval_label = new JLabel("Value:");
		JTextPane tval_text = new JTextPane();
		JButton transfer_button = new JButton("OK");
		inBox4.add(tfrom_label);
		inBox4.add(tfrom_text);
		inBox4.add(tto_label);
		inBox4.add(tto_text);
		inBox4.add(tval_label);
		inBox4.add(tval_text);
		inBox4.add(transfer_button);
		transferContent.add(inBox4);
		transferPanel.add(transferContent);
		
		transfer_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt transferTx;
				try {
					transferTx = supplyChain.transferReceipt(tfrom_text.getText(), tto_text.getText(), new BigInteger(tval_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(transferTx.getInput(), transferTx.getOutput());
					System.out.println(strJson);
					Map<String,String> out = JSONArr2Map(strJson);
					BigInteger ret_code = new BigInteger(out.get(""));
					System.out.println(ret_code);
					
					if(ret_code.compareTo(uint256_m1) == 0){
						JOptionPane.showMessageDialog(null, "Receiver hasn\'t registered", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code.compareTo(uint256_m2) == 0){
						JOptionPane.showMessageDialog(null, "Lack of value", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code.compareTo(BigInteger.ZERO) >= 0) {
						JOptionPane.showMessageDialog(null, "Transfer Balance Success.");
						refreshAccountInfo();
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Financing Panel
		 */
		JPanel financingContent = new JPanel();
		Box inBox5 =  Box.createVerticalBox();
		JLabel ffrom_label = new JLabel("From:");
		JTextPane ffrom_text = new JTextPane();
		ffrom_text.setText(myName);
		ffrom_text.disable();
		JLabel fto_label = new JLabel("To:");
		JTextPane fto_text = new JTextPane();
		JButton financing_button = new JButton("OK");
		inBox5.add(ffrom_label);
		inBox5.add(ffrom_text);
		inBox5.add(fto_label);
		inBox5.add(fto_text);
		inBox5.add(financing_button);
		financingContent.add(inBox5);
		financingPanel.add(financingContent);
		
		financing_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt financingTx;
				try {
					financingTx = supplyChain.applyFinancing(ffrom_text.getText(), fto_text.getText()).send();
					String strJson = txDecoder.decodeOutputReturnJson(financingTx.getInput(), financingTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					int ret_code = listJson.get(0).getInteger("data");
					if(ret_code == -1){
						JOptionPane.showMessageDialog(null, "Receiver hasn\'t registered", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code == -2){
						JOptionPane.showMessageDialog(null, "Receiver isn\'t a bank", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code == -4){
						JOptionPane.showMessageDialog(null, "Lack of Receipts", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code >= 0) {
						JOptionPane.showMessageDialog(null, "Financing Value:"+listJson.get(1).getBigInteger("data").toString());
						refreshAccountInfo();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Return Debt Panel
		 */
		JPanel debtContent = new JPanel();
		Box inBox6 =  Box.createVerticalBox();
		JLabel dfrom_label = new JLabel("From:");
		JTextPane dfrom_text = new JTextPane();
		dfrom_text.setText(myName);
		dfrom_text.disable();
		JLabel dto_label = new JLabel("To:");
		JTextPane dto_text = new JTextPane();
		JButton debt_button = new JButton("OK");
		inBox6.add(dfrom_label);
		inBox6.add(dfrom_text);
		inBox6.add(dto_label);
		inBox6.add(dto_text);
		inBox6.add(debt_button);
		debtContent.add(inBox6);
		debtPanel.add(debtContent);
		
		debt_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt debtTx;
				try {
					debtTx = supplyChain.returnDebtCompany(dfrom_text.getText(), dto_text.getText()).send();
					String strJson = txDecoder.decodeOutputReturnJson(debtTx.getInput(), debtTx.getOutput());
					System.out.println(strJson);
					List<JSONObject> listJson = JSONArr2List(strJson);
					int ret_code = listJson.get(0).getInteger("data");
					
					if(ret_code == -1){
						JOptionPane.showMessageDialog(null, "Receiver hasn\'t registered", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code == -2){
						JOptionPane.showMessageDialog(null, "Lack of Balance", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code == -3){
						JOptionPane.showMessageDialog(null, "Lack of Receipts", "Message",JOptionPane.ERROR_MESSAGE); 
					}
					else if(ret_code == 0) {
						JOptionPane.showMessageDialog(null, "Return Value:"+listJson.get(1).getBigInteger("data").toString());
						refreshAccountInfo();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		/*
		 * Transfer Balance Panel
		 */
		JPanel tbContent = new JPanel();
		Box inBox7 =  Box.createVerticalBox();
		JLabel tbfrom_label = new JLabel("From:");
		JTextField tbfrom_text = new JTextField(20);
		tbfrom_text.setText(myName);
		tbfrom_text.disable();
		JLabel tbto_label = new JLabel("To:");
		JTextField tbto_text = new JTextField(20);
		JLabel tbval_label = new JLabel("Value:");
		JTextField tbval_text = new JTextField(20);
		JButton tb_button = new JButton("OK");
		inBox7.add(tbfrom_label);
		inBox7.add(tbfrom_text);
		inBox7.add(tbto_label);
		inBox7.add(tbto_text);
		inBox7.add(tbval_label);
		inBox7.add(tbval_text);
		inBox7.add(tb_button);
		tbContent.add(inBox7);
		tbPanel.add(tbContent);
		
		tb_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionReceipt tbTx;
				try {
					tbTx = supplyChain.transferBalance(tbfrom_text.getText(), tbto_text.getText(), new BigInteger(tbval_text.getText())).send();
					String strJson = txDecoder.decodeOutputReturnJson(tbTx.getInput(), tbTx.getOutput());
					System.out.println(strJson);
					Map<String,String> out = JSONArr2Map(strJson);
					int ret_code = Integer.valueOf(out.get(""));
					System.out.println(ret_code);
					if(ret_code == 0) {
						JOptionPane.showMessageDialog(null, "Transfer Balance "+tbval_text.getText()+" Success.");
						refreshAccountInfo();
					}
					else if(ret_code == -1) {
						JOptionPane.showMessageDialog(null, "Receiver hasn\'t registered", "Message",JOptionPane.ERROR_MESSAGE);
					}
					else if(ret_code == -2) {
						JOptionPane.showMessageDialog(null, "Lack of Balance.", "Message",JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		tabbedPane.add("Account", accountPanel);
		tabbedPane.add("Show & Sign Receipt", showReceiptPanel);
		tabbedPane.add("Show Company", companyPanel);
		if(myType.contentEquals("company")) {
			tabbedPane.add("Register Receipt", receiptPanel);
			tabbedPane.add("Transfer Receipt", transferPanel);
			tabbedPane.add("Financing", financingPanel);
			tabbedPane.add("Return Debt", debtPanel);
		}
		if(!myType.contentEquals("arbitrator")) {
			tabbedPane.add("Transfer Balance", tbPanel);
		}
		this.add(tabbedPane);
	}
	
	private Map<String,String> JSONArr2Map(String strJson) {
		JSONObject outJson = JSON.parseObject(strJson);
		JSONArray result = outJson.getJSONArray("result");
		Map<String,String> nv = new HashMap<String,String>();
		for(int i = 0; i < result.size(); i ++) {
			JSONObject obj = result.getJSONObject(i);
			nv.put(obj.getString("name"), obj.getString("data"));
		}
		return nv;
	}
	
	private List<JSONObject> JSONArr2List(String strJson) {
		JSONObject outJson = JSON.parseObject(strJson);
		JSONArray result = outJson.getJSONArray("result");
		List<JSONObject>nv = new LinkedList<JSONObject>();
		for(int i = 0; i < result.size(); i ++) {
			JSONObject obj = result.getJSONObject(i);
			nv.add(obj);
		}
		return nv;
	}
}